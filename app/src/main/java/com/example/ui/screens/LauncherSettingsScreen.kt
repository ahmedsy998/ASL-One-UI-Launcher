package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.GestureAction
import com.example.model.IconPackPreset
import com.example.model.IconShapeStyle
import com.example.model.LauncherSettings
import com.example.model.PageTransitionEffect
import com.example.model.ThemeMode
import com.example.ui.theme.OneUiAccentPresets
import kotlinx.coroutines.launch

private data class SettingsCategoryEntry(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val icon: ImageVector,
    val iconBgColor: Color
)

@Composable
fun LauncherSettingsScreen(
    settings: LauncherSettings,
    isDefaultHome: Boolean,
    thirdPartyIconPacks: List<Pair<String, String>>,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onOpenDefaultLauncherPicker: () -> Unit,
    onOpenWallpapers: () -> Unit,
    onOpenWidgets: () -> Unit,
    onResetDefaultHomeLayout: () -> Unit,
    onResetAllCustomIcons: () -> Unit,
    onExportBackup: suspend () -> String,
    onImportBackup: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var activeSectionId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var backupJsonDialogText by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    BackHandler {
        if (activeSectionId != null) {
            activeSectionId = null
        } else {
            onDismiss()
        }
    }

    val categories = remember {
        listOf(
            SettingsCategoryEntry("HOME", "الشاشة الرئيسية", "التخطيط، الشبكة، الشريط السفلي، قفل الترتيب", Icons.Default.Home, Color(0xFF3B82F6)),
            SettingsCategoryEntry("DRAWER", "درج التطبيقات", "التخطيط، الفرز، إخفاء التطبيقات، الشفافية", Icons.Default.Apps, Color(0xFF0284C7)),
            SettingsCategoryEntry("ICONS", "الأيقونات", "الشكل، الحجم، حزم الأيقونات، تسميات التطبيقات", Icons.Default.AutoAwesome, Color(0xFFEC4899)),
            SettingsCategoryEntry("THEMES", "الأنماط والألوان", "الوضع الفاتح والداكن و AMOLED، اللون المميز", Icons.Default.ColorLens, Color(0xFFF43F5E)),
            SettingsCategoryEntry("ANIMATIONS", "الحركات", "تأثيرات الانتقال وسرعة الحركة واستجابة 120Hz", Icons.Default.PlayArrow, Color(0xFF0EA5E9)),
            SettingsCategoryEntry("GESTURES", "الإيماءات", "إجراءات السحب لأعلى ولأسفل والنقر المزدوج", Icons.Default.TouchApp, Color(0xFF2563EB)),
            SettingsCategoryEntry("WIDGETS", "الويدجيت", "إدارة وإضافة الويدجيت وتخصيص مدينة الطقس", Icons.Default.Widgets, Color(0xFF06B6D4)),
            SettingsCategoryEntry("WALLPAPERS", "الخلفيات", "مكتبة خلفيات متنوعة وعالية الجودة وتأثير العمق", Icons.Default.Wallpaper, Color(0xFFD946EF)),
            SettingsCategoryEntry("TYPOGRAPHY", "الخطوط العربية", "نوع الخط العربي (Cairo / Tajawal) وحجم النصوص", Icons.Default.FontDownload, Color(0xFF8B5CF6)),
            SettingsCategoryEntry("FOLDERS", "المجلدات", "تخصيص مظهر مجلدات الشاشة الرئيسية والدرج", Icons.Default.FolderSpecial, Color(0xFFF59E0B)),
            SettingsCategoryEntry("BACKUP", "النسخ الاحتياطي والاستعادة", "حفظ واستيراد تخطيط الشاشة والإعدادات بالكامل", Icons.Default.Backup, Color(0xFF10B981)),
            SettingsCategoryEntry("PERFORMANCE", "الأداء وتوافق HyperOS", "تحسين Snapdragon 7s Gen 2 وشاشة 120Hz AMOLED", Icons.Default.Speed, Color(0xFF14B8A6)),
            SettingsCategoryEntry("ABOUT", "حول اللانشر", "ASL One UI 9 Launcher • الواجهة الأساسية للهاتف", Icons.Default.Info, Color(0xFF6366F1))
        )
    }

    val filteredCategories = remember(searchQuery, categories) {
        if (searchQuery.isBlank()) categories
        else categories.filter {
            it.titleAr.contains(searchQuery, ignoreCase = true) ||
                it.subtitleAr.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0D1C), Color(0xFF11152E), Color(0xFF090B18))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("launcher_settings_screen")
    ) {
        if (activeSectionId == null) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header matching Screen 1 of the reference image ("إعدادات اللانشر")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "إعدادات اللانشر",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "بحث في الإعدادات",
                            tint = Color.White
                        )
                    }
                }

                if (showSearchBar) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث في إعدادات اللانشر...", color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF191E3A),
                            unfocusedContainerColor = Color(0xFF191E3A)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // Set as Default Home Launcher Card at top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                if (isDefaultHome) listOf(Color(0xFF065F46), Color(0xFF047857))
                                else listOf(Color(0xFF3B28CC), Color(0xFF6D28D9))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp))
                        .clickable { onOpenDefaultLauncherPicker() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = if (isDefaultHome) "ASL One UI 9 هو اللانشر الافتراضي حالياً"
                                else "تعيين ASL One UI 9 كواجهة أساسية للهاتف",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "اضغط هنا لفتح إعدادات التطبيق الرئيسي الافتراضي في أندرويد / HyperOS",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                // Settings Categories List matching Screen 1 of the reference image
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredCategories, key = { it.id }) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF171B34))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                                .clickable {
                                    when (cat.id) {
                                        "WALLPAPERS" -> onOpenWallpapers()
                                        "WIDGETS" -> activeSectionId = "WIDGETS"
                                        else -> activeSectionId = cat.id
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("settings_cat_${cat.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Colorful One UI Squircle Icon on the right (in RTL)
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(cat.iconBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.titleAr,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = cat.titleAr,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = cat.subtitleAr,
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        } else {
            // Sub-screen for the selected settings category
            val currentCat = categories.firstOrNull { it.id == activeSectionId }
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { activeSectionId = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                    Text(
                        text = currentCat?.titleAr ?: "الإعدادات",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (activeSectionId) {
                        "HOME" -> {
                            item {
                                SettingsSliderCard(
                                    title = "عدد أعمدة الشاشة الرئيسية: ${settings.homeGridColumns}",
                                    value = settings.homeGridColumns.toFloat(),
                                    range = 3f..6f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(homeGridColumns = v.toInt()) } }
                                )
                            }
                            item {
                                SettingsSliderCard(
                                    title = "عدد صفوف الشاشة الرئيسية: ${settings.homeGridRows}",
                                    value = settings.homeGridRows.toFloat(),
                                    range = 4f..7f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(homeGridRows = v.toInt()) } }
                                )
                            }
                            item {
                                SettingsSliderCard(
                                    title = "عدد أيقونات الشريط السفلي (Dock): ${settings.dockIconCount}",
                                    value = settings.dockIconCount.toFloat(),
                                    range = 3f..5f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(dockIconCount = v.toInt()) } }
                                )
                            }
                            item {
                                SettingsToggleCard(
                                    title = "إظهار أسماء التطبيقات على الشاشة الرئيسية",
                                    checked = settings.showHomeLabels,
                                    onCheckedChange = { c -> onUpdateSettings { it.copy(showHomeLabels = c) } }
                                )
                            }
                            item {
                                SettingsToggleCard(
                                    title = "إظهار أسماء التطبيقات في الشريط السفلي",
                                    checked = settings.showDockLabels,
                                    onCheckedChange = { c -> onUpdateSettings { it.copy(showDockLabels = c) } }
                                )
                            }
                            item {
                                SettingsToggleCard(
                                    title = "قفل تخطيط الشاشة الرئيسية (منع الحذف أو النقل العرضي)",
                                    checked = settings.lockHomeLayout,
                                    onCheckedChange = { c -> onUpdateSettings { it.copy(lockHomeLayout = c) } }
                                )
                            }
                            item {
                                Button(
                                    onClick = onResetDefaultHomeLayout,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("استعادة ترتيب الشاشة الرئيسية الافتراضي (One UI 9)")
                                }
                            }
                        }

                        "DRAWER" -> {
                            item {
                                SettingsSliderCard(
                                    title = "عدد أعمدة درج التطبيقات: ${settings.drawerGridColumns}",
                                    value = settings.drawerGridColumns.toFloat(),
                                    range = 3f..6f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(drawerGridColumns = v.toInt()) } }
                                )
                            }
                            item {
                                SettingsSliderCard(
                                    title = "شفافية خلفية درج التطبيقات: ${(settings.drawerBackgroundAlpha * 100).toInt()}%",
                                    value = settings.drawerBackgroundAlpha,
                                    range = 0.45f..1.0f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(drawerBackgroundAlpha = v) } }
                                )
                            }
                            item {
                                SettingsToggleCard(
                                    title = "إظهار أسماء التطبيقات في الدرج",
                                    checked = settings.showDrawerLabels,
                                    onCheckedChange = { c -> onUpdateSettings { it.copy(showDrawerLabels = c) } }
                                )
                            }
                        }

                        "ICONS" -> {
                            item {
                                Text("شكل الأيقونات (One UI 9 Icon Shapes)", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconShapeStyle.entries.forEach { style ->
                                        SettingsChoiceRow(
                                            title = style.titleAr,
                                            selected = settings.iconShape == style,
                                            onClick = { onUpdateSettings { it.copy(iconShape = style) } }
                                        )
                                    }
                                }
                            }
                            item {
                                SettingsSliderCard(
                                    title = "حجم الأيقونات: ${settings.iconSizeDp}dp",
                                    value = settings.iconSizeDp.toFloat(),
                                    range = 44f..72f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(iconSizeDp = v.toInt()) } }
                                )
                            }
                            item {
                                SettingsSliderCard(
                                    title = "نصف قطر الحواف المخصص: ${settings.customCornerRadiusDp}dp",
                                    value = settings.customCornerRadiusDp.toFloat(),
                                    range = 4f..34f,
                                    onValueChange = { v ->
                                        onUpdateSettings {
                                            it.copy(
                                                iconShape = IconShapeStyle.CUSTOM_RADIUS,
                                                customCornerRadiusDp = v.toInt()
                                            )
                                        }
                                    }
                                )
                            }
                            item {
                                SettingsSliderCard(
                                    title = "حجم خط أسماء الأيقونات: ${settings.iconLabelSizeSp.toInt()}sp",
                                    value = settings.iconLabelSizeSp,
                                    range = 9f..16f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(iconLabelSizeSp = v) } }
                                )
                            }
                            item {
                                Text("حزم الأيقونات المدمجة والخارجية", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconPackPreset.entries.forEach { preset ->
                                        SettingsChoiceRow(
                                            title = "${preset.titleAr} — ${preset.subtitleAr}",
                                            selected = settings.iconPackPreset == preset,
                                            onClick = { onUpdateSettings { it.copy(iconPackPreset = preset) } }
                                        )
                                    }
                                    thirdPartyIconPacks.forEach { (pkg, name) ->
                                        SettingsChoiceRow(
                                            title = "حزمة خارجية: $name",
                                            selected = settings.selectedThirdPartyIconPack == pkg,
                                            onClick = { onUpdateSettings { it.copy(selectedThirdPartyIconPack = pkg) } }
                                        )
                                    }
                                }
                            }
                            item {
                                Button(
                                    onClick = onResetAllCustomIcons,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("استعادة جميع الأيقونات الأصلية للتطبيقات")
                                }
                            }
                        }

                        "THEMES" -> {
                            item {
                                Text("نمط المظهر العام", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ThemeMode.entries.forEach { mode ->
                                        SettingsChoiceRow(
                                            title = "${mode.titleAr}\n${mode.subtitleAr}",
                                            selected = settings.themeMode == mode,
                                            onClick = { onUpdateSettings { it.copy(themeMode = mode) } }
                                        )
                                    }
                                }
                            }
                            item {
                                Text("اللون المميز (Accent Color)", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items(OneUiAccentPresets) { preset ->
                                        val hex = String.format("#%06X", 0xFFFFFF and preset.primary.value.toLong().shr(32).toInt())
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color(0xFF1A1F3D))
                                                .clickable {
                                                    val colorHex = when (preset.id) {
                                                        "galaxy_blue" -> "#2563EB"
                                                        "emerald_neon" -> "#10B981"
                                                        "rose_magenta" -> "#EC4899"
                                                        "amber_gold" -> "#F59E0B"
                                                        "cyan_crystal" -> "#06B6D4"
                                                        else -> "#6366F1"
                                                    }
                                                    onUpdateSettings { it.copy(accentColorHex = colorHex) }
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(listOf(preset.primary, preset.secondary)))
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(preset.nameAr, color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }

                        "ANIMATIONS" -> {
                            item {
                                Text("تأثير الانتقال بين الصفحات", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PageTransitionEffect.entries.forEach { effect ->
                                        SettingsChoiceRow(
                                            title = "${effect.titleAr} — ${effect.descriptionAr}",
                                            selected = settings.pageTransitionEffect == effect,
                                            onClick = { onUpdateSettings { it.copy(pageTransitionEffect = effect) } }
                                        )
                                    }
                                }
                            }
                            item {
                                SettingsSliderCard(
                                    title = "سرعة الحركة والانتقالات: ${String.format("%.1fx", settings.animationSpeedMultiplier)}",
                                    value = settings.animationSpeedMultiplier,
                                    range = 0.5f..2.0f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(animationSpeedMultiplier = v) } }
                                )
                            }
                            item {
                                SettingsToggleCard(
                                    title = "تأثير حركة فتح وإغلاق التطبيقات",
                                    checked = settings.enableAppLaunchAnimation,
                                    onCheckedChange = { c -> onUpdateSettings { it.copy(enableAppLaunchAnimation = c) } }
                                )
                            }
                            item {
                                SettingsToggleCard(
                                    title = "تأثير تحرك الخلفية التفاعلي (Parallax)",
                                    checked = settings.enableWallpaperParallax,
                                    onCheckedChange = { c -> onUpdateSettings { it.copy(enableWallpaperParallax = c) } }
                                )
                            }
                        }

                        "GESTURES" -> {
                            item {
                                GesturePickerCard(
                                    label = "السحب لأعلى على الشاشة الرئيسية",
                                    current = settings.swipeUpGesture,
                                    onSelect = { g -> onUpdateSettings { it.copy(swipeUpGesture = g) } }
                                )
                            }
                            item {
                                GesturePickerCard(
                                    label = "السحب لأسفل على الشاشة الرئيسية",
                                    current = settings.swipeDownGesture,
                                    onSelect = { g -> onUpdateSettings { it.copy(swipeDownGesture = g) } }
                                )
                            }
                            item {
                                GesturePickerCard(
                                    label = "النقر المزدوج (Double Tap)",
                                    current = settings.doubleTapGesture,
                                    onSelect = { g -> onUpdateSettings { it.copy(doubleTapGesture = g) } }
                                )
                            }
                            item {
                                GesturePickerCard(
                                    label = "الضغط المطوّل على مساحة فارغة",
                                    current = settings.longPressGesture,
                                    onSelect = { g -> onUpdateSettings { it.copy(longPressGesture = g) } }
                                )
                            }
                        }

                        "WIDGETS" -> {
                            item {
                                Button(
                                    onClick = onOpenWidgets,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Widgets, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("فتح معرض الويدجيت لإضافة ويدجيت جديدة")
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = settings.weatherCityAr,
                                    onValueChange = { city -> onUpdateSettings { it.copy(weatherCityAr = city) } },
                                    label = { Text("مدينة ويدجيت الطقس (مثال: دمشق، الرياض، القاهرة، بغداد)") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                SettingsSliderCard(
                                    title = "درجة الحرارة الحالية للعرض: °${settings.weatherTempCelsius}",
                                    value = settings.weatherTempCelsius.toFloat(),
                                    range = -5f..48f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(weatherTempCelsius = v.toInt()) } }
                                )
                            }
                        }

                        "TYPOGRAPHY" -> {
                            item {
                                Text("الخط العربي الأساسي للواجهة", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        "Cairo" to "خط القاهرة (Cairo) — عصري وواضح جداً",
                                        "Tajawal" to "خط تجوّل (Tajawal) — انسيابي وأنيق",
                                        "System" to "خط النظام الافتراضي"
                                    ).forEach { (fontId, desc) ->
                                        SettingsChoiceRow(
                                            title = desc,
                                            selected = settings.fontFamilyName == fontId,
                                            onClick = { onUpdateSettings { it.copy(fontFamilyName = fontId) } }
                                        )
                                    }
                                }
                            }
                            item {
                                SettingsSliderCard(
                                    title = "مقياس حجم الخط العام: ${(settings.fontScale * 100).toInt()}%",
                                    value = settings.fontScale,
                                    range = 0.85f..1.25f,
                                    onValueChange = { v -> onUpdateSettings { it.copy(fontScale = v) } }
                                )
                            }
                        }

                        "FOLDERS" -> {
                            item {
                                Text(
                                    text = "مجلدات One UI 9 الذكية تدعم الفتح السلس وتغيير لون المجلد وإضافة التطبيقات مباشرة من الشاشة الرئيسية أو درج التطبيقات.",
                                    color = Color(0xFFCBD5E1),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        "BACKUP" -> {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(Color(0xFF181C38))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "النسخ الاحتياطي الكامل والاستعادة (JSON)",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "يتم حفظ جميع إعداداتك وتخطيط الشاشة الرئيسية والمجلدات والويدجيت تلقائياً في قاعدة بيانات الجهاز، ويمكنك أيضاً تصدير نسخة احتياطية أو استعادتها في أي وقت.",
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                backupJsonDialogText = onExportBackup()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("تصدير النسخة الاحتياطية الحالية")
                                    }
                                    Button(
                                        onClick = { showImportDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("استيراد واستعادة نسخة احتياطية")
                                    }
                                }
                            }
                        }

                        "PERFORMANCE" -> {
                            item {
                                SettingsToggleCard(
                                    title = "تفعيل معدل التحديث الفائق 120Hz لشاشة AMOLED",
                                    checked = settings.highRefreshRate120Hz,
                                    onCheckedChange = { c -> onUpdateSettings { it.copy(highRefreshRate120Hz = c) } }
                                )
                            }
                            item {
                                SettingsToggleCard(
                                    title = "تحسين الذاكرة لمعالج Snapdragon 7s Gen 2 ونظام HyperOS",
                                    checked = settings.hyperOsMemoryOptimization,
                                    onCheckedChange = { c -> onUpdateSettings { it.copy(hyperOsMemoryOptimization = c) } }
                                )
                            }
                        }

                        "ABOUT" -> {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF1E1B4B), Color(0xFF312E81))
                                            )
                                        )
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "ASL One UI Launcher 9.0",
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "واجهة أندرويد رئيسية متكاملة مستوحاة من Samsung One UI 9 ومُحسّنة خصيصاً لأجهزة Xiaomi Redmi Note 13 Pro 5G (Snapdragon 7s Gen 2 / HyperOS / 120Hz AMOLED).",
                                        color = Color(0xFFE2E8F0),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Export Backup Dialog
        backupJsonDialogText?.let { json ->
            AlertDialog(
                onDismissRequest = { backupJsonDialogText = null },
                containerColor = Color(0xFF191E3C),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("تم إنشاء النسخة الاحتياطية بنجاح") },
                text = {
                    OutlinedTextField(
                        value = json,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                },
                confirmButton = {
                    Button(onClick = { backupJsonDialogText = null }) {
                        Text("تم")
                    }
                }
            )
        }

        // Import Backup Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                containerColor = Color(0xFF191E3C),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("استعادة نسخة احتياطية") },
                text = {
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text("الصق بيانات JSON الخاصة بالنسخة الاحتياطية هنا...") },
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onImportBackup(importText)
                            showImportDialog = false
                        }
                    ) {
                        Text("استعادة الآن")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("إلغاء", color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSliderCard(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF181C38))
            .padding(16.dp)
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF181C38))
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsChoiceRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color(0xFF312E81) else Color(0xFF181C38))
            .border(
                1.dp,
                if (selected) Color(0xFF818CF8) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF38BDF8))
        }
    }
}

@Composable
private fun GesturePickerCard(
    label: String,
    current: GestureAction,
    onSelect: (GestureAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF181C38))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(GestureAction.entries) { action ->
                val active = current == action
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (active) Color(0xFF2563EB) else Color(0xFF232852))
                        .clickable { onSelect(action) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(action.titleAr, color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
