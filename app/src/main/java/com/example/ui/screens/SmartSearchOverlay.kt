package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.engine.OneUiAppIconView
import com.example.model.InstalledApp
import com.example.model.LauncherSettings
import com.example.viewmodel.ContactSearchResult

private data class SystemShortcutItem(
    val titleAr: String,
    val action: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun SmartSearchOverlay(
    allApps: List<InstalledApp>,
    contacts: List<ContactSearchResult>,
    settings: LauncherSettings,
    onLaunchApp: (InstalledApp) -> Unit,
    onRequestLoadContacts: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasContactsPermission = granted
        if (granted) onRequestLoadContacts()
    }

    LaunchedEffect(hasContactsPermission) {
        if (hasContactsPermission) {
            onRequestLoadContacts()
        }
    }

    val matchingApps = remember(allApps, query) {
        if (query.isBlank()) {
            allApps.filter { !it.isHidden }.take(8)
        } else {
            allApps.filter {
                !it.isHidden && (
                    it.displayLabel.contains(query, ignoreCase = true) ||
                        it.labelEn.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
                    )
            }
        }
    }

    val matchingContacts = remember(contacts, query, hasContactsPermission) {
        if (!hasContactsPermission || query.isBlank()) emptyList()
        else contacts.filter {
            it.name.contains(query, ignoreCase = true) || it.phoneNumber.contains(query)
        }.take(10)
    }

    val systemShortcuts = remember {
        listOf(
            SystemShortcutItem("إعدادات Wi-Fi", Settings.ACTION_WIFI_SETTINGS, Icons.Default.Wifi, Color(0xFF3B82F6)),
            SystemShortcutItem("البلوتوث", Settings.ACTION_BLUETOOTH_SETTINGS, Icons.Default.Bluetooth, Color(0xFF6366F1)),
            SystemShortcutItem("الشاشة والسطوع", Settings.ACTION_DISPLAY_SETTINGS, Icons.Default.DisplaySettings, Color(0xFFF59E0B)),
            SystemShortcutItem("الصوت والاهتزاز", Settings.ACTION_SOUND_SETTINGS, Icons.Default.VolumeUp, Color(0xFFEC4899)),
            SystemShortcutItem("استهلاك البطارية", Intent.ACTION_POWER_USAGE_SUMMARY, Icons.Default.BatteryChargingFull, Color(0xFF10B981))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF20A0D1E), Color(0xFA131735))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("smart_search_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "إغلاق البحث",
                        tint = Color.White
                    )
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("ابحث بالعربية أو الإنجليزية في التطبيقات وجهات الاتصال...", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح", tint = Color.White)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1E2342),
                        unfocusedContainerColor = Color(0xFF1E2342),
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.16f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("smart_search_input")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Applications Section
                item {
                    Text(
                        text = if (query.isBlank()) "التطبيقات المقترحة والأخيرة" else "نتائج التطبيقات (${matchingApps.size})",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        items(matchingApps, key = { it.packageName }) { app ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        onLaunchApp(app)
                                        onDismiss()
                                    }
                                    .padding(8.dp)
                            ) {
                                OneUiAppIconView(app = app, settings = settings, sizeOverrideDp = 54.dp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = app.displayLabel,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // System Shortcuts Section
                item {
                    Text(
                        text = "اختصارات النظام السريعة",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(systemShortcuts) { sc ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF1C2142))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                                    .clickable {
                                        runCatching {
                                            context.startActivity(Intent(sc.action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(sc.color.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(sc.icon, contentDescription = null, tint = sc.color, modifier = Modifier.size(18.dp))
                                }
                                Text(sc.titleAr, color = Color.White, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }

                // Contacts Search Section (Permission-gated per Requirement 11)
                item {
                    Text(
                        text = "البحث في جهات الاتصال",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!hasContactsPermission) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1A1F3D))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Contacts, contentDescription = null, tint = Color(0xFFA855F7))
                                Column {
                                    Text(
                                        text = "تفعيل البحث في جهات الاتصال",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "يتم البحث محلياً فقط عند منح صلاحية قراءة جهات الاتصال",
                                        color = Color(0xFF94A3B8),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("منح الإذن")
                            }
                        }
                    }
                } else if (matchingContacts.isNotEmpty()) {
                    items(matchingContacts, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1A1F3D))
                                .clickable {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phoneNumber}"))
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(contact.name, color = Color.White, style = MaterialTheme.typography.titleSmall)
                                Text(contact.phoneNumber, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.Call, contentDescription = "اتصال", tint = Color(0xFF22C55E))
                        }
                    }
                }
            }
        }
    }
}
