package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.GestureAction
import com.example.model.IconPackPreset
import com.example.model.IconShapeStyle
import com.example.model.LauncherSettings
import com.example.model.PageTransitionEffect
import com.example.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "asl_one_ui_9_preferences"
)

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val HOME_COLS = intPreferencesKey("home_cols")
        val HOME_ROWS = intPreferencesKey("home_rows")
        val DOCK_COUNT = intPreferencesKey("dock_count")
        val HOME_PAGE_COUNT = intPreferencesKey("home_page_count")
        val DEFAULT_HOME_PAGE = intPreferencesKey("default_home_page")

        val ICON_SIZE_DP = intPreferencesKey("icon_size_dp")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val CUSTOM_CORNER_RADIUS = intPreferencesKey("custom_corner_radius")
        val ICON_PACK_PRESET = stringPreferencesKey("icon_pack_preset")
        val THIRD_PARTY_PACK = stringPreferencesKey("third_party_pack")
        val LABEL_SIZE_SP = floatPreferencesKey("label_size_sp")
        val LABEL_COLOR_HEX = stringPreferencesKey("label_color_hex")
        val SHOW_HOME_LABELS = booleanPreferencesKey("show_home_labels")
        val SHOW_DOCK_LABELS = booleanPreferencesKey("show_dock_labels")
        val SHOW_DRAWER_LABELS = booleanPreferencesKey("show_drawer_labels")
        val ICON_SPACING_DP = intPreferencesKey("icon_spacing_dp")
        val SHOW_ICON_BG_GLOW = booleanPreferencesKey("show_icon_bg_glow")

        val DRAWER_COLS = intPreferencesKey("drawer_cols")
        val DRAWER_SORT = stringPreferencesKey("drawer_sort")
        val DRAWER_BG_ALPHA = floatPreferencesKey("drawer_bg_alpha")

        val ANIM_SPEED = floatPreferencesKey("anim_speed")
        val SPRING_DAMPING = floatPreferencesKey("spring_damping")
        val PAGE_TRANSITION = stringPreferencesKey("page_transition")
        val ENABLE_APP_ANIM = booleanPreferencesKey("enable_app_anim")
        val ENABLE_PARALLAX = booleanPreferencesKey("enable_parallax")

        val SWIPE_UP = stringPreferencesKey("swipe_up")
        val SWIPE_DOWN = stringPreferencesKey("swipe_down")
        val DOUBLE_TAP = stringPreferencesKey("double_tap")
        val TWO_FINGER = stringPreferencesKey("two_finger")
        val LONG_PRESS = stringPreferencesKey("long_press")

        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_HEX = stringPreferencesKey("accent_hex")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val FONT_SCALE = floatPreferencesKey("font_scale")

        val WALLPAPER_ID = stringPreferencesKey("wallpaper_id")
        val CUSTOM_WALLPAPER_URI = stringPreferencesKey("custom_wallpaper_uri")
        val FAVORITE_WALLPAPERS = stringSetPreferencesKey("favorite_wallpapers")

        val WEATHER_CITY = stringPreferencesKey("weather_city")
        val WEATHER_TEMP = intPreferencesKey("weather_temp")
        val WEATHER_COND = stringPreferencesKey("weather_cond")
        val HIGH_REFRESH_120 = booleanPreferencesKey("high_refresh_120")
        val HYPER_OS_OPT = booleanPreferencesKey("hyper_os_opt")
        val LOCK_HOME_LAYOUT = booleanPreferencesKey("lock_home_layout")
    }

    val settingsFlow: Flow<LauncherSettings> = context.launcherDataStore.data.map { prefs ->
        val defaults = LauncherSettings()
        LauncherSettings(
            homeGridColumns = prefs[Keys.HOME_COLS] ?: defaults.homeGridColumns,
            homeGridRows = prefs[Keys.HOME_ROWS] ?: defaults.homeGridRows,
            dockIconCount = prefs[Keys.DOCK_COUNT] ?: defaults.dockIconCount,
            homePageCount = prefs[Keys.HOME_PAGE_COUNT] ?: defaults.homePageCount,
            defaultHomePage = prefs[Keys.DEFAULT_HOME_PAGE] ?: defaults.defaultHomePage,
            iconSizeDp = prefs[Keys.ICON_SIZE_DP] ?: defaults.iconSizeDp,
            iconShape = prefs[Keys.ICON_SHAPE]?.let {
                runCatching { IconShapeStyle.valueOf(it) }.getOrNull()
            } ?: defaults.iconShape,
            customCornerRadiusDp = prefs[Keys.CUSTOM_CORNER_RADIUS] ?: defaults.customCornerRadiusDp,
            iconPackPreset = prefs[Keys.ICON_PACK_PRESET]?.let {
                runCatching { IconPackPreset.valueOf(it) }.getOrNull()
            } ?: defaults.iconPackPreset,
            selectedThirdPartyIconPack = prefs[Keys.THIRD_PARTY_PACK] ?: defaults.selectedThirdPartyIconPack,
            iconLabelSizeSp = prefs[Keys.LABEL_SIZE_SP] ?: defaults.iconLabelSizeSp,
            iconLabelColorHex = prefs[Keys.LABEL_COLOR_HEX] ?: defaults.iconLabelColorHex,
            showHomeLabels = prefs[Keys.SHOW_HOME_LABELS] ?: defaults.showHomeLabels,
            showDockLabels = prefs[Keys.SHOW_DOCK_LABELS] ?: defaults.showDockLabels,
            showDrawerLabels = prefs[Keys.SHOW_DRAWER_LABELS] ?: defaults.showDrawerLabels,
            iconSpacingDp = prefs[Keys.ICON_SPACING_DP] ?: defaults.iconSpacingDp,
            showIconBackgroundGlow = prefs[Keys.SHOW_ICON_BG_GLOW] ?: defaults.showIconBackgroundGlow,
            drawerGridColumns = prefs[Keys.DRAWER_COLS] ?: defaults.drawerGridColumns,
            drawerSortMode = prefs[Keys.DRAWER_SORT] ?: defaults.drawerSortMode,
            drawerBackgroundAlpha = prefs[Keys.DRAWER_BG_ALPHA] ?: defaults.drawerBackgroundAlpha,
            animationSpeedMultiplier = prefs[Keys.ANIM_SPEED] ?: defaults.animationSpeedMultiplier,
            springDampingRatio = prefs[Keys.SPRING_DAMPING] ?: defaults.springDampingRatio,
            pageTransitionEffect = prefs[Keys.PAGE_TRANSITION]?.let {
                runCatching { PageTransitionEffect.valueOf(it) }.getOrNull()
            } ?: defaults.pageTransitionEffect,
            enableAppLaunchAnimation = prefs[Keys.ENABLE_APP_ANIM] ?: defaults.enableAppLaunchAnimation,
            enableWallpaperParallax = prefs[Keys.ENABLE_PARALLAX] ?: defaults.enableWallpaperParallax,
            swipeUpGesture = prefs[Keys.SWIPE_UP]?.let {
                runCatching { GestureAction.valueOf(it) }.getOrNull()
            } ?: defaults.swipeUpGesture,
            swipeDownGesture = prefs[Keys.SWIPE_DOWN]?.let {
                runCatching { GestureAction.valueOf(it) }.getOrNull()
            } ?: defaults.swipeDownGesture,
            doubleTapGesture = prefs[Keys.DOUBLE_TAP]?.let {
                runCatching { GestureAction.valueOf(it) }.getOrNull()
            } ?: defaults.doubleTapGesture,
            twoFingerSwipeGesture = prefs[Keys.TWO_FINGER]?.let {
                runCatching { GestureAction.valueOf(it) }.getOrNull()
            } ?: defaults.twoFingerSwipeGesture,
            longPressGesture = prefs[Keys.LONG_PRESS]?.let {
                runCatching { GestureAction.valueOf(it) }.getOrNull()
            } ?: defaults.longPressGesture,
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: defaults.themeMode,
            accentColorHex = prefs[Keys.ACCENT_HEX] ?: defaults.accentColorHex,
            fontFamilyName = prefs[Keys.FONT_FAMILY] ?: defaults.fontFamilyName,
            fontScale = prefs[Keys.FONT_SCALE] ?: defaults.fontScale,
            activeWallpaperId = prefs[Keys.WALLPAPER_ID] ?: defaults.activeWallpaperId,
            customWallpaperUri = prefs[Keys.CUSTOM_WALLPAPER_URI] ?: defaults.customWallpaperUri,
            favoriteWallpapers = prefs[Keys.FAVORITE_WALLPAPERS] ?: defaults.favoriteWallpapers,
            weatherCityAr = prefs[Keys.WEATHER_CITY] ?: defaults.weatherCityAr,
            weatherTempCelsius = prefs[Keys.WEATHER_TEMP] ?: defaults.weatherTempCelsius,
            weatherConditionAr = prefs[Keys.WEATHER_COND] ?: defaults.weatherConditionAr,
            highRefreshRate120Hz = prefs[Keys.HIGH_REFRESH_120] ?: defaults.highRefreshRate120Hz,
            hyperOsMemoryOptimization = prefs[Keys.HYPER_OS_OPT] ?: defaults.hyperOsMemoryOptimization,
            lockHomeLayout = prefs[Keys.LOCK_HOME_LAYOUT] ?: defaults.lockHomeLayout
        )
    }

    suspend fun saveSettings(settings: LauncherSettings) {
        context.launcherDataStore.edit { prefs ->
            prefs[Keys.HOME_COLS] = settings.homeGridColumns
            prefs[Keys.HOME_ROWS] = settings.homeGridRows
            prefs[Keys.DOCK_COUNT] = settings.dockIconCount
            prefs[Keys.HOME_PAGE_COUNT] = settings.homePageCount
            prefs[Keys.DEFAULT_HOME_PAGE] = settings.defaultHomePage
            prefs[Keys.ICON_SIZE_DP] = settings.iconSizeDp
            prefs[Keys.ICON_SHAPE] = settings.iconShape.name
            prefs[Keys.CUSTOM_CORNER_RADIUS] = settings.customCornerRadiusDp
            prefs[Keys.ICON_PACK_PRESET] = settings.iconPackPreset.name
            prefs[Keys.THIRD_PARTY_PACK] = settings.selectedThirdPartyIconPack
            prefs[Keys.LABEL_SIZE_SP] = settings.iconLabelSizeSp
            prefs[Keys.LABEL_COLOR_HEX] = settings.iconLabelColorHex
            prefs[Keys.SHOW_HOME_LABELS] = settings.showHomeLabels
            prefs[Keys.SHOW_DOCK_LABELS] = settings.showDockLabels
            prefs[Keys.SHOW_DRAWER_LABELS] = settings.showDrawerLabels
            prefs[Keys.ICON_SPACING_DP] = settings.iconSpacingDp
            prefs[Keys.SHOW_ICON_BG_GLOW] = settings.showIconBackgroundGlow
            prefs[Keys.DRAWER_COLS] = settings.drawerGridColumns
            prefs[Keys.DRAWER_SORT] = settings.drawerSortMode
            prefs[Keys.DRAWER_BG_ALPHA] = settings.drawerBackgroundAlpha
            prefs[Keys.ANIM_SPEED] = settings.animationSpeedMultiplier
            prefs[Keys.SPRING_DAMPING] = settings.springDampingRatio
            prefs[Keys.PAGE_TRANSITION] = settings.pageTransitionEffect.name
            prefs[Keys.ENABLE_APP_ANIM] = settings.enableAppLaunchAnimation
            prefs[Keys.ENABLE_PARALLAX] = settings.enableWallpaperParallax
            prefs[Keys.SWIPE_UP] = settings.swipeUpGesture.name
            prefs[Keys.SWIPE_DOWN] = settings.swipeDownGesture.name
            prefs[Keys.DOUBLE_TAP] = settings.doubleTapGesture.name
            prefs[Keys.TWO_FINGER] = settings.twoFingerSwipeGesture.name
            prefs[Keys.LONG_PRESS] = settings.longPressGesture.name
            prefs[Keys.THEME_MODE] = settings.themeMode.name
            prefs[Keys.ACCENT_HEX] = settings.accentColorHex
            prefs[Keys.FONT_FAMILY] = settings.fontFamilyName
            prefs[Keys.FONT_SCALE] = settings.fontScale
            prefs[Keys.WALLPAPER_ID] = settings.activeWallpaperId
            prefs[Keys.CUSTOM_WALLPAPER_URI] = settings.customWallpaperUri
            prefs[Keys.FAVORITE_WALLPAPERS] = settings.favoriteWallpapers
            prefs[Keys.WEATHER_CITY] = settings.weatherCityAr
            prefs[Keys.WEATHER_TEMP] = settings.weatherTempCelsius
            prefs[Keys.WEATHER_COND] = settings.weatherConditionAr
            prefs[Keys.HIGH_REFRESH_120] = settings.highRefreshRate120Hz
            prefs[Keys.HYPER_OS_OPT] = settings.hyperOsMemoryOptimization
            prefs[Keys.LOCK_HOME_LAYOUT] = settings.lockHomeLayout
        }
    }
}
