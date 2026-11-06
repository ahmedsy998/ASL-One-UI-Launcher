package com.example.viewmodel

import android.app.Application
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppOverrideEntity
import com.example.data.db.HomeItemEntity
import com.example.data.db.LauncherDatabase
import com.example.data.prefs.LauncherPreferencesRepository
import com.example.engine.AppManagerEngine
import com.example.engine.WallpaperEngine
import com.example.model.BuiltInWidgetType
import com.example.model.FolderAppEntry
import com.example.model.HomeItemType
import com.example.model.InstalledApp
import com.example.model.LauncherSettings
import com.example.model.OneUiSystemIconKind
import com.example.widget.LauncherWidgetHost
import com.example.widget.SystemWidgetProviderItem
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class LauncherFullBackupPayload(
    val version: Int = 9,
    val timestamp: Long = System.currentTimeMillis(),
    val settings: LauncherSettings,
    val homeItems: List<HomeItemEntity>,
    val appOverrides: List<AppOverrideEntity>
)

data class ContactSearchResult(
    val id: String,
    val name: String,
    val phoneNumber: String
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val db = LauncherDatabase.getInstance(appContext)
    private val dao = db.launcherDao()
    private val prefsRepo = LauncherPreferencesRepository(appContext)
    val appEngine = AppManagerEngine(appContext)
    val widgetHost = LauncherWidgetHost(appContext)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val folderListType = Types.newParameterizedType(List::class.java, FolderAppEntry::class.java)
    private val folderAdapter = moshi.adapter<List<FolderAppEntry>>(folderListType)
    private val backupAdapter = moshi.adapter(LauncherFullBackupPayload::class.java)

    val settingsState: StateFlow<LauncherSettings> = prefsRepo.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherSettings()
    )

    val homeItemsState: StateFlow<List<HomeItemEntity>> = dao.observeAllHomeItems().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val rawInstalledApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val overridesFlow = dao.observeAppOverrides()

    val allAppsState: StateFlow<List<InstalledApp>> = combine(
        rawInstalledApps,
        overridesFlow
    ) { apps, overrides ->
        val map = overrides.associateBy { it.packageName }
        apps.map { app ->
            val ov = map[app.packageName]
            app.copy(
                isHidden = ov?.isHidden ?: false,
                customLabel = ov?.customLabel?.takeIf { it.isNotBlank() },
                customIconUri = ov?.customIconUri?.takeIf { it.isNotBlank() },
                launchCount = ov?.launchCount ?: 0,
                lastLaunchedAt = ov?.lastLaunchedAt ?: 0L,
                drawerFolder = ov?.drawerFolder?.takeIf { it.isNotBlank() }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isDefaultHome = MutableStateFlow(true)
    val isDefaultHome: StateFlow<Boolean> = _isDefaultHome.asStateFlow()

    private val _systemWidgets = MutableStateFlow<List<SystemWidgetProviderItem>>(emptyList())
    val systemWidgets: StateFlow<List<SystemWidgetProviderItem>> = _systemWidgets.asStateFlow()

    private val _thirdPartyIconPacks = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val thirdPartyIconPacks: StateFlow<List<Pair<String, String>>> = _thirdPartyIconPacks.asStateFlow()

    private val _contactsState = MutableStateFlow<List<ContactSearchResult>>(emptyList())
    val contactsState: StateFlow<List<ContactSearchResult>> = _contactsState.asStateFlow()

    private var packageReceiver: BroadcastReceiver? = null

    init {
        widgetHost.startListening()
        refreshAppsAndWidgets()
        ensureDefaultOneUi9HomeLayout()
        packageReceiver = appEngine.registerPackageReceiver {
            refreshAppsAndWidgets()
        }
    }

    override fun onCleared() {
        super.onCleared()
        widgetHost.stopListening()
        packageReceiver?.let {
            runCatching { appContext.unregisterReceiver(it) }
        }
    }

    fun checkDefaultLauncherStatus() {
        _isDefaultHome.value = appEngine.isDefaultLauncher()
    }

    fun refreshAppsAndWidgets() {
        viewModelScope.launch {
            checkDefaultLauncherStatus()
            val overrides = dao.getAllAppOverridesOnce().associateBy { it.packageName }
            rawInstalledApps.value = appEngine.loadInstalledApps(overrides)
            _systemWidgets.value = withContext(Dispatchers.IO) {
                widgetHost.loadInstalledSystemWidgets()
            }
            _thirdPartyIconPacks.value = withContext(Dispatchers.IO) {
                appEngine.discoverThirdPartyIconPacks()
            }
        }
    }

    /**
     * Populates the exact One UI 9 default Home Screen arrangement shown in the reference image
     * on first launch if the database is empty.
     */
    private fun ensureDefaultOneUi9HomeLayout() {
        viewModelScope.launch {
            val existing = dao.getAllHomeItemsOnce()
            if (existing.isNotEmpty()) return@launch

            val googleFolderApps = listOf(
                FolderAppEntry("oneui.shortcut.gmail", "Gmail", OneUiSystemIconKind.GMAIL.name),
                FolderAppEntry("oneui.shortcut.maps", "خرائط Google", OneUiSystemIconKind.MAPS.name),
                FolderAppEntry("oneui.shortcut.playstore", "متجر Play", OneUiSystemIconKind.PLAY_STORE.name),
                FolderAppEntry("oneui.shortcut.internet", "الإنترنت", OneUiSystemIconKind.INTERNET.name)
            )
            val googleFolderJson = folderAdapter.toJson(googleFolderApps)

            val initialItems = listOf(
                // Page 0: Clock & Weather Dual Hero Widget
                HomeItemEntity(
                    page = 0, row = 0, col = 0, spanX = 4, spanY = 2, isDock = false,
                    itemType = HomeItemType.BUILTIN_WIDGET.name,
                    builtInWidgetType = BuiltInWidgetType.CLOCK_WEATHER_DUAL.name,
                    title = "الساعة والطقس"
                ),
                // Page 0: Google Search Bar Pill Widget
                HomeItemEntity(
                    page = 0, row = 2, col = 0, spanX = 4, spanY = 1, isDock = false,
                    itemType = HomeItemType.BUILTIN_WIDGET.name,
                    builtInWidgetType = BuiltInWidgetType.GOOGLE_SEARCH_BAR.name,
                    title = "بحث Google"
                ),
                // Page 0 Row 3: Gallery, Internet, Files, Galaxy Store (matches reference image row 1)
                HomeItemEntity(
                    page = 0, row = 3, col = 0, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.gallery",
                    title = "الاستوديو",
                    iconKindName = OneUiSystemIconKind.GALLERY.name
                ),
                HomeItemEntity(
                    page = 0, row = 3, col = 1, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.internet",
                    title = "الإنترنت",
                    iconKindName = OneUiSystemIconKind.INTERNET.name
                ),
                HomeItemEntity(
                    page = 0, row = 3, col = 2, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.files",
                    title = "الملفات",
                    iconKindName = OneUiSystemIconKind.FILES.name
                ),
                HomeItemEntity(
                    page = 0, row = 3, col = 3, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.galaxystore",
                    title = "Galaxy Store",
                    iconKindName = OneUiSystemIconKind.GALAXY_STORE.name
                ),
                // Page 0 Row 4: Google Folder, Play Store, Calendar, Settings (matches reference image row 2)
                HomeItemEntity(
                    page = 0, row = 4, col = 0, isDock = false,
                    itemType = HomeItemType.FOLDER.name,
                    title = "Google",
                    folderAppsJson = googleFolderJson,
                    folderColorHex = "#334155"
                ),
                HomeItemEntity(
                    page = 0, row = 4, col = 1, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.playstore",
                    title = "متجر Play",
                    iconKindName = OneUiSystemIconKind.PLAY_STORE.name
                ),
                HomeItemEntity(
                    page = 0, row = 4, col = 2, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.calendar",
                    title = "التقويم",
                    iconKindName = OneUiSystemIconKind.CALENDAR.name
                ),
                HomeItemEntity(
                    page = 0, row = 4, col = 3, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.settings",
                    title = "الإعدادات",
                    iconKindName = OneUiSystemIconKind.SETTINGS.name
                ),
                // Page 1: Interactive Widget Dashboard Page (Analog Clock, Calendar Month, Battery Ring, Music Wave)
                HomeItemEntity(
                    page = 1, row = 0, col = 0, spanX = 2, spanY = 2, isDock = false,
                    itemType = HomeItemType.BUILTIN_WIDGET.name,
                    builtInWidgetType = BuiltInWidgetType.ANALOG_CLOCK.name,
                    title = "الساعة التناظرية"
                ),
                HomeItemEntity(
                    page = 1, row = 0, col = 2, spanX = 2, spanY = 2, isDock = false,
                    itemType = HomeItemType.BUILTIN_WIDGET.name,
                    builtInWidgetType = BuiltInWidgetType.CALENDAR_MONTH.name,
                    title = "التقويم الشهري"
                ),
                HomeItemEntity(
                    page = 1, row = 2, col = 0, spanX = 2, spanY = 2, isDock = false,
                    itemType = HomeItemType.BUILTIN_WIDGET.name,
                    builtInWidgetType = BuiltInWidgetType.BATTERY_RING.name,
                    title = "البطارية"
                ),
                HomeItemEntity(
                    page = 1, row = 2, col = 2, spanX = 2, spanY = 2, isDock = false,
                    itemType = HomeItemType.BUILTIN_WIDGET.name,
                    builtInWidgetType = BuiltInWidgetType.MUSIC_WAVE.name,
                    title = "الموسيقى"
                ),
                // Page 2: Productivity Apps & World Clock
                HomeItemEntity(
                    page = 2, row = 0, col = 0, spanX = 4, spanY = 2, isDock = false,
                    itemType = HomeItemType.BUILTIN_WIDGET.name,
                    builtInWidgetType = BuiltInWidgetType.WORLD_CLOCK.name,
                    title = "الساعة العالمية"
                ),
                HomeItemEntity(
                    page = 2, row = 2, col = 0, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.notes",
                    title = "Notes",
                    iconKindName = OneUiSystemIconKind.NOTES.name
                ),
                HomeItemEntity(
                    page = 2, row = 2, col = 1, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.reminders",
                    title = "التذكيرات",
                    iconKindName = OneUiSystemIconKind.REMINDERS.name
                ),
                HomeItemEntity(
                    page = 2, row = 2, col = 2, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.calculator",
                    title = "الحاسبة",
                    iconKindName = OneUiSystemIconKind.CALCULATOR.name
                ),
                HomeItemEntity(
                    page = 2, row = 2, col = 3, isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.health",
                    title = "الصحة",
                    iconKindName = OneUiSystemIconKind.HEALTH.name
                ),
                // Bottom Dock: Phone, Contacts, Messages, Camera (matches reference image bottom dock)
                HomeItemEntity(
                    page = 0, row = 0, col = 0, isDock = true,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.phone",
                    title = "الهاتف",
                    iconKindName = OneUiSystemIconKind.PHONE.name
                ),
                HomeItemEntity(
                    page = 0, row = 0, col = 1, isDock = true,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.contacts",
                    title = "جهات الاتصال",
                    iconKindName = OneUiSystemIconKind.CONTACTS.name
                ),
                HomeItemEntity(
                    page = 0, row = 0, col = 2, isDock = true,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.messages",
                    title = "الرسائل",
                    iconKindName = OneUiSystemIconKind.MESSAGES.name
                ),
                HomeItemEntity(
                    page = 0, row = 0, col = 3, isDock = true,
                    itemType = HomeItemType.APP.name,
                    packageName = "oneui.shortcut.camera",
                    title = "الكاميرا",
                    iconKindName = OneUiSystemIconKind.CAMERA.name
                )
            )
            dao.insertHomeItems(initialItems)
        }
    }

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch {
            val updated = transform(settingsState.value)
            prefsRepo.saveSettings(updated)
        }
    }

    fun parseFolderApps(json: String): List<FolderAppEntry> {
        return runCatching { folderAdapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }

    fun addAppToHomeScreen(app: InstalledApp, targetPage: Int = 0) {
        viewModelScope.launch {
            if (settingsState.value.lockHomeLayout) {
                Toast.makeText(appContext, "تخطيط الشاشة الرئيسية مقفل حالياً", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val itemsOnPage = homeItemsState.value.filter { !it.isDock && it.page == targetPage }
            val nextRow = ((itemsOnPage.maxOfOrNull { it.row } ?: 2) + 1).coerceAtMost(settingsState.value.homeGridRows - 1)
            val nextCol = ((itemsOnPage.filter { it.row == nextRow }.maxOfOrNull { it.col } ?: -1) + 1) % settingsState.value.homeGridColumns

            dao.insertHomeItem(
                HomeItemEntity(
                    page = targetPage,
                    row = nextRow,
                    col = nextCol,
                    isDock = false,
                    itemType = HomeItemType.APP.name,
                    packageName = app.packageName,
                    title = app.displayLabel,
                    iconKindName = app.iconKind.name,
                    customIconUri = app.customIconUri ?: ""
                )
            )
            Toast.makeText(appContext, "تمت إضافة ${app.displayLabel} إلى الشاشة الرئيسية", Toast.LENGTH_SHORT).show()
        }
    }

    fun addBuiltInWidgetToHome(type: BuiltInWidgetType, targetPage: Int) {
        viewModelScope.launch {
            dao.insertHomeItem(
                HomeItemEntity(
                    page = targetPage,
                    row = 0,
                    col = 0,
                    spanX = type.defaultSpanX,
                    spanY = type.defaultSpanY,
                    isDock = false,
                    itemType = HomeItemType.BUILTIN_WIDGET.name,
                    builtInWidgetType = type.name,
                    title = type.titleAr
                )
            )
            Toast.makeText(appContext, "تمت إضافة ويدجيت ${type.titleAr}", Toast.LENGTH_SHORT).show()
        }
    }

    fun addSystemWidgetToHome(providerItem: SystemWidgetProviderItem, targetPage: Int) {
        viewModelScope.launch {
            val widgetId = widgetHost.allocateWidgetId()
            val bound = runCatching {
                widgetHost.appWidgetManager.bindAppWidgetIdIfAllowed(
                    widgetId,
                    providerItem.providerInfo.provider
                )
            }.getOrDefault(false)

            dao.insertHomeItem(
                HomeItemEntity(
                    page = targetPage,
                    row = 1,
                    col = 0,
                    spanX = providerItem.minCellsX,
                    spanY = providerItem.minCellsY,
                    isDock = false,
                    itemType = HomeItemType.SYSTEM_WIDGET.name,
                    systemAppWidgetId = widgetId,
                    title = providerItem.widgetLabel
                )
            )
            val msg = if (bound) {
                "تم ربط وإضافة ويدجيت ${providerItem.widgetLabel}"
            } else {
                "تمت إضافة حاوية ويدجيت ${providerItem.widgetLabel} إلى الشاشة"
            }
            Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun createHomeFolder(titleAr: String, apps: List<InstalledApp>, page: Int, colorHex: String = "#4F46E5") {
        viewModelScope.launch {
            val entries = apps.map {
                FolderAppEntry(it.packageName, it.displayLabel, it.iconKind.name)
            }
            dao.insertHomeItem(
                HomeItemEntity(
                    page = page,
                    row = 3,
                    col = 0,
                    isDock = false,
                    itemType = HomeItemType.FOLDER.name,
                    title = titleAr,
                    folderAppsJson = folderAdapter.toJson(entries),
                    folderColorHex = colorHex
                )
            )
            Toast.makeText(appContext, "تم إنشاء مجلد «$titleAr»", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateHomeFolder(item: HomeItemEntity, newTitle: String, newEntries: List<FolderAppEntry>, newColorHex: String) {
        viewModelScope.launch {
            dao.updateHomeItem(
                item.copy(
                    title = newTitle,
                    folderAppsJson = folderAdapter.toJson(newEntries),
                    folderColorHex = newColorHex
                )
            )
        }
    }

    fun moveHomeItemToPage(item: HomeItemEntity, newPage: Int) {
        viewModelScope.launch {
            dao.updateHomeItem(item.copy(page = newPage.coerceAtLeast(0)))
        }
    }

    fun swapOrReorderHomeItems(first: HomeItemEntity, second: HomeItemEntity) {
        viewModelScope.launch {
            dao.updateHomeItem(first.copy(row = second.row, col = second.col, page = second.page))
            dao.updateHomeItem(second.copy(row = first.row, col = first.col, page = first.page))
        }
    }

    fun updateWidgetAppearance(item: HomeItemEntity, spanX: Int, spanY: Int, bgColorHex: String, alpha: Float) {
        viewModelScope.launch {
            dao.updateHomeItem(
                item.copy(
                    spanX = spanX.coerceIn(1, 4),
                    spanY = spanY.coerceIn(1, 4),
                    widgetBgColorHex = bgColorHex,
                    widgetAlpha = alpha.coerceIn(0.2f, 1.0f)
                )
            )
        }
    }

    fun removeHomeItem(item: HomeItemEntity) {
        viewModelScope.launch {
            if (settingsState.value.lockHomeLayout) {
                Toast.makeText(appContext, "تخطيط الشاشة مقفل! قم بإلغاء القفل من الإعدادات أولاً", Toast.LENGTH_SHORT).show()
                return@launch
            }
            if (item.systemAppWidgetId != -1) {
                widgetHost.deleteWidgetId(item.systemAppWidgetId)
            }
            dao.deleteHomeItemById(item.id)
        }
    }

    fun recordAppLaunchAndOpen(
        app: InstalledApp,
        onInternalToolRequested: (OneUiSystemIconKind) -> Unit
    ) {
        viewModelScope.launch {
            val current = dao.getAppOverride(app.packageName) ?: AppOverrideEntity(packageName = app.packageName)
            dao.upsertAppOverride(
                current.copy(
                    launchCount = current.launchCount + 1,
                    lastLaunchedAt = System.currentTimeMillis()
                )
            )
        }
        appEngine.launchApp(
            app = app,
            enableAnimation = settingsState.value.enableAppLaunchAnimation,
            onInternalToolRequested = onInternalToolRequested
        )
    }

    fun setAppHidden(packageName: String, hidden: Boolean) {
        viewModelScope.launch {
            val current = dao.getAppOverride(packageName) ?: AppOverrideEntity(packageName = packageName)
            dao.upsertAppOverride(current.copy(isHidden = hidden))
        }
    }

    fun customizeSingleApp(packageName: String, customLabel: String, customIconUri: String, drawerFolder: String = "") {
        viewModelScope.launch {
            val current = dao.getAppOverride(packageName) ?: AppOverrideEntity(packageName = packageName)
            dao.upsertAppOverride(
                current.copy(
                    customLabel = customLabel.trim(),
                    customIconUri = customIconUri.trim(),
                    drawerFolder = drawerFolder.trim()
                )
            )
            // Also update matching items on home screen
            val allHome = dao.getAllHomeItemsOnce().filter { it.packageName == packageName }
            for (h in allHome) {
                dao.updateHomeItem(
                    h.copy(
                        title = customLabel.trim().ifBlank { h.title },
                        customIconUri = customIconUri.trim()
                    )
                )
            }
            Toast.makeText(appContext, "تم تحديث تخصيص الأيقونة بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetAllCustomAppIcons() {
        viewModelScope.launch {
            val all = dao.getAllAppOverridesOnce()
            for (ov in all) {
                dao.upsertAppOverride(ov.copy(customIconUri = "", customLabel = ""))
            }
            Toast.makeText(appContext, "تمت استعادة جميع الأيقونات الأصلية للتطبيقات", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetHomeLayoutToDefault() {
        viewModelScope.launch {
            dao.clearAllHomeItems()
            ensureDefaultOneUi9HomeLayout()
            Toast.makeText(appContext, "تمت استعادة تخطيط One UI 9 الافتراضي", Toast.LENGTH_SHORT).show()
        }
    }

    fun applyBuiltInWallpaper(wallpaperId: String, applyToSystem: Boolean, targetFlag: Int = WallpaperManager.FLAG_SYSTEM) {
        viewModelScope.launch {
            updateSettings {
                it.copy(activeWallpaperId = wallpaperId, customWallpaperUri = "")
            }
            if (applyToSystem) {
                val ok = WallpaperEngine.applySystemWallpaper(appContext, wallpaperId, targetFlag)
                val msg = if (ok) "تم تعيين الخلفية على النظام واللانشر بنجاح" else "تم تعيين الخلفية داخل اللانشر بنجاح"
                Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(appContext, "تم تطبيق الخلفية بنجاح", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleFavoriteWallpaper(wallpaperId: String) {
        updateSettings { current ->
            val next = current.favoriteWallpapers.toMutableSet()
            if (!next.add(wallpaperId)) next.remove(wallpaperId)
            current.copy(favoriteWallpapers = next)
        }
    }

    fun loadDeviceContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val results = mutableListOf<ContactSearchResult>()
            runCatching {
                val cursor: Cursor? = appContext.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                )
                cursor?.use { c ->
                    val idIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
                    val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (c.moveToNext() && results.size < 100) {
                        val id = if (idIdx >= 0) c.getString(idIdx) ?: "" else ""
                        val name = if (nameIdx >= 0) c.getString(nameIdx) ?: "" else ""
                        val num = if (numIdx >= 0) c.getString(numIdx) ?: "" else ""
                        if (name.isNotBlank()) {
                            results.add(ContactSearchResult(id, name, num))
                        }
                    }
                }
            }
            _contactsState.value = results
        }
    }

    suspend fun exportFullBackupJson(): String = withContext(Dispatchers.IO) {
        val payload = LauncherFullBackupPayload(
            settings = settingsState.value,
            homeItems = dao.getAllHomeItemsOnce(),
            appOverrides = dao.getAllAppOverridesOnce()
        )
        backupAdapter.toJson(payload)
    }

    fun importFullBackupJson(jsonString: String) {
        viewModelScope.launch {
            val parsed = withContext(Dispatchers.IO) {
                runCatching { backupAdapter.fromJson(jsonString) }.getOrNull()
            }
            if (parsed == null) {
                Toast.makeText(appContext, "ملف النسخة الاحتياطية غير صالح", Toast.LENGTH_SHORT).show()
                return@launch
            }
            prefsRepo.saveSettings(parsed.settings)
            dao.clearAllHomeItems()
            dao.insertHomeItems(parsed.homeItems)
            dao.clearAllAppOverrides()
            dao.upsertAppOverrides(parsed.appOverrides)
            Toast.makeText(appContext, "تمت استعادة النسخة الاحتياطية وتخطيط الشاشة بالكامل!", Toast.LENGTH_LONG).show()
        }
    }
}
