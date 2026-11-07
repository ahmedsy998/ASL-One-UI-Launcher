package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.HomeItemEntity
import com.example.engine.OneUiAppIconView
import com.example.engine.resolveIconShape
import com.example.model.BuiltInWidgetType
import com.example.model.FolderAppEntry
import com.example.model.GestureAction
import com.example.model.HomeItemType
import com.example.model.InstalledApp
import com.example.model.LauncherSettings
import com.example.model.OneUiSystemIconKind
import com.example.model.PageTransitionEffect
import com.example.ui.components.BuiltInOneUiWidgetCard
import com.example.ui.components.SystemAppWidgetCard
import com.example.ui.theme.parseHexColor
import com.example.widget.LauncherWidgetHost
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    pagerState: PagerState,
    settings: LauncherSettings,
    homeItems: List<HomeItemEntity>,
    allApps: List<InstalledApp>,
    isDefaultHome: Boolean,
    widgetHost: LauncherWidgetHost,
    parseFolderApps: (String) -> List<FolderAppEntry>,
    onExecuteGesture: (GestureAction) -> Unit,
    onLaunchApp: (InstalledApp) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenWidgets: () -> Unit,
    onOpenCustomization: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDefaultLauncherPicker: () -> Unit,
    onRemoveHomeItem: (HomeItemEntity) -> Unit,
    onMoveItemToPage: (HomeItemEntity, Int) -> Unit,
    onSwapHomeItems: (HomeItemEntity, HomeItemEntity) -> Unit,
    onUpdateWidgetAppearance: (HomeItemEntity, Int, Int, String, Float) -> Unit,
    onUpdateFolder: (HomeItemEntity, String, List<FolderAppEntry>, String) -> Unit,
    onCreateFolder: (String, List<InstalledApp>, Int) -> Unit,
    onOpenAppInfo: (String) -> Unit
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var selectedHomeItemForMenu by remember { mutableStateOf<HomeItemEntity?>(null) }
    var draggingOrSwappingItem by remember { mutableStateOf<HomeItemEntity?>(null) }
    var openedFolderItem by remember { mutableStateOf<HomeItemEntity?>(null) }
    var editingWidgetItem by remember { mutableStateOf<HomeItemEntity?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    val appsByPkg = remember(allApps) { allApps.associateBy { it.packageName } }
    val dockItems = remember(homeItems, settings.dockIconCount) {
        homeItems.filter { it.isDock }.take(settings.dockIconCount)
    }

    var verticalDragDelta by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(settings) {
                detectTapGestures(
                    onDoubleTap = {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onExecuteGesture(settings.doubleTapGesture)
                    },
                    onLongPress = {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onExecuteGesture(settings.longPressGesture)
                    }
                )
            }
            .pointerInput(settings) {
                detectVerticalDragGestures(
                    onDragStart = { verticalDragDelta = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        verticalDragDelta += dragAmount
                    },
                    onDragEnd = {
                        if (verticalDragDelta < -95f) {
                            onExecuteGesture(settings.swipeUpGesture)
                        } else if (verticalDragDelta > 95f) {
                            onExecuteGesture(settings.swipeDownGesture)
                        }
                        verticalDragDelta = 0f
                    }
                )
            }
            .testTag("main_home_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top One UI 9 Interactive Mode Bar + Default Launcher Status Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Mode Pills to inspect all 5 screens from the reference image effortlessly
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        QuickTopPill(
                            label = "الإعدادات",
                            icon = Icons.Default.Settings,
                            tag = "quick_open_settings",
                            onClick = onOpenSettings
                        )
                    }
                    item {
                        QuickTopPill(
                            label = "الويدجيت",
                            icon = Icons.Default.Widgets,
                            tag = "quick_open_widgets",
                            onClick = onOpenWidgets
                        )
                    }
                    item {
                        QuickTopPill(
                            label = "الدرج",
                            icon = Icons.Default.Apps,
                            tag = "quick_open_drawer",
                            onClick = onOpenAppDrawer
                        )
                    }
                    item {
                        QuickTopPill(
                            label = "التخصيص",
                            icon = Icons.Default.AutoAwesome,
                            tag = "quick_open_customization",
                            onClick = onOpenCustomization
                        )
                    }
                    item {
                        QuickTopPill(
                            label = "بحث",
                            icon = Icons.Default.Search,
                            tag = "quick_open_search",
                            onClick = onOpenSearch
                        )
                    }
                }

                if (!isDefaultHome) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF10B981).copy(alpha = 0.9f))
                            .clickable { onOpenDefaultLauncherPicker() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("set_default_home_banner")
                    ) {
                        Text(
                            text = "تعيين كافتراضي",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Multi-Page Home Screen Pager with Configurable 3D / Slide / Stack / Cube Animations
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val pageOffset = (
                    (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                ).coerceIn(-1f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val absOffset = pageOffset.absoluteValue
                            when (settings.pageTransitionEffect) {
                                PageTransitionEffect.ONE_UI_SLIDE -> {
                                    alpha = 1f - (absOffset * 0.15f)
                                }
                                PageTransitionEffect.CAROUSEL_3D -> {
                                    rotationY = pageOffset * -28f
                                    val s = 1f - (absOffset * 0.14f)
                                    scaleX = s
                                    scaleY = s
                                    alpha = 1f - (absOffset * 0.25f)
                                    cameraDistance = 14f * density
                                }
                                PageTransitionEffect.DEPTH_STACK -> {
                                    val s = 1f - (absOffset * 0.22f)
                                    scaleX = s
                                    scaleY = s
                                    alpha = 1f - (absOffset * 0.45f)
                                }
                                PageTransitionEffect.CUBE_ROTATION -> {
                                    rotationY = pageOffset * -65f
                                    cameraDistance = 18f * density
                                }
                                PageTransitionEffect.ACCORDION_FOLD -> {
                                    scaleX = 1f - (absOffset * 0.35f)
                                    alpha = 1f - (absOffset * 0.2f)
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    HomePageContent(
                        pageIndex = pageIndex,
                        settings = settings,
                        pageItems = homeItems.filter { !it.isDock && it.page == pageIndex },
                        appsByPkg = appsByPkg,
                        widgetHost = widgetHost,
                        parseFolderApps = parseFolderApps,
                        onLaunchApp = { clickedApp ->
                            val swapping = draggingOrSwappingItem
                            if (swapping != null) {
                                val targetEntity = homeItems.firstOrNull {
                                    !it.isDock && it.page == pageIndex && it.packageName == clickedApp.packageName
                                }
                                if (targetEntity != null && targetEntity.id != swapping.id) {
                                    onSwapHomeItems(swapping, targetEntity)
                                }
                                draggingOrSwappingItem = null
                            } else {
                                onLaunchApp(clickedApp)
                            }
                        },
                        onOpenFolder = { folderItem ->
                            val swapping = draggingOrSwappingItem
                            if (swapping != null && swapping.id != folderItem.id) {
                                onSwapHomeItems(swapping, folderItem)
                                draggingOrSwappingItem = null
                            } else {
                                openedFolderItem = folderItem
                            }
                        },
                        onOpenSearch = onOpenSearch,
                        onOpenSettings = onOpenSettings,
                        onLongPressItem = { item ->
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            val swapping = draggingOrSwappingItem
                            if (swapping != null && swapping.id != item.id) {
                                onSwapHomeItems(swapping, item)
                                draggingOrSwappingItem = null
                            } else {
                                selectedHomeItemForMenu = item
                            }
                        }
                    )
                }
            }

            // Page Indicator Dots (matching Screen 3 of the reference image)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until settings.homePageCount) {
                    val isCurrent = pagerState.currentPage == i
                    val isDefaultPage = settings.defaultHomePage == i
                    if (isDefaultPage && isCurrent) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "الصفحة الرئيسية",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(14.dp)
                                .clickable { scope.launch { pagerState.animateScrollToPage(i) } }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isCurrent) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) Color.White else Color.White.copy(alpha = 0.40f))
                                .clickable { scope.launch { pagerState.animateScrollToPage(i) } }
                        )
                    }
                }
            }

            // Bottom Dock matching Screen 3 of the reference image (Phone, Contacts, Messages, Camera)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF141838).copy(alpha = 0.42f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(28.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp)
                    .testTag("home_bottom_dock"),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val labelColor = parseHexColor(settings.iconLabelColorHex, Color.White)
                dockItems.forEach { dockEntity ->
                    val resolvedApp = resolveInstalledApp(dockEntity, appsByPkg)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = { onLaunchApp(resolvedApp) },
                                onLongClick = {
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    selectedHomeItemForMenu = dockEntity
                                }
                            )
                    ) {
                        OneUiAppIconView(
                            app = resolvedApp,
                            settings = settings,
                            sizeOverrideDp = (settings.iconSizeDp + 2).dp
                        )
                        if (settings.showDockLabels) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = resolvedApp.displayLabel,
                                color = labelColor,
                                fontSize = (settings.iconLabelSizeSp - 1f).sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Expanded One UI 9 Folder Modal
        openedFolderItem?.let { folderEntity ->
            val currentFolderEntity = homeItems.firstOrNull { it.id == folderEntity.id } ?: folderEntity
            val entries = parseFolderApps(currentFolderEntity.folderAppsJson)
            OneUiExpandedFolderModal(
                folderEntity = currentFolderEntity,
                entries = entries,
                allApps = allApps,
                appsByPkg = appsByPkg,
                settings = settings,
                onLaunchApp = { app ->
                    openedFolderItem = null
                    onLaunchApp(app)
                },
                onSaveFolder = { newTitle, newEntries, newColorHex ->
                    onUpdateFolder(currentFolderEntity, newTitle, newEntries, newColorHex)
                },
                onDismiss = { openedFolderItem = null }
            )
        }

        // Long-Press Item Context Menu (for Apps, Folders, and Widgets on Home Screen)
        selectedHomeItemForMenu?.let { item ->
            AlertDialog(
                onDismissRequest = { selectedHomeItemForMenu = null },
                containerColor = Color(0xFF181C3A),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = {
                    Text(
                        text = item.title.ifBlank { "عنصر الشاشة الرئيسية" },
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (item.itemType == HomeItemType.BUILTIN_WIDGET.name ||
                            item.itemType == HomeItemType.SYSTEM_WIDGET.name
                        ) {
                            ContextMenuButton(
                                icon = Icons.Default.Tune,
                                label = "تغيير حجم ولون وشفافية الويدجيت",
                                tint = Color(0xFF38BDF8)
                            ) {
                                editingWidgetItem = item
                                selectedHomeItemForMenu = null
                            }
                        }

                        if (item.itemType == HomeItemType.APP.name) {
                            ContextMenuButton(
                                icon = Icons.Default.Info,
                                label = "معلومات التطبيق",
                                tint = Color(0xFF10B981)
                            ) {
                                onOpenAppInfo(item.packageName)
                                selectedHomeItemForMenu = null
                            }
                        }

                        ContextMenuButton(
                            icon = Icons.Default.CreateNewFolder,
                            label = "إنشاء مجلد جديد على هذه الصفحة",
                            tint = Color(0xFFFBBF24)
                        ) {
                            selectedHomeItemForMenu = null
                            showCreateFolderDialog = true
                        }

                        ContextMenuButton(
                            icon = Icons.Default.SwapHoriz,
                            label = "سحب أو تبديل موضع العنصر على الشبكة",
                            tint = Color(0xFF38BDF8)
                        ) {
                            draggingOrSwappingItem = item
                            selectedHomeItemForMenu = null
                        }

                        if (!item.isDock && settings.homePageCount > 1) {
                            val nextPage = (item.page + 1) % settings.homePageCount
                            ContextMenuButton(
                                icon = Icons.Default.SwapHoriz,
                                label = "نقل إلى الصفحة رقم ${nextPage + 1}",
                                tint = Color(0xFFA855F7)
                            ) {
                                onMoveItemToPage(item, nextPage)
                                selectedHomeItemForMenu = null
                            }
                        }

                        ContextMenuButton(
                            icon = Icons.Default.DeleteOutline,
                            label = "إزالة من الشاشة الرئيسية",
                            tint = Color(0xFFEF4444)
                        ) {
                            onRemoveHomeItem(item)
                            selectedHomeItemForMenu = null
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedHomeItemForMenu = null }) {
                        Text("إغلاق", color = Color(0xFF94A3B8))
                    }
                }
            )
        }

        // Widget Resize & Styling Modal
        editingWidgetItem?.let { wItem ->
            var spanX by remember { mutableIntStateOf(wItem.spanX) }
            var spanY by remember { mutableIntStateOf(wItem.spanY) }
            var alpha by remember { mutableFloatStateOf(wItem.widgetAlpha) }
            var bgHex by remember { mutableStateOf(wItem.widgetBgColorHex) }

            AlertDialog(
                onDismissRequest = { editingWidgetItem = null },
                containerColor = Color(0xFF181C3A),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("تخصيص وتغيير حجم الويدجيت") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("العرض (عدد الأعمدة): $spanX")
                        Slider(
                            value = spanX.toFloat(),
                            onValueChange = { spanX = it.toInt() },
                            valueRange = 1f..4f
                        )
                        Text("الارتفاع (عدد الصفوف): $spanY")
                        Slider(
                            value = spanY.toFloat(),
                            onValueChange = { spanY = it.toInt() },
                            valueRange = 1f..4f
                        )
                        Text("شفافية الخلفية: ${(alpha * 100).toInt()}%")
                        Slider(
                            value = alpha,
                            onValueChange = { alpha = it },
                            valueRange = 0.25f..1.0f
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("#1E293B", "#1E40AF", "#4C1D95", "#065F46", "#000000").forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(hex))
                                        .border(
                                            width = if (bgHex == hex) 2.dp else 1.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable { bgHex = hex }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onUpdateWidgetAppearance(wItem, spanX, spanY, bgHex, alpha)
                            editingWidgetItem = null
                        }
                    ) {
                        Text("تطبيق")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingWidgetItem = null }) {
                        Text("إلغاء", color = Color(0xFF94A3B8))
                    }
                }
            )
        }

        // Create New Home Folder Dialog
        if (showCreateFolderDialog) {
            var folderTitle by remember { mutableStateOf("مجلد جديد") }
            val selectedPkgs = remember { mutableStateOf(setOf("oneui.shortcut.gallery", "oneui.shortcut.files")) }

            AlertDialog(
                onDismissRequest = { showCreateFolderDialog = false },
                containerColor = Color(0xFF181C3A),
                titleContentColor = Color.White,
                textContentColor = Color.White,
                title = { Text("إنشاء مجلد One UI 9") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = folderTitle,
                            onValueChange = { folderTitle = it },
                            label = { Text("اسم المجلد") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Text("اختر التطبيقات داخل المجلد:", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(allApps.take(12), key = { it.packageName }) { app ->
                                val picked = app.packageName in selectedPkgs.value
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (picked) Color(0xFF3B82F6) else Color(0xFF232852))
                                        .clickable {
                                            val next = selectedPkgs.value.toMutableSet()
                                            if (!next.add(app.packageName)) next.remove(app.packageName)
                                            selectedPkgs.value = next
                                        }
                                        .padding(8.dp)
                                ) {
                                    OneUiAppIconView(app = app, settings = settings, sizeOverrideDp = 40.dp)
                                    Text(app.displayLabel, fontSize = 10.sp, color = Color.White, maxLines = 1)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val chosenApps = allApps.filter { it.packageName in selectedPkgs.value }
                            onCreateFolder(folderTitle, chosenApps, pagerState.currentPage)
                            showCreateFolderDialog = false
                        }
                    ) {
                        Text("إنشاء المجلد")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolderDialog = false }) {
                        Text("إلغاء", color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun HomePageContent(
    pageIndex: Int,
    settings: LauncherSettings,
    pageItems: List<HomeItemEntity>,
    appsByPkg: Map<String, InstalledApp>,
    widgetHost: LauncherWidgetHost,
    parseFolderApps: (String) -> List<FolderAppEntry>,
    onLaunchApp: (InstalledApp) -> Unit,
    onOpenFolder: (HomeItemEntity) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onLongPressItem: (HomeItemEntity) -> Unit,
    isCompactPreview: Boolean = false
) {
    val widgets = pageItems.filter {
        it.itemType == HomeItemType.BUILTIN_WIDGET.name ||
            it.itemType == HomeItemType.SYSTEM_WIDGET.name
    }
    val iconsAndFolders = pageItems.filter {
        it.itemType == HomeItemType.APP.name ||
            it.itemType == HomeItemType.FOLDER.name
    }

    val labelColor = parseHexColor(settings.iconLabelColorHex, Color.White)
    val scaleFactor = if (isCompactPreview) 0.72f else 1.0f
    val iconDp: Dp = (settings.iconSizeDp * scaleFactor).dp

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top / Middle Widgets Area
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy((10 * scaleFactor).dp)
        ) {
            // Group 2x2 widgets side-by-side if spanX <= 2
            val fullWidthWidgets = widgets.filter { it.spanX >= 3 }
            val halfWidthWidgets = widgets.filter { it.spanX <= 2 }

            fullWidthWidgets.forEach { wItem ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { onLongPressItem(wItem) }
                        )
                ) {
                    RenderWidgetItem(
                        wItem = wItem,
                        pageIndex = pageIndex,
                        settings = settings,
                        widgetHost = widgetHost,
                        onOpenSearch = onOpenSearch,
                        onOpenSettings = onOpenSettings
                    )
                }
            }

            halfWidthWidgets.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { wItem ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height((wItem.spanY * 82 * scaleFactor).dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { onLongPressItem(wItem) }
                                )
                        ) {
                            RenderWidgetItem(
                                wItem = wItem,
                                pageIndex = pageIndex,
                                settings = settings,
                                widgetHost = widgetHost,
                                onOpenSearch = onOpenSearch,
                                onOpenSettings = onOpenSettings,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Bottom App Icons & Folders Grid Area (matches Screen 3 of the reference image)
        val cols = settings.homeGridColumns.coerceIn(3, 6)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = (6 * scaleFactor).dp),
            verticalArrangement = Arrangement.spacedBy((settings.iconSpacingDp * scaleFactor + 6).dp)
        ) {
            iconsAndFolders.chunked(cols).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowItems.forEach { item ->
                        if (item.itemType == HomeItemType.FOLDER.name) {
                            val entries = parseFolderApps(item.folderAppsJson)
                            OneUiFolderHomeIcon(
                                title = item.title,
                                folderColorHex = item.folderColorHex,
                                entries = entries,
                                appsByPkg = appsByPkg,
                                settings = settings,
                                iconDp = iconDp,
                                showLabel = settings.showHomeLabels && !isCompactPreview,
                                labelColor = labelColor,
                                onClick = { onOpenFolder(item) },
                                onLongClick = { onLongPressItem(item) }
                            )
                        } else {
                            val app = resolveInstalledApp(item, appsByPkg)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width((iconDp.value + 18).dp)
                                    .combinedClickable(
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = { onLongPressItem(item) }
                                    )
                            ) {
                                OneUiAppIconView(
                                    app = app,
                                    settings = settings,
                                    sizeOverrideDp = iconDp
                                )
                                if (settings.showHomeLabels && !isCompactPreview) {
                                    Spacer(modifier = Modifier.height(5.dp))
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
                    // Pad remaining columns for consistent grid alignment
                    repeat((cols - rowItems.size).coerceAtLeast(0)) {
                        Spacer(modifier = Modifier.width((iconDp.value + 18).dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderWidgetItem(
    wItem: HomeItemEntity,
    pageIndex: Int,
    settings: LauncherSettings,
    widgetHost: LauncherWidgetHost,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (wItem.itemType == HomeItemType.SYSTEM_WIDGET.name) {
        SystemAppWidgetCard(
            widgetHost = widgetHost,
            appWidgetId = wItem.systemAppWidgetId,
            modifier = modifier
        )
    } else {
        val type = runCatching {
            BuiltInWidgetType.valueOf(wItem.builtInWidgetType)
        }.getOrDefault(BuiltInWidgetType.CLOCK_WEATHER_DUAL)

        BuiltInOneUiWidgetCard(
            type = type,
            settings = settings,
            bgColorHex = wItem.widgetBgColorHex,
            alpha = wItem.widgetAlpha,
            isHomeHeroTransparent = (pageIndex == 0 && type == BuiltInWidgetType.CLOCK_WEATHER_DUAL),
            onOpenSearch = onOpenSearch,
            onOpenWeatherEdit = onOpenSettings,
            onOpenClockApp = {},
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OneUiFolderHomeIcon(
    title: String,
    folderColorHex: String,
    entries: List<FolderAppEntry>,
    appsByPkg: Map<String, InstalledApp>,
    settings: LauncherSettings,
    iconDp: Dp,
    showLabel: Boolean,
    labelColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = resolveIconShape(settings.iconShape, settings.customCornerRadiusDp)
    val folderColor = parseHexColor(folderColorHex, Color(0xFF334155))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width((iconDp.value + 18).dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(iconDp)
                .clip(shape)
                .background(folderColor.copy(alpha = 0.78f))
                .border(1.dp, Color.White.copy(alpha = 0.28f), shape)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            // 2x2 mini icons inside folder squircle (matches "Google" folder on Screen 3)
            val miniSize = (iconDp.value * 0.34f).dp
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                entries.take(4).chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        row.forEach { entry ->
                            val kind = runCatching {
                                OneUiSystemIconKind.valueOf(entry.iconKindName)
                            }.getOrDefault(OneUiSystemIconKind.GENERIC_APP)
                            val app = appsByPkg[entry.packageName] ?: InstalledApp(
                                packageName = entry.packageName,
                                activityName = "",
                                labelAr = entry.labelAr,
                                labelEn = entry.labelAr,
                                iconKind = kind,
                                isSystemShortcut = true
                            )
                            OneUiAppIconView(
                                app = app,
                                settings = settings.copy(showIconBackgroundGlow = false),
                                sizeOverrideDp = miniSize
                            )
                        }
                    }
                }
            }
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = title,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OneUiExpandedFolderModal(
    folderEntity: HomeItemEntity,
    entries: List<FolderAppEntry>,
    allApps: List<InstalledApp>,
    appsByPkg: Map<String, InstalledApp>,
    settings: LauncherSettings,
    onLaunchApp: (InstalledApp) -> Unit,
    onSaveFolder: (String, List<FolderAppEntry>, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember(folderEntity) { mutableStateOf(folderEntity.title) }
    var colorHex by remember(folderEntity) { mutableStateOf(folderEntity.folderColorHex) }
    var currentEntries by remember(entries) { mutableStateOf(entries) }
    var showAddAppsPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC090C1E))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(34.dp))
                .background(parseHexColor(colorHex, Color(0xFF1E2448)).copy(alpha = 0.94f))
                .border(1.5.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(34.dp))
                .clickable(enabled = false) {}
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        onSaveFolder(it, currentEntries, colorHex)
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Folder Color Palette Picker
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("#334155", "#3B82F6", "#7C3AED", "#10B981", "#EC4899").forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(1.dp, Color.White, CircleShape)
                                .clickable {
                                    colorHex = hex
                                    onSaveFolder(title, currentEntries, hex)
                                }
                        )
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                maxItemsInEachRow = 4
            ) {
                currentEntries.forEach { entry ->
                    val kind = runCatching {
                        OneUiSystemIconKind.valueOf(entry.iconKindName)
                    }.getOrDefault(OneUiSystemIconKind.GENERIC_APP)
                    val app = appsByPkg[entry.packageName] ?: InstalledApp(
                        packageName = entry.packageName,
                        activityName = "",
                        labelAr = entry.labelAr,
                        labelEn = entry.labelAr,
                        iconKind = kind,
                        isSystemShortcut = true
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(68.dp)
                            .clickable { onLaunchApp(app) }
                    ) {
                        OneUiAppIconView(app = app, settings = settings, sizeOverrideDp = 52.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = app.displayLabel,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // "+" Add App to Folder button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(68.dp)
                        .clickable { showAddAppsPicker = !showAddAppsPicker }
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة تطبيق", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("إضافة", color = Color.White, fontSize = 11.sp)
                }
            }

            if (showAddAppsPicker) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(allApps, key = { it.packageName }) { candidate ->
                        val inFolder = currentEntries.any { it.packageName == candidate.packageName }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (inFolder) Color(0xFF2563EB) else Color.Black.copy(alpha = 0.25f))
                                .clickable {
                                    val updated = if (inFolder) {
                                        currentEntries.filterNot { it.packageName == candidate.packageName }
                                    } else {
                                        currentEntries + FolderAppEntry(
                                            candidate.packageName,
                                            candidate.displayLabel,
                                            candidate.iconKind.name
                                        )
                                    }
                                    currentEntries = updated
                                    onSaveFolder(title, updated, colorHex)
                                }
                                .padding(6.dp)
                        ) {
                            OneUiAppIconView(app = candidate, settings = settings, sizeOverrideDp = 36.dp)
                            Text(candidate.displayLabel, color = Color.White, fontSize = 9.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTopPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF151936).copy(alpha = 0.62f))
            .border(0.8.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(13.dp))
        Text(text = label, color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ContextMenuButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF242A54))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint)
        Text(text = label, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}

fun resolveInstalledApp(
    entity: HomeItemEntity,
    appsByPkg: Map<String, InstalledApp>
): InstalledApp {
    appsByPkg[entity.packageName]?.let { found ->
        return if (entity.customIconUri.isNotBlank()) {
            found.copy(customIconUri = entity.customIconUri)
        } else found
    }
    val kind = runCatching {
        OneUiSystemIconKind.valueOf(entity.iconKindName)
    }.getOrDefault(OneUiSystemIconKind.GENERIC_APP)
    return InstalledApp(
        packageName = entity.packageName,
        activityName = "",
        labelAr = entity.title,
        labelEn = entity.title,
        iconKind = kind,
        isSystemShortcut = true,
        customIconUri = entity.customIconUri.takeIf { it.isNotBlank() }
    )
}
