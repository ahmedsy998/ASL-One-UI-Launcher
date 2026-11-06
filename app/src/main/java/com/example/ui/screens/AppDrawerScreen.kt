package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.OneUiAppIconView
import com.example.model.InstalledApp
import com.example.model.LauncherSettings
import com.example.ui.theme.parseHexColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    allApps: List<InstalledApp>,
    settings: LauncherSettings,
    onLaunchApp: (InstalledApp) -> Unit,
    onAddAppToHome: (InstalledApp) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onRequestUninstall: (String) -> Unit,
    onToggleHideApp: (String, Boolean) -> Unit,
    onCustomizeSingleApp: (String, String, String, String) -> Unit,
    onChangeSortMode: (String) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showHiddenApps by remember { mutableStateOf(false) }
    var selectedFolderFilter by remember { mutableStateOf("الكل") }
    var selectedAppForMenu by remember { mutableStateOf<InstalledApp?>(null) }
    var editingApp by remember { mutableStateOf<InstalledApp?>(null) }

    // State for single-app icon & label customization dialog
    var editLabelText by remember { mutableStateOf("") }
    var editIconUri by remember { mutableStateOf("") }
    var editFolderText by remember { mutableStateOf("") }

    val customIconPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            editIconUri = uri.toString()
        }
    }

    val drawerFolders = remember(allApps) {
        val customFolders = allApps.mapNotNull { it.drawerFolder?.takeIf { f -> f.isNotBlank() } }.distinct()
        listOf("الكل", "الأحدث", "الأكثر استخداماً") + customFolders
    }

    val visibleApps = remember(allApps, searchQuery, showHiddenApps, selectedFolderFilter, settings.drawerSortMode) {
        val base = allApps.filter { app ->
            if (showHiddenApps) true else !app.isHidden
        }.filter { app ->
            if (searchQuery.isBlank()) true
            else app.displayLabel.contains(searchQuery, ignoreCase = true) ||
                app.labelEn.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)
        }.filter { app ->
            when (selectedFolderFilter) {
                "الكل" -> true
                "الأحدث" -> app.lastLaunchedAt > 0L || !app.isSystemShortcut
                "الأكثر استخداماً" -> app.launchCount > 0 || app.isSystemShortcut
                else -> app.drawerFolder == selectedFolderFilter
            }
        }

        when {
            selectedFolderFilter == "الأكثر استخداماً" || settings.drawerSortMode == "MOST_USED" ->
                base.sortedByDescending { it.launchCount }
            selectedFolderFilter == "الأحدث" || settings.drawerSortMode == "RECENT" ->
                base.sortedByDescending { it.lastLaunchedAt }
            else -> base.sortedBy { it.displayLabel }
        }
    }

    val labelColor = parseHexColor(settings.iconLabelColorHex, Color.White)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E22).copy(alpha = settings.drawerBackgroundAlpha))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("app_drawer_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Search Bar matching Screen 4 of the reference image ("البحث عن التطبيقات")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "البحث عن التطبيقات",
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color(0xFFCBD5E1)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.White)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1E2342).copy(alpha = 0.9f),
                        unfocusedContainerColor = Color(0xFF1E2342).copy(alpha = 0.8f),
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .testTag("drawer_search_input")
                )

                // Sort & Hidden Apps Toggle Button
                IconButton(
                    onClick = {
                        val nextSort = when (settings.drawerSortMode) {
                            "ALPHABETICAL" -> "MOST_USED"
                            "MOST_USED" -> "RECENT"
                            else -> "ALPHABETICAL"
                        }
                        onChangeSortMode(nextSort)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E2342))
                ) {
                    Icon(
                        imageVector = Icons.Default.SortByAlpha,
                        contentDescription = "فرز التطبيقات",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { showHiddenApps = !showHiddenApps },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (showHiddenApps) Color(0xFF7C3AED) else Color(0xFF1E2342))
                ) {
                    Icon(
                        imageVector = if (showHiddenApps) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "إظهار التطبيقات المخفية",
                        tint = Color.White
                    )
                }
            }

            // Drawer Category / Folder Pills
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(drawerFolders) { folder ->
                    val selected = selectedFolderFilter == folder
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) Color(0xFF3B82F6) else Color(0xFF1B203E))
                            .clickable { selectedFolderFilter = folder }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (folder !in listOf("الكل", "الأحدث", "الأكثر استخداماً")) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = folder,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Grid of Installed Applications (matches Screen 4 of reference image)
            LazyVerticalGrid(
                columns = GridCells.Fixed(settings.drawerGridColumns.coerceIn(3, 6)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(settings.iconSpacingDp.dp + 8.dp),
                horizontalArrangement = Arrangement.spacedBy(settings.iconSpacingDp.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(visibleApps, key = { it.packageName }) { app ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .combinedClickable(
                                onClick = { onLaunchApp(app) },
                                onLongClick = { selectedAppForMenu = app }
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Box {
                            OneUiAppIconView(
                                app = app,
                                settings = settings
                            )
                            if (app.isHidden) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "مخفي",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        if (settings.showDrawerLabels) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = app.displayLabel,
                                color = labelColor,
                                fontSize = settings.iconLabelSizeSp.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Page indicator dots at bottom of App Drawer (matching Screen 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f))
                )
            }
        }

        // Long-press App Context Menu Dialog
        selectedAppForMenu?.let { app ->
            AlertDialog(
                onDismissRequest = { selectedAppForMenu = null },
                containerColor = Color(0xFF191E3C),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OneUiAppIconView(app = app, settings = settings, sizeOverrideDp = 46.dp)
                        Column {
                            Text(app.displayLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(app.labelEn, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        }
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DrawerMenuOptionRow(
                            icon = Icons.Default.AddToHomeScreen,
                            title = "إضافة إلى الشاشة الرئيسية",
                            tint = Color(0xFF38BDF8)
                        ) {
                            onAddAppToHome(app)
                            selectedAppForMenu = null
                        }
                        DrawerMenuOptionRow(
                            icon = Icons.Default.Edit,
                            title = "تخصيص الأيقونة والاسم والمجلد",
                            tint = Color(0xFFA855F7)
                        ) {
                            editLabelText = app.displayLabel
                            editIconUri = app.customIconUri ?: ""
                            editFolderText = app.drawerFolder ?: ""
                            editingApp = app
                            selectedAppForMenu = null
                        }
                        DrawerMenuOptionRow(
                            icon = if (app.isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            title = if (app.isHidden) "إظهار التطبيق في الدرج" else "إخفاء التطبيق من الدرج",
                            tint = Color(0xFFFBBF24)
                        ) {
                            onToggleHideApp(app.packageName, !app.isHidden)
                            selectedAppForMenu = null
                        }
                        DrawerMenuOptionRow(
                            icon = Icons.Default.Info,
                            title = "معلومات التطبيق (النظام)",
                            tint = Color(0xFF10B981)
                        ) {
                            onOpenAppInfo(app.packageName)
                            selectedAppForMenu = null
                        }
                        if (!app.isSystemShortcut) {
                            DrawerMenuOptionRow(
                                icon = Icons.Default.DeleteOutline,
                                title = "إلغاء تثبيت التطبيق",
                                tint = Color(0xFFEF4444)
                            ) {
                                onRequestUninstall(app.packageName)
                                selectedAppForMenu = null
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedAppForMenu = null }) {
                        Text("إغلاق", color = Color(0xFF94A3B8))
                    }
                }
            )
        }

        // Single App Customization Dialog (Custom Gallery Icon + Custom Label + Drawer Folder)
        editingApp?.let { app ->
            AlertDialog(
                onDismissRequest = { editingApp = null },
                containerColor = Color(0xFF191E3C),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("تخصيص ${app.displayLabel}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editLabelText,
                            onValueChange = { editLabelText = it },
                            label = { Text("اسم التطبيق المخصص") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = editFolderText,
                            onValueChange = { editFolderText = it },
                            label = { Text("مجلد درج التطبيقات (اختياري)") },
                            placeholder = { Text("مثال: تواصل، أدوات، ألعاب") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    customIconPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("صورة من الاستوديو")
                            }
                            if (editIconUri.isNotBlank()) {
                                IconButton(onClick = { editIconUri = "" }) {
                                    Icon(Icons.Default.Restore, contentDescription = "استعادة الأيقونة الأصلية", tint = Color(0xFFF43F5E))
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onCustomizeSingleApp(app.packageName, editLabelText, editIconUri, editFolderText)
                            editingApp = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("حفظ التغييرات")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingApp = null }) {
                        Text("إلغاء", color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}

@Composable
private fun DrawerMenuOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF232952))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = tint)
        Text(text = title, color = Color.White, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
