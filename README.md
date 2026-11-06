# ASL-One-UI-Launcher (ASL One UI 9 Launcher)

لانشر أندرويد أصلي متكامل (**Native Android Home Launcher**) مطوّر بلغة **Kotlin** و **Jetpack Compose**، مستوحى من التصميم البصري وتجربة التخصيص لواجهة **Samsung One UI 9**، مع دعم كامل للغة العربية (RTL) وتحسين خاص لأجهزة **Xiaomi Redmi Note 13 Pro 5G (Snapdragon 7s Gen 2 / Android 14 / HyperOS / 120Hz AMOLED)**.

---

## طبيعة المشروع (Project Architecture)

هذا المشروع هو **تطبيق أندرويد أصلي كامل (Native Android Studio / Gradle Kotlin Project)** وليس موقع ويب أو تطبيق WebView:
- مُعرَّف في `app/src/main/AndroidManifest.xml` بفلتر اللانشر الأساسي للنظام:
  - `android.intent.action.MAIN`
  - `android.intent.category.HOME`
  - `android.intent.category.DEFAULT`
  - `android.intent.category.LAUNCHER`
- يرتبط مباشرة بواجهات نظام أندرويد الحقيقية:
  - `PackageManager` لاكتشاف وتشغيل جميع التطبيقات المثبتة على الهاتف والاستماع الفوري لتثبيت أو إزالة التطبيقات (`BroadcastReceiver`).
  - `AppWidgetHost` و `AppWidgetManager` لاستضافة ويدجيت تطبيقات النظام الحقيقية بجانب ويدجيت One UI 9 المدمجة.
  - `WallpaperManager` لتعيين الخلفيات عالية الدقة على الشاشة الرئيسية أو شاشة القفل أو كليهما معاً.
  - `BatteryManager` و `AudioManager` لقراءة نسبة شحن البطارية الحقيقية والتحكم بالوسائط الصوتية.
  - `Room Database` و `Jetpack DataStore` لحفظ تخطيط الشاشة الرئيسية، المجلدات، الويدجيت، وتخصيص الأيقونات، مع دعم التصدير والاستيراد الكامل بصيغة `JSON`.

---

## هيكلية ملفات المشروع الكاملة (Project Structure)

```text
ASL-One-UI-Launcher/
├── .env.example
├── .gitignore
├── README.md
├── build.gradle.kts
├── gradle.properties
├── metadata.json
├── settings.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       └── gradle-wrapper.properties
└── app/
    ├── .gitignore
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/example/
        │   │   ├── MainActivity.kt
        │   │   ├── data/
        │   │   │   ├── db/
        │   │   │   │   └── LauncherDatabase.kt
        │   │   │   └── prefs/
        │   │   │       └── LauncherPreferencesRepository.kt
        │   │   ├── engine/
        │   │   │   ├── AppManagerEngine.kt
        │   │   │   ├── OneUiIconRenderer.kt
        │   │   │   └── WallpaperEngine.kt
        │   │   ├── model/
        │   │   │   └── LauncherModels.kt
        │   │   ├── ui/
        │   │   │   ├── components/
        │   │   │   │   └── OneUiWidgets.kt
        │   │   │   ├── screens/
        │   │   │   │   ├── AppDrawerScreen.kt
        │   │   │   │   ├── CustomizationOverlay.kt
        │   │   │   │   ├── HomeScreen.kt
        │   │   │   │   ├── LauncherSettingsScreen.kt
        │   │   │   │   ├── SmartSearchOverlay.kt
        │   │   │   │   ├── WallpaperCenterSheet.kt
        │   │   │   │   └── WidgetGallerySheet.kt
        │   │   │   └── theme/
        │   │   │       ├── Color.kt
        │   │   │       ├── Theme.kt
        │   │   │       └── Type.kt
        │   │   ├── viewmodel/
        │   │   │   └── LauncherViewModel.kt
        │   │   └── widget/
        │   │       └── LauncherWidgetHost.kt
        │   └── res/
        │       ├── drawable/
        │       │   ├── ic_launcher_background.xml
        │       │   └── ic_launcher_foreground.xml
        │       ├── font/
        │       │   ├── cairo.ttf
        │       │   └── tajawal.ttf
        │       ├── mipmap-anydpi-v26/
        │       │   ├── ic_launcher.xml
        │       │   └── ic_launcher_round.xml
        │       ├── mipmap-hdpi/
        │       ├── mipmap-mdpi/
        │       ├── mipmap-xhdpi/
        │       ├── mipmap-xxhdpi/
        │       ├── mipmap-xxxhdpi/
        │       ├── values/
        │       │   ├── colors.xml
        │       │   ├── strings.xml
        │       │   └── themes.xml
        │       └── xml/
        │           ├── backup_rules.xml
        │           └── data_extraction_rules.xml
        ├── test/java/com/example/
        │   ├── ExampleRobolectricTest.kt
        │   └── ExampleUnitTest.kt
        └── androidTest/java/com/example/
            └── ExampleInstrumentedTest.kt
```

---

## طريقة التصدير إلى GitHub وبناء ملف APK

### 1. التصدير المباشر إلى GitHub من Google AI Studio
1. اضغط على قائمة الإعدادات / التصدير (**Settings / Export**) في واجهة Google AI Studio.
2. اختر **Push to GitHub** وحدد اسم المستودع:
   - **Repository Name:** `ASL-One-UI-Launcher`
   - **Default Branch:** `main`
3. أو اختر **Download APK** / **Export ZIP** لتحميل ملف الـ APK الجاهز للتثبيت مباشرة على هاتفك.

### 2. البناء محلياً عبر Android Studio أو سطر الأوامر
```bash
# بناء نسخة Debug APK القابلة للتثبيت المباشر
gradle :app:assembleDebug

# تشغيل اختبارات الوحدة والتحقق
gradle :app:testDebugUnitTest
```
سيتم توليد ملف الـ APK القابل للتثبيت في المسار:
`app/build/outputs/apk/debug/app-debug.apk`

---

## التفعيل كواجهة أساسية للهاتف على Xiaomi HyperOS / Android 14
1. ثبّت ملف الـ APK على جهاز **Xiaomi Redmi Note 13 Pro 5G** (أو أي هاتف أندرويد).
2. افتح تطبيق **ASL One UI Launcher** واضغط على شريط **«تعيين ASL One UI 9 كواجهة أساسية للهاتف»** في الشاشة الرئيسية أو من داخل **إعدادات اللانشر**.
3. اختر **ASL One UI Launcher** كـ **تطبيق الشاشة الرئيسية الافتراضي (Default Home App)**.
