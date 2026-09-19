package com.sahed.money_tracker.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sahed.money_tracker.R
import com.sahed.money_tracker.data.preferences.ThemeMode
import com.sahed.money_tracker.ui.components.CurrencyPickerDialog
import com.sahed.money_tracker.ui.designsystem.components.EmeraldAlertDialog
import com.sahed.money_tracker.ui.designsystem.components.EmeraldModalBottomSheet
import com.sahed.money_tracker.ui.designsystem.components.EmeraldOptionRow
import com.sahed.money_tracker.ui.designsystem.components.bouncyClickable
import com.sahed.money_tracker.ui.designsystem.components.gentleEntrance
import com.sahed.money_tracker.ui.components.BuiltBySahedFooter
import com.sahed.money_tracker.ui.components.OtherAppsDialog
import com.sahed.money_tracker.ui.designsystem.theme.DialogShape
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldPalette
import com.sahed.money_tracker.ui.designsystem.theme.EmeraldTheme
import com.sahed.money_tracker.util.AppVersionHelper
import com.sahed.money_tracker.util.DateUtils
import com.sahed.money_tracker.viewmodel.SettingsUiState
import com.sahed.money_tracker.viewmodel.SettingsViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToManageSources: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val versionName = remember(context) { AppVersionHelper.getVersionName(context) }

    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showOtherAppsDialog by remember { mutableStateOf(false) }

    // Allocation Edit Dialog
    var showAllocationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.allocationSuccess) {
        if (uiState.allocationSuccess) {
            Toast.makeText(context, "Allocation settings updated!", Toast.LENGTH_SHORT).show()
            showAllocationDialog = false
            viewModel.clearAllocationStatus()
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            onDismissRequest = { showCurrencyPicker = false },
            onCurrencySelected = { currency ->
                viewModel.updateCurrencyOnly(currency.code, currency.symbol)
                showCurrencyPicker = false
            }
        )
    }

    if (showThemePicker) {
        EmeraldModalBottomSheet(
            onDismissRequest = { showThemePicker = false },
            title = "Select Theme"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    ThemeMode.DARK to "Dark",
                    ThemeMode.LIGHT to "Light",
                    ThemeMode.SYSTEM to "System default"
                ).forEach { (mode, label) ->
                    EmeraldOptionRow(
                        label = label,
                        isSelected = uiState.themeMode == mode,
                        accentColor = EmeraldPalette.SoftEmerald,
                        onClick = {
                            viewModel.setThemeMode(mode)
                            showThemePicker = false
                        }
                    )
                }
            }
        }
    }

    if (showSignOutConfirm) {
        EmeraldAlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            onConfirm = {
                showSignOutConfirm = false
                viewModel.signOut()
                onSignOut()
            },
            title = "Sign Out",
            message = "Are you sure you want to sign out from Money Tracker App?",
            confirmText = "Sign Out",
            cancelText = "Cancel",
            isDestructive = true
        )
    }

    if (showAllocationDialog) {
        AllocationEditDialog(
            currentSettings = uiState.allocationSettings,
            errorMessage = uiState.allocationErrorMessage,
            isSaving = uiState.isSavingAllocation,
            onSave = { saving, sLabel, invest, iLabel, donate, dLabel, rLabel ->
                viewModel.updateAllocation(saving, sLabel, invest, iLabel, donate, dLabel, rLabel)
            },
            onDismissRequest = {
                viewModel.clearAllocationStatus()
                showAllocationDialog = false
            }
        )
    }

    if (showOtherAppsDialog) {
        OtherAppsDialog(
            onDismissRequest = { showOtherAppsDialog = false }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.gentleEntrance(0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Google Profile Card (Read-Only)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .gentleEntrance(1)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.userProfile.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = uiState.userProfile.photoUrl,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(EmeraldPalette.SoftEmerald.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = EmeraldPalette.SoftEmerald,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.userProfile.name.ifBlank { "Google User" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.userProfile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. App Settings Section
            SectionHeader(title = "App Settings", modifier = Modifier.gentleEntrance(2))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .gentleEntrance(2)
            ) {
                Column {
                    val currencyOption = remember(uiState.userProfile.currencyCode) {
                        com.sahed.money_tracker.util.CountriesData.allCurrencies.find { it.code == uiState.userProfile.currencyCode }
                    }
                    val currencySubtitle = if (currencyOption != null) {
                        "${uiState.userProfile.currencySymbol} ${currencyOption.name}"
                    } else if (uiState.userProfile.currencyCode.isNotBlank()) {
                        "${uiState.userProfile.currencySymbol} ${uiState.userProfile.currencyCode}"
                    } else {
                        "Not set"
                    }

                    // Currency row (in down show currency icon and name)
                    SettingsItem(
                        icon = Icons.Default.Payments,
                        title = "Currency",
                        subtitle = currencySubtitle,
                        onClick = { showCurrencyPicker = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Theme row (popup like sex option not directly)
                    SettingsItem(
                        icon = Icons.Default.SettingsBrightness,
                        title = "Theme",
                        subtitle = when (uiState.themeMode) {
                            ThemeMode.DARK -> "Dark"
                            ThemeMode.LIGHT -> "Light"
                            ThemeMode.SYSTEM -> "System default"
                        },
                        onClick = { showThemePicker = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Notifications row with Timezone
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = EmeraldPalette.SoftEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Notifications",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Timezone: ${uiState.timeZone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldPalette.SoftEmerald
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Allocation Settings row
                    SettingsItem(
                        icon = Icons.Default.PieChart,
                        title = "Allocation Settings",
                        subtitle = "${uiState.allocationSettings.savingLabel} ${uiState.allocationSettings.savingPercent.toInt()}%, ${uiState.allocationSettings.investLabel} ${uiState.allocationSettings.investPercent.toInt()}%, ${uiState.allocationSettings.donateLabel} ${uiState.allocationSettings.donatePercent.toInt()}%",
                        onClick = { showAllocationDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    // Manage Sources row
                    SettingsItem(
                        icon = Icons.Default.Category,
                        title = "Manage Sources",
                        subtitle = "Main sources & sub-sources",
                        onClick = onNavigateToManageSources
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Account Section
            SectionHeader(title = "Account", modifier = Modifier.gentleEntrance(3))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .gentleEntrance(3)
            ) {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconTint = EmeraldPalette.ErrorRed,
                    title = "Sign Out",
                    subtitle = "Disconnect from Cloud",
                    onClick = { showSignOutConfirm = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Support Section (Optional App-support Donation)
            SectionHeader(title = "Support Money Tracker App", modifier = Modifier.gentleEntrance(4))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .gentleEntrance(4)
            ) {
                Column {
                    // Other Android Apps row (opens modal popup)
                    SettingsItem(
                        icon = Icons.Default.Apps,
                        title = "Other Android Apps",
                        subtitle = "By Sahed Alom Sumit",
                        onClick = { showOtherAppsDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "If you benefit from this ad-free app, you may support it for maintenance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://donate.stripe.com/7sY9AS57S4XL7F4aqP8AE03"))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Donate with Stripe", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 5. Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .gentleEntrance(5),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Version $versionName",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "© ${DateUtils.getCurrentYear()} Sahed Alom Sumit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BuiltBySahedFooter(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sahedalomsumit.com"))
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = EmeraldPalette.SoftEmerald,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    iconTint: Color = EmeraldPalette.SoftEmerald
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(pressedScale = 0.98f, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ThemeSelectionRow(
    currentTheme: ThemeMode,
    onThemeSelect: (ThemeMode) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SettingsBrightness,
                contentDescription = null,
                tint = EmeraldPalette.SoftEmerald,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOptionChip(
                label = "Dark",
                icon = Icons.Default.DarkMode,
                selected = currentTheme == ThemeMode.DARK,
                onClick = { onThemeSelect(ThemeMode.DARK) },
                modifier = Modifier.weight(1f)
            )
            ThemeOptionChip(
                label = "Light",
                icon = Icons.Default.LightMode,
                selected = currentTheme == ThemeMode.LIGHT,
                onClick = { onThemeSelect(ThemeMode.LIGHT) },
                modifier = Modifier.weight(1f)
            )
            ThemeOptionChip(
                label = "System",
                icon = Icons.Default.SettingsBrightness,
                selected = currentTheme == ThemeMode.SYSTEM,
                onClick = { onThemeSelect(ThemeMode.SYSTEM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) EmeraldPalette.SoftEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "themeChipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) EmeraldPalette.SoftEmerald else Color.Transparent,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "themeChipBorder"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) EmeraldPalette.SoftEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "themeChipIcon"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) EmeraldPalette.SoftEmerald else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "themeChipText"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun AllocationEditDialog(
    currentSettings: com.sahed.money_tracker.data.model.AllocationSettings,
    errorMessage: String?,
    isSaving: Boolean,
    onSave: (Double, String, Double, String, Double, String, String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var savingText by remember { mutableStateOf(currentSettings.savingPercent.toInt().toString()) }
    var savingLabel by remember { mutableStateOf(currentSettings.savingLabel) }

    var investText by remember { mutableStateOf(currentSettings.investPercent.toInt().toString()) }
    var investLabel by remember { mutableStateOf(currentSettings.investLabel) }

    var donateText by remember { mutableStateOf(currentSettings.donatePercent.toInt().toString()) }
    var donateLabel by remember { mutableStateOf(currentSettings.donateLabel) }

    var restLabel by remember { mutableStateOf(currentSettings.restLabel) }

    val saving = savingText.toDoubleOrNull() ?: 0.0
    val invest = investText.toDoubleOrNull() ?: 0.0
    val donate = donateText.toDoubleOrNull() ?: 0.0
    val sum = saving + invest + donate
    val restPercent = (100.0 - sum).coerceAtLeast(0.0)
    val isOver100 = sum > 100.0

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Edit Allocation Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Configure your global percentage allocations and category labels.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Inline Error Warning if > 100%
                if (isOver100 || errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldPalette.ErrorRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPalette.ErrorRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = EmeraldPalette.ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "Total percentages sum to ${String.format(java.util.Locale.US, "%.1f", sum)}% (cannot exceed 100%).",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldPalette.ErrorRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Saving Category
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = savingLabel,
                        onValueChange = { savingLabel = it },
                        label = { Text("Label") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = savingText,
                        onValueChange = { savingText = it },
                        label = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Investing Category
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = investLabel,
                        onValueChange = { investLabel = it },
                        label = { Text("Label") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = investText,
                        onValueChange = { investText = it },
                        label = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Donate Category
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = donateLabel,
                        onValueChange = { donateLabel = it },
                        label = { Text("Label") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = donateText,
                        onValueChange = { donateText = it },
                        label = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rest Category (Auto-calculated, read-only percent)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = restLabel,
                        onValueChange = { restLabel = it },
                        label = { Text("Rest Label") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = "${String.format(java.util.Locale.US, "%.1f", restPercent)}%",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Auto %") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(saving, savingLabel, invest, investLabel, donate, donateLabel, restLabel)
                },
                enabled = !isOver100 && !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPalette.SoftEmerald),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel", color = EmeraldTheme.extended.subText) }
        },
        shape = DialogShape,
        containerColor = EmeraldTheme.extended.surfaceTier2
    )
}
