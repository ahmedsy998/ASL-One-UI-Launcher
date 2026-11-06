package com.example.engine

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.LruCache
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.db.AppOverrideEntity
import com.example.model.InstalledApp
import com.example.model.OneUiSystemIconKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManagerEngine(private val context: Context) {

    private val iconCache = LruCache<String, ImageBitmap>(120)

    data class SystemCatalogEntry(
        val virtualPackage: String,
        val labelAr: String,
        val labelEn: String,
        val kind: OneUiSystemIconKind,
        val matchKeywords: List<String>
    )

    val oneUiCoreCatalog = listOf(
        SystemCatalogEntry("oneui.shortcut.phone", "الهاتف", "Phone", OneUiSystemIconKind.PHONE, listOf("dialer", "telecom", "phone", "contacts.activities.dialtacts")),
        SystemCatalogEntry("oneui.shortcut.messages", "الرسائل", "Messages", OneUiSystemIconKind.MESSAGES, listOf("messaging", "mms", "sms")),
        SystemCatalogEntry("oneui.shortcut.contacts", "جهات الاتصال", "Contacts", OneUiSystemIconKind.CONTACTS, listOf("contacts")),
        SystemCatalogEntry("oneui.shortcut.camera", "الكاميرا", "Camera", OneUiSystemIconKind.CAMERA, listOf("camera")),
        SystemCatalogEntry("oneui.shortcut.gallery", "الاستوديو", "Gallery", OneUiSystemIconKind.GALLERY, listOf("gallery", "photos")),
        SystemCatalogEntry("oneui.shortcut.clock", "الساعة", "Clock", OneUiSystemIconKind.CLOCK, listOf("deskclock", "clock", "alarm")),
        SystemCatalogEntry("oneui.shortcut.calendar", "التقويم", "Calendar", OneUiSystemIconKind.CALENDAR, listOf("calendar")),
        SystemCatalogEntry("oneui.shortcut.settings", "الإعدادات", "Settings", OneUiSystemIconKind.SETTINGS, listOf("com.android.settings")),
        SystemCatalogEntry("oneui.shortcut.internet", "الإنترنت", "Internet", OneUiSystemIconKind.INTERNET, listOf("browser", "chrome", "webview")),
        SystemCatalogEntry("oneui.shortcut.files", "الملفات", "My Files", OneUiSystemIconKind.FILES, listOf("documentsui", "filemanager", "files")),
        SystemCatalogEntry("oneui.shortcut.notes", "Notes", "Samsung Notes", OneUiSystemIconKind.NOTES, listOf("notes", "keep")),
        SystemCatalogEntry("oneui.shortcut.reminders", "التذكيرات", "Reminders", OneUiSystemIconKind.REMINDERS, listOf("reminder", "todo", "tasks")),
        SystemCatalogEntry("oneui.shortcut.calculator", "الحاسبة", "Calculator", OneUiSystemIconKind.CALCULATOR, listOf("calculator", "calc")),
        SystemCatalogEntry("oneui.shortcut.recorder", "المسجل", "Voice Recorder", OneUiSystemIconKind.RECORDER, listOf("soundrecorder", "recorder")),
        SystemCatalogEntry("oneui.shortcut.weather", "الطقس", "Weather", OneUiSystemIconKind.WEATHER, listOf("weather")),
        SystemCatalogEntry("oneui.shortcut.health", "الصحة", "Samsung Health", OneUiSystemIconKind.HEALTH, listOf("health", "fitness")),
        SystemCatalogEntry("oneui.shortcut.galaxystore", "Galaxy Store", "Galaxy Store", OneUiSystemIconKind.GALAXY_STORE, listOf("galaxyapps", "mipicks")),
        SystemCatalogEntry("oneui.shortcut.playstore", "متجر Play", "Play Store", OneUiSystemIconKind.PLAY_STORE, listOf("vending", "playstore")),
        SystemCatalogEntry("oneui.shortcut.maps", "خرائط Google", "Google Maps", OneUiSystemIconKind.MAPS, listOf("apps.maps", "maps")),
        SystemCatalogEntry("oneui.shortcut.gmail", "Gmail", "Gmail", OneUiSystemIconKind.GMAIL, listOf("android.gm", "email", "mail"))
    )

    suspend fun loadInstalledApps(
        overrides: Map<String, AppOverrideEntity>
    ): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = runCatching {
            pm.queryIntentActivities(mainIntent, 0)
        }.getOrDefault(emptyList())

        val myPkg = context.packageName
        val discoveredApps = mutableListOf<InstalledApp>()
        val matchedKinds = mutableSetOf<OneUiSystemIconKind>()

        for (ri in resolveInfos) {
            val pkg = ri.activityInfo?.packageName ?: continue
            val act = ri.activityInfo?.name ?: continue
            if (pkg == myPkg) continue

            val rawLabel = runCatching { ri.loadLabel(pm).toString() }.getOrDefault(pkg)
            val lowerCombo = "$pkg.$act.${rawLabel.lowercase()}"

            val matchedCatalog = oneUiCoreCatalog.firstOrNull { entry ->
                entry.matchKeywords.any { kw -> lowerCombo.contains(kw) }
            }
            val kind = matchedCatalog?.kind ?: OneUiSystemIconKind.GENERIC_APP
            if (kind != OneUiSystemIconKind.GENERIC_APP) {
                matchedKinds.add(kind)
            }

            val labelAr = matchedCatalog?.labelAr ?: translateCommonLabelToAr(rawLabel)
            val bmp = getOrLoadIcon(pkg, ri.activityInfo.loadIcon(pm))
            val ov = overrides[pkg]

            discoveredApps.add(
                InstalledApp(
                    packageName = pkg,
                    activityName = act,
                    labelAr = labelAr,
                    labelEn = rawLabel,
                    iconKind = kind,
                    nativeIcon = bmp,
                    isSystemShortcut = false,
                    isHidden = ov?.isHidden ?: false,
                    customLabel = ov?.customLabel?.takeIf { it.isNotBlank() },
                    customIconUri = ov?.customIconUri?.takeIf { it.isNotBlank() },
                    launchCount = ov?.launchCount ?: 0,
                    lastLaunchedAt = ov?.lastLaunchedAt ?: 0L,
                    drawerFolder = ov?.drawerFolder?.takeIf { it.isNotBlank() }
                )
            )
        }

        // Ensure all 20 core One UI 9 system apps from the reference design are present
        for (entry in oneUiCoreCatalog) {
            if (entry.kind !in matchedKinds && discoveredApps.none { it.packageName == entry.virtualPackage }) {
                val ov = overrides[entry.virtualPackage]
                discoveredApps.add(
                    InstalledApp(
                        packageName = entry.virtualPackage,
                        activityName = "",
                        labelAr = entry.labelAr,
                        labelEn = entry.labelEn,
                        iconKind = entry.kind,
                        nativeIcon = null,
                        isSystemShortcut = true,
                        isHidden = ov?.isHidden ?: false,
                        customLabel = ov?.customLabel?.takeIf { it.isNotBlank() },
                        customIconUri = ov?.customIconUri?.takeIf { it.isNotBlank() },
                        launchCount = ov?.launchCount ?: 0,
                        lastLaunchedAt = ov?.lastLaunchedAt ?: 0L,
                        drawerFolder = ov?.drawerFolder?.takeIf { it.isNotBlank() }
                    )
                )
            }
        }

        discoveredApps
    }

    private fun translateCommonLabelToAr(label: String): String {
        return when (label.trim().lowercase()) {
            "settings" -> "الإعدادات"
            "phone", "dialer" -> "الهاتف"
            "messages", "messaging" -> "الرسائل"
            "contacts" -> "جهات الاتصال"
            "camera" -> "الكاميرا"
            "gallery", "photos" -> "الاستوديو"
            "clock" -> "الساعة"
            "calendar" -> "التقويم"
            "files", "downloads" -> "الملفات"
            "calculator" -> "الحاسبة"
            "chrome", "browser", "webview browser tester" -> "الإنترنت"
            "youtube" -> "يوتيوب"
            "whatsapp" -> "واتساب"
            "telegram" -> "تيليجرام"
            else -> label
        }
    }

    private fun getOrLoadIcon(pkg: String, drawable: Drawable?): ImageBitmap? {
        iconCache.get(pkg)?.let { return it }
        if (drawable == null) return null
        return runCatching {
            val bmp = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                val width = drawable.intrinsicWidth.coerceAtLeast(96).coerceAtMost(192)
                val height = drawable.intrinsicHeight.coerceAtLeast(96).coerceAtMost(192)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
            bmp.asImageBitmap().also { iconCache.put(pkg, it) }
        }.getOrNull()
    }

    fun launchApp(
        app: InstalledApp,
        enableAnimation: Boolean = true,
        onInternalToolRequested: (OneUiSystemIconKind) -> Unit = {}
    ): Boolean {
        val pm = context.packageManager
        val optionsBundle = if (enableAnimation) {
            runCatching {
                ActivityOptions.makeCustomAnimation(
                    context,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                ).toBundle()
            }.getOrNull()
        } else null

        if (!app.isSystemShortcut) {
            val launchIntent = if (app.activityName.isNotBlank()) {
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(app.packageName, app.activityName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
            } else {
                pm.getLaunchIntentForPackage(app.packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            if (launchIntent != null) {
                val started = runCatching {
                    context.startActivity(launchIntent, optionsBundle)
                    true
                }.getOrDefault(false)
                if (started) return true
            }
        }

        // Launch corresponding system intent for virtual/system shortcuts
        val fallbackIntent: Intent? = when (app.iconKind) {
            OneUiSystemIconKind.PHONE -> Intent(Intent.ACTION_DIAL)
            OneUiSystemIconKind.MESSAGES -> Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_MESSAGING) }
            OneUiSystemIconKind.CONTACTS -> Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
            OneUiSystemIconKind.CAMERA -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            OneUiSystemIconKind.GALLERY -> Intent(Intent.ACTION_VIEW).apply { type = "image/*" }
            OneUiSystemIconKind.CLOCK -> Intent(AlarmClock.ACTION_SHOW_ALARMS)
            OneUiSystemIconKind.CALENDAR -> Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_CALENDAR) }
            OneUiSystemIconKind.SETTINGS -> Intent(Settings.ACTION_SETTINGS)
            OneUiSystemIconKind.INTERNET -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            OneUiSystemIconKind.FILES -> Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            OneUiSystemIconKind.CALCULATOR -> Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_CALCULATOR) }
            OneUiSystemIconKind.MAPS -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:33.5138,36.2765?q=Damascus"))
            OneUiSystemIconKind.GMAIL -> Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_APP_EMAIL) }
            OneUiSystemIconKind.PLAY_STORE -> Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms"))
            else -> null
        }

        if (fallbackIntent != null) {
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val ok = runCatching {
                context.startActivity(fallbackIntent, optionsBundle)
                true
            }.getOrDefault(false)
            if (ok) return true
        }

        onInternalToolRequested(app.iconKind)
        return true
    }

    fun openAppInfo(packageName: String) {
        val targetPkg = if (packageName.startsWith("oneui.shortcut.")) {
            "com.android.settings"
        } else {
            packageName
        }
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", targetPkg, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    fun requestUninstallApp(packageName: String) {
        if (packageName.startsWith("oneui.shortcut.")) {
            Toast.makeText(context, "هذا تطبيق أساسي في النظام لا يمكن إلغاء تثبيته، يمكنك إزالته من الشاشة", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun isDefaultLauncher(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val res = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return res?.activityInfo?.packageName == context.packageName
    }

    fun openDefaultLauncherSettings() {
        val homeSettingsIntent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val started = runCatching {
            context.startActivity(homeSettingsIntent)
            true
        }.getOrDefault(false)

        if (!started) {
            val chooser = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(chooser) }
        }
    }

    @SuppressLint("WrongConstant")
    fun expandNotificationShade(): Boolean {
        return try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManager = Class.forName("android.app.StatusBarManager")
            val expandMethod = statusBarManager.getMethod("expandNotificationsPanel")
            expandMethod.invoke(statusBarService)
            true
        } catch (_: Exception) {
            Toast.makeText(context, "تم تفعيل إيماءة السحب لأسفل", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun discoverThirdPartyIconPacks(): List<Pair<String, String>> {
        val pm = context.packageManager
        val actions = listOf("org.adw.launcher.THEMES", "com.novalauncher.THEME")
        val found = mutableMapOf<String, String>()
        for (act in actions) {
            val list = runCatching { pm.queryIntentActivities(Intent(act), 0) }.getOrDefault(emptyList())
            for (ri in list) {
                val pkg = ri.activityInfo?.packageName ?: continue
                val label = runCatching { ri.loadLabel(pm).toString() }.getOrDefault(pkg)
                found[pkg] = label
            }
        }
        return found.entries.map { it.key to it.value }
    }

    fun registerPackageReceiver(onPackagesChanged: () -> Unit): BroadcastReceiver {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                iconCache.evictAll()
                onPackagesChanged()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        return receiver
    }
}
