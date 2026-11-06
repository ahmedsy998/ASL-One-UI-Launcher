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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.BuiltInWidgetType
import com.example.model.LauncherSettings
import com.example.ui.components.BuiltInOneUiWidgetCard
import com.example.widget.SystemWidgetProviderItem

@Composable
fun WidgetGallerySheet(
    settings: LauncherSettings,
    systemWidgets: List<SystemWidgetProviderItem>,
    onAddBuiltInWidget: (BuiltInWidgetType) -> Unit,
    onAddSystemWidget: (SystemWidgetProviderItem) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    var selectedCategory by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "سامسونج", "الساعة", "الطقس", "النظام (${systemWidgets.size})")

    val filteredBuiltIn = remember(selectedCategory, searchQuery) {
        BuiltInWidgetType.entries.filter { w ->
            val matchesCat = selectedCategory == "الكل" || w.categoryAr == selectedCategory
            val matchesQuery = searchQuery.isBlank() ||
                w.titleAr.contains(searchQuery, ignoreCase = true) ||
                w.descriptionAr.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A0D1E), Color(0xFF131735), Color(0xFF0B0E21))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("widget_gallery_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header matching Screen 2 of reference image: Search on right, Title + Back arrow on left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("widget_gallery_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "الويدجيت",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { showSearchBar = !showSearchBar },
                    modifier = Modifier.testTag("widget_search_toggle")
                ) {
                    Icon(
                        imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "بحث في الويدجيت",
                        tint = Color.White
                    )
                }
            }

            if (showSearchBar) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث عن ويدجيت الساعة، الطقس، التقويم...", color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1A1F3D),
                        unfocusedContainerColor = Color(0xFF1A1F3D),
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Category Filter Pills matching Screen 2 (الكل | سامسونج | الساعة | الطقس)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) Color(0xFF2563EB)
                                else Color(0xFF1E2342)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (!selectedCategory.startsWith("النظام")) {
                    // Featured Layout matching Screen 2 of Reference Image when "الكل" or "سامسونج" is active
                    if (selectedCategory == "الكل" && searchQuery.isBlank()) {
                        item {
                            // Hero Clock & Weather Dual Card
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddBuiltInWidget(BuiltInWidgetType.CLOCK_WEATHER_DUAL)
                                        onDismiss()
                                    }
                            ) {
                                BuiltInOneUiWidgetCard(
                                    type = BuiltInWidgetType.CLOCK_WEATHER_DUAL,
                                    settings = settings,
                                    bgColorHex = "#1E40AF",
                                    alpha = 0.95f,
                                    isHomeHeroTransparent = false
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "الساعة والطقس (اضغط للإضافة)",
                                    color = Color(0xFFCBD5E1),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        item {
                            // 2x2 Grid matching Screen 2: Analog Clock + Calendar Month
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(175.dp)
                                        .clickable {
                                            onAddBuiltInWidget(BuiltInWidgetType.ANALOG_CLOCK)
                                            onDismiss()
                                        }
                                ) {
                                    BuiltInOneUiWidgetCard(
                                        type = BuiltInWidgetType.ANALOG_CLOCK,
                                        settings = settings,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(175.dp)
                                        .clickable {
                                            onAddBuiltInWidget(BuiltInWidgetType.CALENDAR_MONTH)
                                            onDismiss()
                                        }
                                ) {
                                    BuiltInOneUiWidgetCard(
                                        type = BuiltInWidgetType.CALENDAR_MONTH,
                                        settings = settings,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        item {
                            // Second 2x2 Row matching Screen 2: Battery Ring + Music Wave
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(165.dp)
                                        .clickable {
                                            onAddBuiltInWidget(BuiltInWidgetType.BATTERY_RING)
                                            onDismiss()
                                        }
                                ) {
                                    BuiltInOneUiWidgetCard(
                                        type = BuiltInWidgetType.BATTERY_RING,
                                        settings = settings,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(165.dp)
                                        .clickable {
                                            onAddBuiltInWidget(BuiltInWidgetType.MUSIC_WAVE)
                                            onDismiss()
                                        }
                                ) {
                                    BuiltInOneUiWidgetCard(
                                        type = BuiltInWidgetType.MUSIC_WAVE,
                                        settings = settings,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    // All remaining / filtered One UI 9 Built-in Widgets
                    items(filteredBuiltIn) { widgetType ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF181C38))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = widgetType.titleAr,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${widgetType.descriptionAr} (${widgetType.defaultSpanX}×${widgetType.defaultSpanY})",
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Button(
                                    onClick = {
                                        onAddBuiltInWidget(widgetType)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("إضافة", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            BuiltInOneUiWidgetCard(
                                type = widgetType,
                                settings = settings,
                                isHomeHeroTransparent = false
                            )
                        }
                    }
                }

                // Real Android AppWidgetManager System Widgets Section
                item {
                    Text(
                        text = "ويدجيت تطبيقات النظام المثبتة (AppWidgetHost)",
                        color = Color(0xFFA855F7),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (systemWidgets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF181C38))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد ويدجيت خارجية إضافية مسجلة حالياً على هذا الجهاز",
                                color = Color(0xFF94A3B8),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(systemWidgets) { sysWidget ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF181C38))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .clickable {
                                    onAddSystemWidget(sysWidget)
                                    onDismiss()
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF312E81)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Widgets,
                                        contentDescription = null,
                                        tint = Color(0xFF818CF8)
                                    )
                                }
                                Column {
                                    Text(
                                        text = sysWidget.widgetLabel,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${sysWidget.appLabel} • الحجم ${sysWidget.minCellsX}×${sysWidget.minCellsY}",
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    onAddSystemWidget(sysWidget)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("إضافة")
                            }
                        }
                    }
                }
            }
        }
    }
}
