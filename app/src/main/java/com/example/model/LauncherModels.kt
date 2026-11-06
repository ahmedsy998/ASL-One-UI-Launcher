package com.example.model

import androidx.compose.ui.graphics.ImageBitmap
import com.squareup.moshi.JsonClass

enum class ThemeMode(val titleAr: String, val subtitleAr: String) {
    ONE_UI_DARK_GLASS("الوضع الداكن الزجاجي (One UI 9)", "واجهات شفافة وتدرجات بنفسجية وزرقاء عميقة"),
    AMOLED_BLACK("وضع AMOLED الأسود الفائق", "سواد حقيقي #000000 لتوفير بطارية شاشات AMOLED"),
    ONE_UI_LIGHT("الوضع الفاتح النقي", "واجهة نهارية مشرقة بألوان ناعمة ومريحة للعين")
}

enum class IconShapeStyle(val titleAr: String, val cornerFraction: Float) {
    ONE_UI_SQUIRCLE("مربع سامسونج المنحني (One UI)", 0.30f),
    ROUNDED_SQUARE("مربع بحواف دائرية", 0.22f),
    CIRCLE("دائري كامل", 0.50f),
    TEARDROP("قطرة ماء", 0.36f),
    HEXAGON("سداسي الأضلاع", 0.18f),
    CUSTOM_RADIUS("نصف قطر مخصص", -1f)
}

enum class IconPackPreset(val titleAr: String, val subtitleAr: String) {
    ONE_UI_9_DEFAULT("حزمة One UI 9 الأصلية", "أيقونات سامسونج الغنية بالتفاصيل والألوان الزاهية"),
    GALAXY_DARK_GLASS("زجاج المجرة الداكن", "أيقونات داكنة شفافة مع إضاءة محيطية ناعمة"),
    AMOLED_NEON("نيون AMOLED", "خلفيات سوداء مع رموز متوهجة عالية التباين"),
    PASTEL_MINIMAL("باستيل هادئ", "ألوان باستيل ناعمة ومريحة للنظر"),
    GOLDEN_LUXURY("ذهبي ملكي", "تدرجات ذهبية فاخرة على خلفية كحلية")
}

enum class PageTransitionEffect(val titleAr: String, val descriptionAr: String) {
    ONE_UI_SLIDE("انزلاق One UI السلس", "انتقال أفقي فائق النعومة مع استجابة 120Hz"),
    CAROUSEL_3D("بطاقات ثلاثية الأبعاد", "تأثير دوران البطاقات بعمق ثلاثي الأبعاد كما في المرجع"),
    DEPTH_STACK("تكديس العمق", "تلاشي وتصغير الصفحة الخلفية بنعومة"),
    CUBE_ROTATION("مكعب دوّار", "تدوير الصفحات كأوجه مكعب متكامل"),
    ACCORDION_FOLD("طي متدرج", "تمدد وانكماش مرن للصفحات أثناء السحب")
}

enum class GestureAction(val titleAr: String) {
    OPEN_APP_DRAWER("فتح درج التطبيقات"),
    OPEN_SEARCH("فتح البحث الذكي الشامل"),
    EXPAND_NOTIFICATIONS("سحب لوحة الإشعارات للنظام"),
    OPEN_CUSTOMIZATION("فتح وضع تخصيص الشاشة"),
    OPEN_SETTINGS("فتح إعدادات اللانشر"),
    OPEN_WIDGETS("فتح معرض الويدجيت"),
    OPEN_WALLPAPERS("فتح مكتبة الخلفيات"),
    LOCK_SCREEN_GUIDE("قفل الشاشة (دليل الصلاحيات)"),
    NONE("إيقاف الإيماءة")
}

enum class HomeItemType {
    APP,
    FOLDER,
    BUILTIN_WIDGET,
    SYSTEM_WIDGET
}

enum class BuiltInWidgetType(
    val titleAr: String,
    val categoryAr: String,
    val defaultSpanX: Int,
    val defaultSpanY: Int,
    val descriptionAr: String
) {
    CLOCK_WEATHER_DUAL(
        "الساعة والطقس",
        "سامسونج",
        4,
        2,
        "ويدجيت One UI 9 المزدوج للساعة والتاريخ وحالة الطقس في مدينتك"
    ),
    GOOGLE_SEARCH_BAR(
        "شريط بحث Google الذكي",
        "سامسونج",
        4,
        1,
        "شريط بحث سريع مع اختصار الأوامر الصوتية والعدسة والبحث الفوري"
    ),
    ANALOG_CLOCK(
        "الساعة التناظرية",
        "الساعة",
        2,
        2,
        "ساعة عقارب كلاسيكية بتصميم زجاجي داكن مستوحى من One UI 9"
    ),
    CALENDAR_MONTH(
        "التقويم الشهري",
        "سامسونج",
        2,
        2,
        "عرض شهري كامل باللغة العربية مع تمييز اليوم الحالي"
    ),
    BATTERY_RING(
        "البطارية الذكية",
        "سامسونج",
        2,
        2,
        "حلقة طاقة تفاعلية تعرض نسبة شحن بطارية الهاتف الحقيقية وحالتها"
    ),
    MUSIC_WAVE(
        "الموسيقى والموجات",
        "سامسونج",
        2,
        2,
        "مشغل وسائط مع موجات صوتية متحركة وأزرار التحكم بالصوت"
    ),
    WORLD_CLOCK(
        "الساعة العالمية",
        "الساعة",
        4,
        2,
        "توقيت دمشق، مكة المكرمة، القاهرة، ودبي في بطاقة واحدة"
    ),
    WEATHER_DETAILED(
        "الطقس التفصيلي",
        "الطقس",
        4,
        2,
        "توقعات الطقس ودرجات الحرارة العظمى والصغرى والرطوبة"
    )
}

