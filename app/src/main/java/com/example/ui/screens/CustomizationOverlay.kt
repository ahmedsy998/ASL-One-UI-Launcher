package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine. resolveIconShape
import com.example.model.IconPackPreset
import com.example.model.IconShapeStyle
import com.example.model.LauncherSettings
import com.example.model.PageTransitionEffect

private enum class CustomSubPanel {
    NONE,
    GRID_LAYOUT,
    ICONS_STYLE,
    TRANSITIONS
}

@Composable
fun CustomizationOverlay(
    settings: LauncherSettings,
    currentPage: Int,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onOpenWallpapers: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    livePagePreviewContent: @Composable (Int) -> Unit
) {
    BackHandler {
        onDismiss()
    }

    var activeSubPanel by remember { mutableStateOf(CustomSubPanel.NONE) }
    var previewPageIdx by remember { mutableIntStateOf(currentPage.coerceIn(0, (settings.homePageCount - 1).coerceAtLeast(0))) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD90A0D1E))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("customization_overlay_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar: Default Home Screen indicator icon + Add/Remove Page controls (matches Screen 5 top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onUpdateSettings { it.copy(homePageCount = (it.homePageCount + 1).coerceAtMost(7)) }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E2342))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة صفحة جديدة", tint = Color.White)
                }

                // Set as Default Home Page Icon (House icon at top center of Screen 5)
                IconButton(
                    onClick = {
                        onUpdateSettings { it.copy(defaultHomePage = previewPageIdx) }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (settings.defaultHomePage == previewPageIdx) Color(0xFF3B82F6)
                            else Color(0xFF1E2342)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "تعيين كصفحة رئيسية افتراضية",
                        tint = Color.White
                    )
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تم", style = MaterialTheme.typography.labelLarge)
                }
            }

            // Center Scaled-Down Live Home Screen Card with Side Peeking Cards (matches Screen 5 of reference image)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Right edge peeking card (in RTL)
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .fillMaxHeight(0.75f)
                        .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                        .clickable {
                            if (previewPageIdx > 0) previewPageIdx--
                        }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Main Center Framed Preview Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(0.88f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFF141838).copy(alpha = 0.72f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(30.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Delete Page / Trash icon inside frame (matches Screen 5)
                        if (settings.homePageCount > 1) {
                            IconButton(
                                onClick = {
                                    onUpdateSettings {
                                        val nextCount = (it.homePageCount - 1).coerceAtLeast(1)
                                        if (previewPageIdx >= nextCount) previewPageIdx = nextCount - 1
                                        it.copy(homePageCount = nextCount)
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.35f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "حذف الصفحة",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            livePagePreviewContent(previewPageIdx)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Left edge peeking card (in RTL)
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .fillMaxHeight(0.75f)
                        .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                        .clickable {
                            if (previewPageIdx < settings.homePageCount - 1) previewPageIdx++
                        }
                )
            }

            // Multi-Page Dots Indicator (matches Screen 5)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                for (i in 0 until settings.homePageCount) {
                    val active = i == previewPageIdx
                    Box(
                        modifier = Modifier
                            .size(if (active) 9.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) Color.White else Color.White.copy(alpha = 0.38f))
                            .clickable { previewPageIdx = i }
                    )
                }
            }

            // Dynamic Sub-Panel for Grid, Icon Customization, or Transitions (Bottom panels of reference image)
            if (activeSubPanel != CustomSubPanel.NONE) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF171B3A))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(24.dp))
                        .padding(14.dp)
                ) {
                    when (activeSubPanel) {
                        CustomSubPanel.GRID_LAYOUT -> GridCustomizationSubPanel(settings, onUpdateSettings)
                        CustomSubPanel.ICONS_STYLE -> IconCustomizationSubPanel(settings, onUpdateSettings)
                        CustomSubPanel.TRANSITIONS -> TransitionsCustomizationSubPanel(settings, onUpdateSettings)
                        CustomSubPanel.NONE -> {}
                    }
                }
            }

            // Bottom Quick Action Glass Cards Row matching Screen 5:
            // الخلفيات | الويدجيت | التخطيط | الأيقونات | الحركات | الإعدادات
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    CustomizationBottomCard(
                        title = "الخلفيات",
                        icon = Icons.Default.Wallpaper,
                        isSelected = false,
                        onClick = onOpenWallpapers
                    )
                }
                item {
                    CustomizationBottomCard(
                        title = "الويدجيت",
                        icon = Icons.Default.Widgets,
                        isSelected = false,
                        onClick = onOpenWidgets
                    )
                }
                item {
                    CustomizationBottomCard(
                        title = "التخطيط",
                        icon = Icons.Default.GridView,
                        isSelected = activeSubPanel == CustomSubPanel.GRID_LAYOUT,
                        onClick = {
                            activeSubPanel = if (activeSubPanel == CustomSubPanel.GRID_LAYOUT) CustomSubPanel.NONE
                            else CustomSubPanel.GRID_LAYOUT
                        }
                    )
                }
                item {
                    CustomizationBottomCard(
                        title = "الأيقونات",
                        icon = Icons.Default.AutoAwesome,
                        isSelected = activeSubPanel == CustomSubPanel.ICONS_STYLE,
                        onClick = {
                            activeSubPanel = if (activeSubPanel == CustomSubPanel.ICONS_STYLE) CustomSubPanel.NONE
                            else CustomSubPanel.ICONS_STYLE
                        }
                    )
                }
                item {
                    CustomizationBottomCard(
                        title = "الحركات",
                        icon = Icons.Default.Animation,
                        isSelected = activeSubPanel == CustomSubPanel.TRANSITIONS,
                        onClick = {
                            activeSubPanel = if (activeSubPanel == CustomSubPanel.TRANSITIONS) CustomSubPanel.NONE
                            else CustomSubPanel.TRANSITIONS
                        }
                    )
                }
                item {
                    CustomizationBottomCard(
                        title = "الإعدادات",
                        icon = Icons.Default.Settings,
                        isSelected = false,
                        onClick = onOpenSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomizationBottomCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(74.dp)
            .height(78.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Color(0xFF4F46E5)
                else Color(0xFF1F2447).copy(alpha = 0.88f)
            )
            .border(
                1.dp,
                if (isSelected) Color(0xFF818CF8) else Color.White.copy(alpha = 0.16f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GridCustomizationSubPanel(
    settings: LauncherSettings,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit
) {
    val gridPresets = listOf(
        4 to 5,
        4 to 6,
        5 to 5,
        5 to 6
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "تخطيط مرن للشاشة (عدد الصفوف والأعمدة والمسافات)",
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            gridPresets.forEach { (cols, rows) ->
                val active = settings.homeGridColumns == cols && settings.homeGridRows == rows
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (active) Color(0xFF3B82F6) else Color(0xFF232852))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
                        .clickable {
                            onUpdateSettings { it.copy(homeGridColumns = cols, homeGridRows = rows) }
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Mini visual grid preview matching bottom-right of reference image
                    Canvas(modifier = Modifier.size(32.dp)) {
                        val cellW = size.width / cols
                        val cellH = size.height / rows
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(c * cellW + 2f, r * cellH + 2f),
                                    size = Size(cellW - 4f, cellH - 4f),
                                    cornerRadius = CornerRadius(3f, 3f),
                                    style = Stroke(width = 1.5f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$cols×$rows",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("المسافة بين الأيقونات: ${settings.iconSpacingDp}dp", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.labelMedium)
            Slider(
                value = settings.iconSpacingDp.toFloat(),
                onValueChange = { v -> onUpdateSettings { it.copy(iconSpacingDp = v.toInt()) } },
                valueRange = 2f..18f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF3B82F6)),
                modifier = Modifier.width(180.dp)
            )
        }
    }
}

@Composable
private fun IconCustomizationSubPanel(
    settings: LauncherSettings,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "تخصيص الأيقونات (الشكل والحجم وحزم الأيقونات)",
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(IconShapeStyle.entries) { shapeStyle ->
                val selected = settings.iconShape == shapeStyle
                val previewShape = resolveIconShape(shapeStyle, settings.customCornerRadiusDp)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) Color(0xFF4F46E5) else Color(0xFF232852))
                        .clickable { onUpdateSettings { it.copy(iconShape = shapeStyle) } }
                        .padding(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(previewShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFA855F7), Color(0xFF3B82F6))
                                )
                            )
                            .border(1.5.dp, Color.White, previewShape)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = shapeStyle.titleAr.substringBefore(" "),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حجم الأيقونة: ${settings.iconSizeDp}dp", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.labelMedium)
            Slider(
                value = settings.iconSizeDp.toFloat(),
                onValueChange = { v -> onUpdateSettings { it.copy(iconSizeDp = v.toInt()) } },
                valueRange = 44f..72f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFA855F7), activeTrackColor = Color(0xFF7C3AED)),
                modifier = Modifier.width(180.dp)
            )
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(IconPackPreset.entries) { pack ->
                val active = settings.iconPackPreset == pack
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (active) Color(0xFF7C3AED) else Color(0xFF232852))
                        .clickable { onUpdateSettings { it.copy(iconPackPreset = pack) } }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(pack.titleAr, color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun TransitionsCustomizationSubPanel(
    settings: LauncherSettings,
    onUpdateSettings: ((LauncherSettings) -> LauncherSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "حركات وانتقالات سلسة (120Hz)",
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PageTransitionEffect.entries) { effect ->
                val active = settings.pageTransitionEffect == effect
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) Color(0xFF2563EB) else Color(0xFF232852))
                        .clickable { onUpdateSettings { it.copy(pageTransitionEffect = effect) } }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(effect.titleAr, color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "سرعة الحركة: ${String.format("%.1fx", settings.animationSpeedMultiplier)}",
                color = Color(0xFFCBD5E1),
                style = MaterialTheme.typography.labelMedium
            )
            Slider(
                value = settings.animationSpeedMultiplier,
                onValueChange = { v -> onUpdateSettings { it.copy(animationSpeedMultiplier = v) } },
                valueRange = 0.5f..2.0f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF2563EB)),
                modifier = Modifier.width(180.dp)
            )
        }
    }
}
