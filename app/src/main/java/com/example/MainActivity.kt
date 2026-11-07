package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.WallpaperEngine
import com.example.model.GestureAction
import com.example.model.OneUiSystemIconKind
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.CustomizationOverlay
import com.example.ui.screens.HomePageContent
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LauncherSettingsScreen
import com.example.ui.screens.SmartSearchOverlay
import com.example.ui.screens.WallpaperCenterSheet
import com.example.ui.screens.WidgetGallerySheet
import com.example.ui.theme.ASLOneUiLauncherTheme
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

enum class ActiveLauncherOverlay {
    NONE,
    APP_DRAWER,
    WIDGET_GALLERY,
    CUSTOMIZATION,
    WALLPAPER_CENTER,
    SETTINGS,
    SMART_SEARCH
}

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureHighRefreshRateWindow()

        setContent {
            val settings by viewModel.settingsState.collectAsStateWithLifecycle()

            ASLOneUiLauncherTheme(
                themeMode = settings.themeMode,
                accentHex = settings.accentColorHex,
                fontFamilyName = settings.fontFamilyName,
                fontScale = settings.fontScale
            ) {
                LauncherRootScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkDefaultLauncherStatus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Pressing the Home button while already in the launcher returns smoothly to the Home Screen
        setIntent(intent)
        viewModel.checkDefaultLauncherStatus()
    }

    private fun configureHighRefreshRateWindow() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = display?.supportedModes
                        ?.maxByOrNull { it.refreshRate }?.modeId ?: 0
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                val modes = windowManager.defaultDisplay.supportedModes
                val best = modes.maxByOrNull { it.refreshRate }
                if (best != null) {
                    val params: WindowManager.LayoutParams = window.attributes
                    params.preferredDisplayModeId = best.modeId
                    window.attributes = params
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherRootScreen(viewModel: LauncherViewModel) {
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val homeItems by viewModel.homeItemsState.collectAsStateWithLifecycle()
    val allApps by viewModel.allAppsState.collectAsStateWithLifecycle()
    val isDefaultHome by viewModel.isDefaultHome.collectAsStateWithLifecycle()
    val systemWidgets by viewModel.systemWidgets.collectAsStateWithLifecycle()
    val thirdPartyPacks by viewModel.thirdPartyIconPacks.collectAsStateWithLifecycle()
    val contacts by viewModel.contactsState.collectAsStateWithLifecycle()

    var activeOverlay by remember { mutableStateOf(ActiveLauncherOverlay.NONE) }
    var activeBuiltInTool by remember { mutableStateOf<OneUiSystemIconKind?>(null) }
    var showLockScreenGuide by remember { mutableStateOf(false) }

    val pageCount = settings.homePageCount.coerceIn(1, 7)
    val pagerState = rememberPagerState(
        initialPage = settings.defaultHomePage.coerceIn(0, pageCount - 1),
        pageCount = { pageCount }
    )
    val scope = rememberCoroutineScope()

    val appsByPkg = remember(allApps) { allApps.associateBy { it.packageName } }

    fun handleGesture(action: GestureAction) {
        when (action) {
            GestureAction.OPEN_APP_DRAWER -> activeOverlay = ActiveLauncherOverlay.APP_DRAWER
            GestureAction.OPEN_SEARCH -> activeOverlay = ActiveLauncherOverlay.SMART_SEARCH
            GestureAction.EXPAND_NOTIFICATIONS -> {
                val expanded = viewModel.appEngine.expandNotificationShade()
                if (!expanded) activeOverlay = ActiveLauncherOverlay.SMART_SEARCH
            }
            GestureAction.OPEN_CUSTOMIZATION -> activeOverlay = ActiveLauncherOverlay.CUSTOMIZATION
            GestureAction.OPEN_SETTINGS -> activeOverlay = ActiveLauncherOverlay.SETTINGS
            GestureAction.OPEN_WIDGETS -> activeOverlay = ActiveLauncherOverlay.WIDGET_GALLERY
            GestureAction.OPEN_WALLPAPERS -> activeOverlay = ActiveLauncherOverlay.WALLPAPER_CENTER
            GestureAction.LOCK_SCREEN_GUIDE -> showLockScreenGuide = true
            GestureAction.NONE -> {}
        }
    }

    val parallaxFraction = if (settings.enableWallpaperParallax && pageCount > 1) {
        ((pagerState.currentPage + pagerState.currentPageOffsetFraction) / (pageCount - 1).toFloat()) - 0.5f
    } else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Live Procedural or Custom Gallery Wallpaper Layer
        WallpaperEngine.WallpaperBackgroundView(
            wallpaperId = settings.activeWallpaperId,
            customWallpaperUri = settings.customWallpaperUri,
            parallaxOffsetFraction = parallaxFraction,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Primary Home Screen Layer
        HomeScreen(
            pagerState = pagerState,
            settings = settings,
            homeItems = homeItems,
            allApps = allApps,
            isDefaultHome = isDefaultHome,
            widgetHost = viewModel.widgetHost,
            parseFolderApps = viewModel::parseFolderApps,
            onExecuteGesture = ::handleGesture,
            onLaunchApp = { app ->
                viewModel.recordAppLaunchAndOpen(app) { toolKind ->
                    activeBuiltInTool = toolKind
                }
            },
            onOpenSearch = { activeOverlay = ActiveLauncherOverlay.SMART_SEARCH },
            onOpenAppDrawer = { activeOverlay = ActiveLauncherOverlay.APP_DRAWER },
            onOpenWidgets = { activeOverlay = ActiveLauncherOverlay.WIDGET_GALLERY },
            onOpenCustomization = { activeOverlay = ActiveLauncherOverlay.CUSTOMIZATION },
            onOpenSettings = { activeOverlay = ActiveLauncherOverlay.SETTINGS },
            onOpenDefaultLauncherPicker = { viewModel.appEngine.openDefaultLauncherSettings() },
            onRemoveHomeItem = viewModel::removeHomeItem,
            onMoveItemToPage = viewModel::moveHomeItemToPage,
            onSwapHomeItems = viewModel::swapOrReorderHomeItems,
            onUpdateWidgetAppearance = viewModel::updateWidgetAppearance,
            onUpdateFolder = viewModel::updateHomeFolder,
            onCreateFolder = viewModel::createHomeFolder,
            onOpenAppInfo = { pkg -> viewModel.appEngine.openAppInfo(pkg) }
        )

        // 3. Animated Overlays (App Drawer, Widget Gallery, Long-Press Customization, Wallpaper Center, Settings, Smart Search)
        AnimatedVisibility(
            visible = activeOverlay == ActiveLauncherOverlay.APP_DRAWER,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AppDrawerScreen(
                allApps = allApps,
                settings = settings,
                onLaunchApp = { app ->
                    activeOverlay = ActiveLauncherOverlay.NONE
                    viewModel.recordAppLaunchAndOpen(app) { toolKind ->
                        activeBuiltInTool = toolKind
                    }
                },
                onAddAppToHome = { app ->
                    viewModel.addAppToHomeScreen(app, pagerState.currentPage)
                },
                onOpenAppInfo = { pkg -> viewModel.appEngine.openAppInfo(pkg) },
                onRequestUninstall = { pkg -> viewModel.appEngine.requestUninstallApp(pkg) },
                onToggleHideApp = viewModel::setAppHidden,
                onCustomizeSingleApp = viewModel::customizeSingleApp,
                onChangeSortMode = { mode ->
                    viewModel.updateSettings { it.copy(drawerSortMode = mode) }
                },
                onDismiss = { activeOverlay = ActiveLauncherOverlay.NONE }
            )
        }

        AnimatedVisibility(
            visible = activeOverlay == ActiveLauncherOverlay.WIDGET_GALLERY,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            WidgetGallerySheet(
                settings = settings,
                systemWidgets = systemWidgets,
                onAddBuiltInWidget = { wType ->
                    viewModel.addBuiltInWidgetToHome(wType, pagerState.currentPage)
                },
                onAddSystemWidget = { sysW ->
                    viewModel.addSystemWidgetToHome(sysW, pagerState.currentPage)
                },
                onDismiss = { activeOverlay = ActiveLauncherOverlay.NONE }
            )
        }

        AnimatedVisibility(
            visible = activeOverlay == ActiveLauncherOverlay.CUSTOMIZATION,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CustomizationOverlay(
                settings = settings,
                currentPage = pagerState.currentPage,
                onUpdateSettings = viewModel::updateSettings,
                onOpenWallpapers = { activeOverlay = ActiveLauncherOverlay.WALLPAPER_CENTER },
                onOpenWidgets = { activeOverlay = ActiveLauncherOverlay.WIDGET_GALLERY },
                onOpenSettings = { activeOverlay = ActiveLauncherOverlay.SETTINGS },
                onDismiss = { activeOverlay = ActiveLauncherOverlay.NONE },
                livePagePreviewContent = { previewIdx ->
                    HomePageContent(
                        pageIndex = previewIdx,
                        settings = settings,
                        pageItems = homeItems.filter { !it.isDock && it.page == previewIdx },
                        appsByPkg = appsByPkg,
                        widgetHost = viewModel.widgetHost,
                        parseFolderApps = viewModel::parseFolderApps,
                        onLaunchApp = {},
                        onOpenFolder = {},
                        onOpenSearch = {},
                        onOpenSettings = {},
                        onLongPressItem = {},
                        isCompactPreview = true
                    )
                }
            )
        }

        AnimatedVisibility(
            visible = activeOverlay == ActiveLauncherOverlay.WALLPAPER_CENTER,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            WallpaperCenterSheet(
                settings = settings,
                onApplyWallpaper = viewModel::applyBuiltInWallpaper,
                onSelectGalleryWallpaper = { uri ->
                    viewModel.updateSettings { it.copy(customWallpaperUri = uri) }
                },
                onToggleFavorite = viewModel::toggleFavoriteWallpaper,
                onDismiss = { activeOverlay = ActiveLauncherOverlay.NONE }
            )
        }

        AnimatedVisibility(
            visible = activeOverlay == ActiveLauncherOverlay.SETTINGS,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            LauncherSettingsScreen(
                settings = settings,
                isDefaultHome = isDefaultHome,
                thirdPartyIconPacks = thirdPartyPacks,
                onUpdateSettings = viewModel::updateSettings,
                onOpenDefaultLauncherPicker = { viewModel.appEngine.openDefaultLauncherSettings() },
                onOpenWallpapers = { activeOverlay = ActiveLauncherOverlay.WALLPAPER_CENTER },
                onOpenWidgets = { activeOverlay = ActiveLauncherOverlay.WIDGET_GALLERY },
                onResetDefaultHomeLayout = viewModel::resetHomeLayoutToDefault,
                onResetAllCustomIcons = viewModel::resetAllCustomAppIcons,
                onExportBackup = viewModel::exportFullBackupJson,
                onImportBackup = viewModel::importFullBackupJson,
                onDismiss = { activeOverlay = ActiveLauncherOverlay.NONE }
            )
        }

        AnimatedVisibility(
            visible = activeOverlay == ActiveLauncherOverlay.SMART_SEARCH,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SmartSearchOverlay(
                allApps = allApps,
                contacts = contacts,
                settings = settings,
                onLaunchApp = { app ->
                    viewModel.recordAppLaunchAndOpen(app) { toolKind ->
                        activeBuiltInTool = toolKind
                    }
                },
                onRequestLoadContacts = viewModel::loadDeviceContacts,
                onDismiss = { activeOverlay = ActiveLauncherOverlay.NONE }
            )
        }

        // Built-in One UI Quick Tool Dialog when a virtual shortcut (Notes, Weather, etc.) is opened on a minimal image
        activeBuiltInTool?.let { kind ->
            OneUiQuickToolModal(
                kind = kind,
                cityAr = settings.weatherCityAr,
                tempC = settings.weatherTempCelsius,
                onUpdateWeather = { city, temp ->
                    viewModel.updateSettings { it.copy(weatherCityAr = city, weatherTempCelsius = temp) }
                },
                onDismiss = { activeBuiltInTool = null }
            )
        }

        if (showLockScreenGuide) {
            AlertDialog(
                onDismissRequest = { showLockScreenGuide = false },
                containerColor = Color(0xFF181C3A),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("قفل الشاشة بالإيماءات") },
                text = {
                    Text("يتطلب قفل الشاشة البرمجي في أندرويد 14 و HyperOS تفعيل خدمة إمكانية الوصول (AccessibilityService) أو مسؤول الجهاز حفاظاً على أمان البصمة.")
                },
                confirmButton = {
                    Button(onClick = { showLockScreenGuide = false }) {
                        Text("حسناً")
                    }
                }
            )
        }
    }
}

@Composable
private fun OneUiQuickToolModal(
    kind: OneUiSystemIconKind,
    cityAr: String,
    tempC: Int,
    onUpdateWeather: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf("ملاحظة سريعة في One UI 9...") }
    var editCity by remember { mutableStateOf(cityAr) }
    var editTemp by remember { mutableStateOf(tempC.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF181C3A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                when (kind) {
                    OneUiSystemIconKind.WEATHER -> "طقس One UI 9 ($cityAr)"
                    OneUiSystemIconKind.NOTES -> "ملاحظات Samsung Notes السريعة"
                    OneUiSystemIconKind.REMINDERS -> "التذكيرات الذكية"
                    else -> "أداة One UI 9 المدمجة"
                }
            )
        },
        text = {
            Column {
                if (kind == OneUiSystemIconKind.WEATHER) {
                    OutlinedTextField(
                        value = editCity,
                        onValueChange = { editCity = it },
                        label = { Text("المدينة") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTemp,
                        onValueChange = { editTemp = it },
                        label = { Text("درجة الحرارة (°C)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("المحتوى") },
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (kind == OneUiSystemIconKind.WEATHER) {
                        onUpdateWeather(editCity.ifBlank { "دمشق" }, editTemp.toIntOrNull() ?: 24)
                    }
                    onDismiss()
                }
            ) {
                Text("حفظ وإغلاق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Color(0xFF94A3B8))
            }
        }
    )
}
