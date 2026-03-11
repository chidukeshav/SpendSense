package com.example.spendsense.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    currency: String,
    onCurrencyChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = currency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                onCurrencyChange(it)
                showCurrencyDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionTitle(title = "General")
            SettingsSwitchItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Enable dark theme across the app",
                checked = isDarkMode,
                onCheckedChange = onDarkModeChange
            )
            SettingsSwitchItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Daily budget reminders",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            SettingsSectionTitle(title = "Preferences")
            SettingsClickableItem(
                icon = Icons.Default.Payments,
                title = "Currency",
                subtitle = "Selected: $currency",
                onClick = { showCurrencyDialog = true }
            )
            SettingsClickableItem(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = "English",
                onClick = { /* Language selection */ }
            )

            SettingsSectionTitle(title = "Data Management")
            SettingsClickableItem(
                icon = Icons.Default.Backup,
                title = "Cloud Backup",
                subtitle = "Sync your data with Google Drive",
                onClick = { /* Backup logic */ }
            )
            SettingsClickableItem(
                icon = Icons.Default.DeleteForever,
                title = "Clear All Data",
                subtitle = "This action cannot be undone",
                onClick = { /* Delete logic */ }
            )
            
            SettingsSectionTitle(title = "Support")
            SettingsClickableItem(
                icon = Icons.Default.Help,
                title = "Help Center",
                onClick = { /* Help logic */ }
            )
            SettingsClickableItem(
                icon = Icons.Default.Info,
                title = "About MyMoney",
                subtitle = "Version 1.0.2",
                onClick = { /* Info logic */ }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CurrencySelectionDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val currencies = listOf("$", "€", "£", "₹", "¥")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column {
                currencies.forEach { curr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(curr) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = curr == currentCurrency, onClick = { onSelect(curr) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = curr, fontSize = 18.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
