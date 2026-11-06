package com.example.engine

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WallpaperItem(
    val id: String,
    val titleAr: String,
    val categoryAr: String,
    val subtitleAr: String,
    val colors: List<Color>,
    val accentGlow: Color,
    val styleType: String // WAVE, MOUNTAIN, DUNES, LOTUS, SPHERES, AMOLED, GEOMETRIC
)

object WallpaperEngine {

    val categories = listOf(
        "الكل",
        "سامسونج One UI",
        "AMOLED",
        "طبيعة",
        "تجريدية",
        "ثلاثية الأبعاد",
        "هندسية",
        "المفضلة"
    )

    val builtInWallpapers = listOf(
        WallpaperItem(
            id = "one_ui_9_signature",
            titleAr = "أمواج One UI 9 البنفسجية",
            categoryAr = "سامسونج One UI",
            subtitleAr = "الخلفية الرسمية لواجهة One UI 9 مع تموجات الحرير البنفسجية والزرقاء",
            colors = listOf(Color(0xFF1E225A), Color(0xFF4338CA), Color(0xFF9333EA), Color(0xFF171636)),
            accentGlow = Color(0xFFD8B4FE),
            styleType = "WAVE"
        ),
        WallpaperItem(
            id = "nature_mountain_twilight",
            titleAr = "قمم الجبال عند الشفق",
            categoryAr = "طبيعة",
            subtitleAr = "سلسلة جبال هادئة تحت شفق وردي وبنفسجي ساحر",
            colors = listOf(Color(0xFF1E1B4B), Color(0xFF701A75), Color(0xFFF472B6), Color(0xFF0F172A)),
            accentGlow = Color(0xFFFBCFE8),
            styleType = "MOUNTAIN"
        ),
        WallpaperItem(
            id = "nature_desert_dunes",
            titleAr = "كثبان الصحراء الذهبية",
            categoryAr = "طبيعة",
            subtitleAr = "تموجات الرمال الذهبية الدافئة وقت الغروب العربي الأصيل",
            colors = listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFF451A03)),
            accentGlow = Color(0xFFFDE68A),
            styleType = "DUNES"
        ),
        WallpaperItem(
            id = "artistic_lotus_bloom",
            titleAr = "زهرة اللوتس المجرية",
            categoryAr = "تجريدية",
            subtitleAr = "بتلات متوهجة بتدرجات الأرجواني والوردي الفاخر",
            colors = listOf(Color(0xFF2E1065), Color(0xFF9333EA), Color(0xFFEC4899), Color(0xFF090514)),
            accentGlow = Color(0xFFF9A8D4),
            styleType = "LOTUS"
        ),
        WallpaperItem(
            id = "3d_glass_spheres",
            titleAr = "كرات الزجاج ثلاثية الأبعاد",
            categoryAr = "ثلاثية الأبعاد",
            subtitleAr = "كرات بلورية عائمة بعمق ضوئي ثلاثي الأبعاد",
            colors = listOf(Color(0xFF090D26), Color(0xFF1D4ED8), Color(0xFF06B6D4), Color(0xFF020617)),
            accentGlow = Color(0xFF67E8F9),
            styleType = "SPHERES"
        ),
        WallpaperItem(
            id = "amoled_obsidian_neon",
            titleAr = "سواد AMOLED والنيون الأزرق",
            categoryAr = "AMOLED",
            subtitleAr = "خلفية سوداء عميقة توفر الطاقة مع هالة نيون لشاشات 120Hz",
            colors = listOf(Color(0xFF000000), Color(0xFF050816), Color(0xFF1E3A8A), Color(0xFF000000)),
            accentGlow = Color(0xFF38BDF8),
            styleType = "AMOLED"
        ),
        WallpaperItem(
            id = "amoled_eclipse_gold",
            titleAr = "كسوف AMOLED الذهبي",
            categoryAr = "AMOLED",
            subtitleAr = "حلقة كسوف ذهبية متوهجة على خلفية سوداء مطلقة",
            colors = listOf(Color(0xFF000000), Color(0xFF140E05), Color(0xFFB45309), Color(0xFF000000)),
            accentGlow = Color(0xFFFBBF24),
            styleType = "AMOLED"
        ),
        WallpaperItem(
            id = "galaxy_s25_ultra",
            titleAr = "بلورات Galaxy Ultra",
            categoryAr = "سامسونج One UI",
            subtitleAr = "انكسارات ضوئية مستوحاة من سلسلة Galaxy S الرائدة",
            colors = listOf(Color(0xFF0F172A), Color(0xFF312E81), Color(0xFF4F46E5), Color(0xFF090D16)),
            accentGlow = Color(0xFF818CF8),
            styleType = "GEOMETRIC"
        ),
        WallpaperItem(
            id = "abstract_liquid_glass",
            titleAr = "الحرير السائل المتدرج",
            categoryAr = "تجريدية",
            subtitleAr = "انسياب لوني ناعم بين البنفسجي والسماوي",
            colors = listOf(Color(0xFF1E1B4B), Color(0xFF0284C7), Color(0xFF7C3AED), Color(0xFF0F172A)),
            accentGlow = Color(0xFF7DD3FC),
            styleType = "WAVE"
        ),
        WallpaperItem(
            id = "geometric_prism_dark",
            titleAr = "الموشور الهندسي الداكن",
            categoryAr = "هندسية",
            subtitleAr = "خطوط ومضلعات هندسية دقيقة بتباين راقٍ",
            colors = listOf(Color(0xFF090D16), Color(0xFF1E293B), Color(0xFF3B82F6), Color(0xFF020617)),
            accentGlow = Color(0xFF60A5FA),
            styleType = "GEOMETRIC"
        )
    )

    fun findById(id: String): WallpaperItem {
        return builtInWallpapers.firstOrNull { it.id == id } ?: builtInWallpapers.first()
    }

    @Composable
    fun WallpaperBackgroundView(
        wallpaperId: String,
        customWallpaperUri: String,
        parallaxOffsetFraction: Float = 0f,
        modifier: Modifier = Modifier
    ) {
        if (customWallpaperUri.isNotBlank()) {
            Image(
                painter = rememberAsyncImagePainter(Uri.parse(customWallpaperUri)),
                contentDescription = "خلفية مخصصة",
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
            return
        }

        val item = findById(wallpaperId)
        ProceduralWallpaperCanvas(
            item = item,
            parallaxOffset = parallaxOffsetFraction,
            modifier = modifier.fillMaxSize()
        )
    }

    @Composable
    fun ProceduralWallpaperCanvas(
        item: WallpaperItem,
        parallaxOffset: Float = 0f,
        modifier: Modifier = Modifier
    ) {
        ComposeCanvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            val shiftX = parallaxOffset * w * 0.08f

            // Base vertical/diagonal gradient
            drawRect(
                brush = Brush.linearGradient(
                    colors = item.colors,
                    start = Offset(0f + shiftX, 0f),
                    end = Offset(w - shiftX, h)
                )
            )

            when (item.styleType) {
                "WAVE" -> {
                    // Iconic One UI 9 flowing silk diagonal ribbon waves (matches center phone in reference image)
                    val wave1 = ComposePath().apply {
                        moveTo(-w * 0.1f, h * 0.28f)
                        cubicTo(
                            w * 0.35f + shiftX, h * 0.15f,
                            w * 0.55f - shiftX, h * 0.52f,
                            w * 1.1f, h * 0.36f
                        )
                        lineTo(w * 1.1f, h * 0.85f)
                        cubicTo(
                            w * 0.6f, h * 0.65f,
                            w * 0.25f, h * 0.82f,
                            -w * 0.1f, h * 0.68f
                        )
                        close()
                    }
                    drawPath(
                        path = wave1,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                item.accentGlow.copy(alpha = 0.65f),
                                item.colors[2].copy(alpha = 0.45f),
                                item.colors[1].copy(alpha = 0.2f)
                            ),
                            start = Offset(w * 0.1f, h * 0.2f),
                            end = Offset(w * 0.9f, h * 0.8f)
                        )
                    )

                    val wave2 = ComposePath().apply {
                        moveTo(-w * 0.1f, h * 0.52f)
                        cubicTo(
                            w * 0.3f, h * 0.35f,
                            w * 0.75f + shiftX, h * 0.68f,
                            w * 1.1f, h * 0.48f
                        )
                        lineTo(w * 1.1f, h * 1.05f)
                        lineTo(-w * 0.1f, h * 1.05f)
                        close()
                    }
                    drawPath(
                        path = wave2,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                item.accentGlow.copy(alpha = 0.35f),
                                item.colors.first().copy(alpha = 0.88f)
                            )
                        )
                    )
                }

                "MOUNTAIN" -> {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(item.accentGlow.copy(alpha = 0.7f), Color.Transparent),
                            center = Offset(w * 0.5f + shiftX, h * 0.35f),
                            radius = w * 0.55f
                        ),
                        center = Offset(w * 0.5f + shiftX, h * 0.35f),
                        radius = w * 0.55f
                    )
                    val m1 = ComposePath().apply {
                        moveTo(0f, h * 0.68f)
                        lineTo(w * 0.32f, h * 0.44f)
                        lineTo(w * 0.65f, h * 0.60f)
                        lineTo(w * 0.88f, h * 0.46f)
                        lineTo(w, h * 0.58f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(m1, color = Color(0xFF13112C).copy(alpha = 0.85f))
                }

                "DUNES" -> {
                    val d1 = ComposePath().apply {
                        moveTo(0f, h * 0.50f)
                        quadraticTo(w * 0.45f + shiftX, h * 0.38f, w, h * 0.56f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        d1,
                        brush = Brush.verticalGradient(
                            listOf(item.accentGlow.copy(alpha = 0.55f), item.colors.last())
                        )
                    )
                }

                "LOTUS" -> {
                    val center = Offset(w * 0.5f + shiftX, h * 0.52f)
                    for (i in -3..3) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    item.accentGlow.copy(alpha = 0.38f),
                                    item.colors[1].copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                center = Offset(center.x + i * w * 0.08f, center.y - kotlin.math.abs(i) * h * 0.04f),
                                radius = w * 0.42f
                            ),
                            radius = w * 0.42f,
                            center = Offset(center.x + i * w * 0.08f, center.y - kotlin.math.abs(i) * h * 0.04f)
                        )
                    }
                }

                "SPHERES", "AMOLED" -> {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                item.accentGlow.copy(alpha = 0.55f),
                                item.colors[2].copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f + shiftX, h * 0.42f),
                            radius = w * 0.52f
                        ),
                        center = Offset(w * 0.5f + shiftX, h * 0.42f),
                        radius = w * 0.52f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                item.colors[1].copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.25f - shiftX, h * 0.72f),
                            radius = w * 0.40f
                        ),
                        center = Offset(w * 0.25f - shiftX, h * 0.72f),
                        radius = w * 0.40f
                    )
                }

                else -> {
                    val prism = ComposePath().apply {
                        moveTo(w * 0.15f, h * 0.2f)
                        lineTo(w * 0.85f, h * 0.35f)
                        lineTo(w * 0.5f, h * 0.82f)
                        close()
                    }
                    drawPath(
                        prism,
                        brush = Brush.linearGradient(
                            listOf(item.accentGlow.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
                }
            }
        }
    }

    /**
     * Generates a high-resolution native Android Bitmap and applies it to the real Android
     * System WallpaperManager (Home Screen, Lock Screen, or Both).
     */
    suspend fun applySystemWallpaper(
        context: Context,
        wallpaperId: String,
        targetFlag: Int // WallpaperManager.FLAG_SYSTEM, FLAG_LOCK, or FLAG_SYSTEM or FLAG_LOCK
    ): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val item = findById(wallpaperId)
            val width = 1080
            val height = 2400
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            val intColors = item.colors.map {
                android.graphics.Color.argb(
                    (it.alpha * 255).toInt(),
                    (it.red * 255).toInt(),
                    (it.green * 255).toInt(),
                    (it.blue * 255).toInt()
                )
            }.toIntArray()

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intColors, null, Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            val glowColor = android.graphics.Color.argb(
                170,
                (item.accentGlow.red * 255).toInt(),
                (item.accentGlow.green * 255).toInt(),
                (item.accentGlow.blue * 255).toInt()
            )
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    width * 0.5f,
                    height * 0.42f,
                    width * 0.65f,
                    intArrayOf(glowColor, android.graphics.Color.TRANSPARENT),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            val wavePath = Path().apply {
                moveTo(0f, height * 0.30f)
                cubicTo(
                    width * 0.35f, height * 0.18f,
                    width * 0.65f, height * 0.55f,
                    width.toFloat(), height * 0.38f
                )
                lineTo(width.toFloat(), height * 0.85f)
                lineTo(0f, height * 0.72f)
                close()
            }
            canvas.drawPath(wavePath, glowPaint)

            val wm = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wm.setBitmap(bmp, null, true, targetFlag)
            } else {
                wm.setBitmap(bmp)
            }
            true
        }.getOrDefault(false)
    }
}
