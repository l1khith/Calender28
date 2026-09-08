package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.AppTheme
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.ui.theme.ThemeManager
import com.l1khith.calender28.utils.AppConfig
import com.l1khith.calender28.utils.UrlLauncher
import com.l1khith.calender28.utils.rememberSecurityLockLauncher

import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileScreen(
    onOpenSubscription: () -> Unit,
    onOpenCustomerCenter: () -> Unit,
    onOpenSecurity: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenFocusStats: () -> Unit = {},
    onOpenCoinStore: () -> Unit = {},
    onOpenExportTasks: () -> Unit = {},
    onOpenMonthView: () -> Unit,
    onOpenSparkyDetail: () -> Unit = {},
    sparkyViewModel: com.l1khith.calender28.viewmodel.SparkyViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.l1khith.calender28.viewmodel.AppViewModelProvider.Factory)
) {
    val isProActive by SubscriptionManager.isProActive.collectAsStateWithLifecycle()
    val sparkyState by sparkyViewModel.sparkyState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val currentTheme by ThemeManager.currentTheme.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAppPreferencesDialog by remember { mutableStateOf(false) }
    val enableSparky by com.l1khith.calender28.utils.AppSettingsManager.enableSparky.collectAsStateWithLifecycle()
    val enableAnimations by com.l1khith.calender28.utils.AppSettingsManager.enableAnimations.collectAsStateWithLifecycle()
    val enableSounds by com.l1khith.calender28.utils.AppSettingsManager.enableSounds.collectAsStateWithLifecycle()

    if (showAppPreferencesDialog) {
        AppPreferencesDialog(
            onDismiss = { showAppPreferencesDialog = false }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            isProActive = isProActive,
            currentTheme = currentTheme,
            onDismiss = { showThemeDialog = false },
            onSelectTheme = { theme ->
                ThemeManager.setTheme(theme)
                showThemeDialog = false
            },
            onOpenPaywall = onOpenSubscription
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixColors.Surface)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section: Sparky Companion (Zero Emoji - Vector Icons only)
        if (enableSparky) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                        imageVector = AppIcons.Sparky,
                        contentDescription = null,
                        tint = MatrixColors.Primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "SPARKY COMPANION",
                        color = MatrixColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MatrixShapes.Lg)
                        .clickable { onOpenSparkyDetail() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MatrixColors.PrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Sparky,
                                        contentDescription = null,
                                        tint = MatrixColors.Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${sparkyState.name}'s Profile",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MatrixColors.TextHeader
                                    )
                                    Text(
                                        text = "${sparkyState.stage.displayName} • Level ${sparkyState.level}",
                                        fontSize = 12.sp,
                                        color = MatrixColors.Primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View",
                                tint = MatrixColors.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalDivider(color = MatrixColors.OutlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFFF43F5E),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Personality: " + sparkyState.dominantTraits.joinToString(", ") { it.displayName },
                                    fontSize = 12.sp,
                                    color = MatrixColors.TextSecondary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MatrixColors.Secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${sparkyState.claimedAchievements.size}/25",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixColors.Secondary
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        // Section: Account Settings
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ACCOUNT SETTINGS",
                    color = MatrixColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AppIcons.Subscription,
                                    contentDescription = "Pro Mode",
                                    tint = MatrixColors.Secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Pro Mode (Testing)",
                                        color = MatrixColors.TextHeader,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isProActive) "Unlocked • Ad-Free" else "Free Tier • Test Ads Shown",
                                        color = MatrixColors.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Switch(
                                checked = isProActive,
                                onCheckedChange = {
                                    SubscriptionManager.toggleProMode(coroutineScope)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MatrixColors.Primary,
                                    checkedTrackColor = MatrixColors.PrimaryContainer
                                )
                            )
                        }

                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)

                        var devToastMessage by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(devToastMessage) {
                            devToastMessage?.let {
                                kotlinx.coroutines.delay(2000)
                                devToastMessage = null
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (SubscriptionManager.onDevModeTap()) {
                                        val nowPro = SubscriptionManager.isProActive.value
                                        devToastMessage = if (nowPro) "Dev Mode: PRO ENABLED" else "Dev Mode: PRO DISABLED"
                                    }
                                    onOpenSubscription()
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AppIcons.Subscription,
                                    contentDescription = "Subscription Status",
                                    tint = MatrixColors.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Subscription Status",
                                    color = MatrixColors.TextHeader,
                                    fontSize = 14.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isProActive) "Pro (Active)" else "Free Tier",
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Go",
                                    tint = MatrixColors.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        devToastMessage?.let { msg ->
                            Text(
                                text = msg,
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        val isAppLockEnabled by com.l1khith.calender28.security.AppLockManager.isAppLockEnabled.collectAsState()
                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)
                        ProfileSubtitleRow(
                            icon = AppIcons.Security,
                            title = "Security & App Lock",
                            subtitle = if (isAppLockEnabled) "Enabled (Biometric/PIN Protected)" else "Disabled (Tap to configure)",
                            onClick = onOpenSecurity
                        )
                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)
                        ProfileSettingRow(
                            icon = AppIcons.Notification,
                            title = "Notifications",
                            onClick = onOpenNotifications
                        )
                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)
                        ProfileSettingRow(
                            icon = Icons.Default.Timer,
                            title = "Focus Analytics & History",
                            onClick = onOpenFocusStats
                        )
                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)
                        ProfileSettingRow(
                            icon = Icons.Default.MonetizationOn,
                            title = "CalCoin Store & Rewards",
                            onClick = onOpenCoinStore
                        )
                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)
                        ProfileSubtitleRow(
                            icon = AppIcons.Sparky,
                            title = "App Preferences",
                            subtitle = "Sparky (${if (enableSparky) "On" else "Off"}) • Animations (${if (enableAnimations) "On" else "Off"}) • Sounds (${if (enableSounds) "On" else "Off"})",
                            onClick = { showAppPreferencesDialog = true }
                        )
                    }
                }
            }
        }

        // Section: APPEARANCE
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "APPEARANCE",
                    color = MatrixColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ProfileValueRow(
                            icon = AppIcons.Appearance,
                            title = "Theme",
                            value = currentTheme.themeName,
                            onClick = { showThemeDialog = true }
                        )
                    }
                }
            }
        }

        // Section: LEGAL & ABOUT
        item {
            val context = LocalContext.current

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "LEGAL & ABOUT",
                    color = MatrixColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Privacy Policy Row
                        ProfileSubtitleRow(
                            icon = Icons.Outlined.Lock,
                            title = "Privacy Policy",
                            subtitle = "Read how your on-device data is protected",
                            onClick = {
                                UrlLauncher.openBrowser(context, AppConfig.PRIVACY_POLICY_URL)
                            }
                        )

                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)

                        // Terms of Service Row
                        ProfileSubtitleRow(
                            icon = Icons.Outlined.Description,
                            title = "Terms of Service",
                            subtitle = "Terms and conditions for using Calender28",
                            onClick = {
                                UrlLauncher.openBrowser(context, AppConfig.TERMS_OF_SERVICE_URL)
                            }
                        )

                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)

                        // Data Management Row
                        ProfileSubtitleRow(
                            icon = Icons.Outlined.Storage,
                            title = "Data Management & Export",
                            subtitle = "Export current month or all tasks to Downloads (CSV/ICS/JSON)",
                            onClick = onOpenExportTasks
                        )

                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)

                        // App Version Info Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "App Version",
                                    tint = MatrixColors.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = AppConfig.APP_NAME,
                                        color = MatrixColors.TextHeader,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "On-device Eisenhower Matrix platform",
                                        color = MatrixColors.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Text(
                                text = AppConfig.APP_BUILD_INFO,
                                color = MatrixColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    isProActive: Boolean,
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onSelectTheme: (AppTheme) -> Unit,
    onOpenPaywall: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select App Theme", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTheme.entries.forEach { theme ->
                    val isSelected = theme == currentTheme
                    val colors = ThemeManager.getColors(theme)
                    Surface(
                        shape = MatrixShapes.Md,
                        color = if (isSelected) MatrixColors.SurfaceContainerHigh else MatrixColors.SurfaceContainerLow,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MatrixColors.Primary else MatrixColors.OutlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (theme.isProOnly && !isProActive) {
                                    onDismiss()
                                    onOpenPaywall()
                                } else {
                                    onSelectTheme(theme)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(colors.surface)
                                            .border(1.dp, Color.Gray, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(colors.primary)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(colors.secondary)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = theme.themeName,
                                    color = MatrixColors.TextHeader,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }

                            if (theme.isProOnly && !isProActive) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                ) {
                                    Text(
                                        text = "PRO",
                                        color = Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MatrixColors.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MatrixColors.Primary)
            }
        },
        containerColor = MatrixColors.SurfaceContainer
    )
}

@Composable
fun ProfileSettingRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MatrixColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = MatrixColors.TextHeader,
                fontSize = 14.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Go",
            tint = MatrixColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ProfileValueRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MatrixColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = MatrixColors.TextHeader,
                fontSize = 14.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = MatrixColors.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go",
                tint = MatrixColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProfileSubtitleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MatrixColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = MatrixColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Go",
            tint = MatrixColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