enum class OneUiSystemIconKind {
    PHONE,
    MESSAGES,
    CONTACTS,
    CAMERA,
    GALLERY,
    CLOCK,
    CALENDAR,
    SETTINGS,
    INTERNET,
    FILES,
    NOTES,
    REMINDERS,
    CALCULATOR,
    RECORDER,
    WEATHER,
    HEALTH,
    GALAXY_STORE,
    PLAY_STORE,
    MAPS,
    GMAIL,
    GENERIC_APP
}

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val labelAr: String,
    val labelEn: String,
    val iconKind: OneUiSystemIconKind = OneUiSystemIconKind.GENERIC_APP,
    val nativeIcon: ImageBitmap? = null,
    val isSystemShortcut: Boolean = false,
    val isHidden: Boolean = false,
    val customLabel: String? = null,
    val customIconUri: String? = null,
    val launchCount: Int = 0,
    val lastLaunchedAt: Long = 0L,
    val drawerFolder: String? = null
) {
    val displayLabel: String
        get() = customLabel?.takeIf { it.isNotBlank() } ?: labelAr
}

@JsonClass(generateAdapter = true)
data class LauncherSettings(
    val homeGridColumns: Int = 4,
    val homeGridRows: Int = 5,
    val dockIconCount: Int = 4,
    val homePageCount: Int = 3,
    val defaultHomePage: Int = 0,
    // Icon customization
    val iconSizeDp: Int = 58,
    val iconShape: IconShapeStyle = IconShapeStyle.ONE_UI_SQUIRCLE,
    val customCornerRadiusDp: Int = 18,
    val iconPackPreset: IconPackPreset = IconPackPreset.ONE_UI_9_DEFAULT,
    val selectedThirdPartyIconPack: String = "",
    val iconLabelSizeSp: Float = 11.5f,
    val iconLabelColorHex: String = "#FFFFFF",
    val showHomeLabels: Boolean = true,
    val showDockLabels: Boolean = false,
    val showDrawerLabels: Boolean = true,
    val iconSpacingDp: Int = 8,
    val showIconBackgroundGlow: Boolean = true,
    // App Drawer
    val drawerGridColumns: Int = 4,
    val drawerSortMode: String = "ALPHABETICAL", // ALPHABETICAL, RECENT, MOST_USED
    val drawerBackgroundAlpha: Float = 0.88f,
    // Animations
    val animationSpeedMultiplier: Float = 1.0f,
    val springDampingRatio: Float = 0.78f,
    val pageTransitionEffect: PageTransitionEffect = PageTransitionEffect.CAROUSEL_3D,
    val enableAppLaunchAnimation: Boolean = true,
    val enableWallpaperParallax: Boolean = true,
    // Gestures
    val swipeUpGesture: GestureAction = GestureAction.OPEN_APP_DRAWER,
    val swipeDownGesture: GestureAction = GestureAction.EXPAND_NOTIFICATIONS,
    val doubleTapGesture: GestureAction = GestureAction.OPEN_SEARCH,
    val twoFingerSwipeGesture: GestureAction = GestureAction.OPEN_SETTINGS,
    val longPressGesture: GestureAction = GestureAction.OPEN_CUSTOMIZATION,
    // Theme & Appearance
    val themeMode: ThemeMode = ThemeMode.ONE_UI_DARK_GLASS,
    val accentColorHex: String = "#6366F1",
    val fontFamilyName: String = "Cairo",
    val fontScale: Float = 1.0f,
    // Wallpaper
    val activeWallpaperId: String = "one_ui_9_signature",
    val customWallpaperUri: String = "",
    val favoriteWallpapers: Set<String> = setOf("one_ui_9_signature", "nature_mountain_twilight", "artistic_lotus_bloom"),
    // Weather & Performance
    val weatherCityAr: String = "دمشق",
    val weatherTempCelsius: Int = 24,
    val weatherConditionAr: String = "مشمس غائم جزئياً",
    val highRefreshRate120Hz: Boolean = true,
    val hyperOsMemoryOptimization: Boolean = true,
    val lockHomeLayout: Boolean = false
)

@JsonClass(generateAdapter = true)
data class FolderAppEntry(
    val packageName: String,
    val labelAr: String,
    val iconKindName: String = OneUiSystemIconKind.GENERIC_APP.name
)
