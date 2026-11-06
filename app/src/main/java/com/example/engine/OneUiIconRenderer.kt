package com.example.engine

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.model.IconPackPreset
import com.example.model.IconShapeStyle
import com.example.model.InstalledApp
import com.example.model.LauncherSettings
import com.example.model.OneUiSystemIconKind
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

fun resolveIconShape(style: IconShapeStyle, customRadiusDp: Int): Shape {
    return when (style) {
        IconShapeStyle.ONE_UI_SQUIRCLE -> RoundedCornerShape(percent = 30)
        IconShapeStyle.ROUNDED_SQUARE -> RoundedCornerShape(percent = 22)
        IconShapeStyle.CIRCLE -> CircleShape
        IconShapeStyle.TEARDROP -> RoundedCornerShape(
            topStartPercent = 50,
            topEndPercent = 50,
            bottomEndPercent = 14,
            bottomStartPercent = 50
        )
        IconShapeStyle.HEXAGON -> GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = minOf(w, h) / 2f
            for (i in 0 until 6) {
                val angle = Math.toRadians((60.0 * i) - 30.0)
                val x = cx + (r * cos(angle)).toFloat()
                val y = cy + (r * sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        IconShapeStyle.CUSTOM_RADIUS -> RoundedCornerShape(customRadiusDp.dp)
    }
}

@Composable
fun OneUiAppIconView(
    app: InstalledApp,
    settings: LauncherSettings,
    sizeOverrideDp: Dp? = null,
    modifier: Modifier = Modifier
) {
    val iconDp = sizeOverrideDp ?: settings.iconSizeDp.dp
    val shape = resolveIconShape(settings.iconShape, settings.customCornerRadiusDp)

    // Custom Gallery Image override if assigned by user
    if (!app.customIconUri.isNullOrBlank()) {
        Box(
            modifier = modifier
                .size(iconDp)
                .shadow(if (settings.showIconBackgroundGlow) 6.dp else 0.dp, shape)
                .clip(shape)
                .background(Color(0xFF1E293B))
                .border(1.dp, Color.White.copy(alpha = 0.22f), shape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(Uri.parse(app.customIconUri)),
                contentDescription = app.displayLabel,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    // If app is one of the 20 iconic One UI 9 apps or uses an IconPackPreset, render our crisp One UI 9 vector artwork
    if (app.iconKind != OneUiSystemIconKind.GENERIC_APP || app.nativeIcon == null) {
        OneUiStyledVectorIcon(
            kind = app.iconKind,
            label = app.displayLabel,
            preset = settings.iconPackPreset,
            shape = shape,
            iconDp = iconDp,
            showGlow = settings.showIconBackgroundGlow,
            modifier = modifier
        )
    } else {
        // Real third-party installed app icon styled with user's chosen shape & pack tint
        val bgColors = when (settings.iconPackPreset) {
            IconPackPreset.GALAXY_DARK_GLASS -> listOf(Color(0xFF1E2448), Color(0xFF11152C))
            IconPackPreset.AMOLED_NEON -> listOf(Color(0xFF050508), Color(0xFF0E1324))
            IconPackPreset.PASTEL_MINIMAL -> listOf(Color(0xFFE0E7FF), Color(0xFFC7D2FE))
            IconPackPreset.GOLDEN_LUXURY -> listOf(Color(0xFF272115), Color(0xFF423418))
            IconPackPreset.ONE_UI_9_DEFAULT -> listOf(Color(0xFF262B52), Color(0xFF1A1E3D))
        }
        Box(
            modifier = modifier
                .size(iconDp)
                .shadow(if (settings.showIconBackgroundGlow) 6.dp else 0.dp, shape)
                .clip(shape)
                .background(Brush.linearGradient(bgColors))
                .border(0.8.dp, Color.White.copy(alpha = 0.18f), shape)
                .padding((iconDp.value * 0.12f).dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = app.nativeIcon,
                contentDescription = app.displayLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun OneUiStyledVectorIcon(
    kind: OneUiSystemIconKind,
    label: String,
    preset: IconPackPreset,
    shape: Shape,
    iconDp: Dp,
    showGlow: Boolean,
    modifier: Modifier = Modifier
) {
    val (baseStart, baseEnd, fgTint) = when (kind) {
        OneUiSystemIconKind.PHONE -> Triple(Color(0xFF22C55E), Color(0xFF15803D), Color.White)
        OneUiSystemIconKind.MESSAGES -> Triple(Color(0xFF3B82F6), Color(0xFF1D4ED8), Color.White)
        OneUiSystemIconKind.CONTACTS -> Triple(Color(0xFFF97316), Color(0xFFEA580C), Color.White)
        OneUiSystemIconKind.CAMERA -> Triple(Color(0xFFEF4444), Color(0xFFB91C1C), Color.White)
        OneUiSystemIconKind.GALLERY -> Triple(Color(0xFFEC4899), Color(0xFFBE185D), Color.White)
        OneUiSystemIconKind.CLOCK -> Triple(Color(0xFF4F46E5), Color(0xFF312E81), Color.White)
        OneUiSystemIconKind.CALENDAR -> Triple(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF0F766E))
        OneUiSystemIconKind.SETTINGS -> Triple(Color(0xFF64748B), Color(0xFF334155), Color.White)
        OneUiSystemIconKind.INTERNET -> Triple(Color(0xFF6366F1), Color(0xFF4338CA), Color.White)
        OneUiSystemIconKind.FILES -> Triple(Color(0xFFFBBF24), Color(0xFFD97706), Color.White)
        OneUiSystemIconKind.NOTES -> Triple(Color(0xFFF43F5E), Color(0xFFE11D48), Color.White)
        OneUiSystemIconKind.REMINDERS -> Triple(Color(0xFF38BDF8), Color(0xFF0284C7), Color.White)
        OneUiSystemIconKind.CALCULATOR -> Triple(Color(0xFF4ADE80), Color(0xFF16A34A), Color.White)
        OneUiSystemIconKind.RECORDER -> Triple(Color(0xFFFB7185), Color(0xFFE11D48), Color.White)
        OneUiSystemIconKind.WEATHER -> Triple(Color(0xFF38BDF8), Color(0xFF2563EB), Color.White)
        OneUiSystemIconKind.HEALTH -> Triple(Color(0xFF2DD4BF), Color(0xFF0D9488), Color.White)
        OneUiSystemIconKind.GALAXY_STORE -> Triple(Color(0xFFD946EF), Color(0xFF9333EA), Color.White)
        OneUiSystemIconKind.PLAY_STORE -> Triple(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFF2563EB))
        OneUiSystemIconKind.MAPS -> Triple(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFF16A34A))
        OneUiSystemIconKind.GMAIL -> Triple(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFDC2626))
        OneUiSystemIconKind.GENERIC_APP -> Triple(Color(0xFF6366F1), Color(0xFF4F46E5), Color.White)
    }

    val (finalStart, finalEnd, finalFg) = when (preset) {
        IconPackPreset.ONE_UI_9_DEFAULT -> Triple(baseStart, baseEnd, fgTint)
        IconPackPreset.GALAXY_DARK_GLASS -> Triple(
            Color(0xFF1E2246),
            Color(0xFF12152E),
            if (fgTint == Color.White) baseStart else fgTint
        )
        IconPackPreset.AMOLED_NEON -> Triple(
            Color(0xFF050508),
            Color(0xFF0B0E1A),
            if (fgTint == Color.White) baseStart else fgTint
        )
        IconPackPreset.PASTEL_MINIMAL -> Triple(
            baseStart.copy(alpha = 0.78f),
            baseEnd.copy(alpha = 0.78f),
            Color.White
        )
        IconPackPreset.GOLDEN_LUXURY -> Triple(
            Color(0xFF1F1C18),
            Color(0xFF0F0D0A),
            Color(0xFFFBBF24)
        )
    }

    Box(
        modifier = modifier
            .size(iconDp)
            .shadow(if (showGlow) 6.dp else 0.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(finalStart, finalEnd),
                    start = Offset(0f, 0f),
                    end = Offset(200f, 200f)
                )
            )
            .border(
                width = 1.dp,
                color = if (preset == IconPackPreset.AMOLED_NEON) finalFg.copy(alpha = 0.55f)
                else Color.White.copy(alpha = 0.22f),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        when (kind) {
            OneUiSystemIconKind.CALENDAR -> {
                val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                Text(
                    text = day.toString(),
                    color = finalFg,
                    fontSize = (iconDp.value * 0.40f).sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            OneUiSystemIconKind.GALLERY -> {
                // Iconic Samsung Gallery 6-petal flower
                Canvas(modifier = Modifier.size(iconDp * 0.56f)) {
                    val r = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    for (i in 0 until 6) {
                        val angle = Math.toRadians(i * 60.0)
                        val petalCenter = Offset(
                            center.x + (r * 0.52f * cos(angle)).toFloat(),
                            center.y + (r * 0.52f * sin(angle)).toFloat()
                        )
                        drawCircle(
                            color = finalFg.copy(alpha = 0.92f),
                            radius = r * 0.32f,
                            center = petalCenter
                        )
                    }
                    drawCircle(color = finalEnd, radius = r * 0.20f, center = center)
                }
            }

            OneUiSystemIconKind.PLAY_STORE -> {
                // Colorful Play triangle
                Canvas(modifier = Modifier.size(iconDp * 0.52f)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.18f, size.height * 0.1f)
                        lineTo(size.width * 0.88f, size.height * 0.5f)
                        lineTo(size.width * 0.18f, size.height * 0.9f)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444))
                        )
                    )
                }
            }

            else -> {
                val vector = when (kind) {
                    OneUiSystemIconKind.PHONE -> Icons.Default.Call
                    OneUiSystemIconKind.MESSAGES -> Icons.Default.ChatBubble
                    OneUiSystemIconKind.CONTACTS -> Icons.Default.Person
                    OneUiSystemIconKind.CAMERA -> Icons.Default.CameraAlt
                    OneUiSystemIconKind.CLOCK -> Icons.Default.AccessTime
                    OneUiSystemIconKind.SETTINGS -> Icons.Default.Settings
                    OneUiSystemIconKind.INTERNET -> Icons.Default.Public
                    OneUiSystemIconKind.FILES -> Icons.Default.Folder
                    OneUiSystemIconKind.NOTES -> Icons.Default.Description
                    OneUiSystemIconKind.REMINDERS -> Icons.Default.Notifications
                    OneUiSystemIconKind.CALCULATOR -> Icons.Default.Calculate
                    OneUiSystemIconKind.RECORDER -> Icons.Default.Mic
                    OneUiSystemIconKind.WEATHER -> Icons.Default.Cloud
                    OneUiSystemIconKind.HEALTH -> Icons.Default.Favorite
                    OneUiSystemIconKind.GALAXY_STORE -> Icons.Default.ShoppingBag
                    OneUiSystemIconKind.MAPS -> Icons.Default.LocationOn
                    OneUiSystemIconKind.GMAIL -> Icons.Default.Email
                    else -> Icons.Default.GridView
                }
                Icon(
                    imageVector = vector,
                    contentDescription = label,
                    tint = finalFg,
                    modifier = Modifier.size(iconDp * 0.54f)
                )
            }
        }
    }
}
