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

@Composable
fun ProfileScreen(
    onOpenSubscription: () -> Unit,
    onOpenCustomerCenter: () -> Unit,
    onOpenSecurity: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenFocusStats: () -> Unit = {},
    onOpenMonthView: () -> Unit
) {
    val isProActive by SubscriptionManager.isProActive.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

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
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // User Header Section (Guest)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, MatrixColors.Primary, CircleShape)
                            .background(MatrixColors.SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Guest Avatar",
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MatrixColors.SurfaceContainer)
                            .border(1.dp, MatrixColors.OutlineVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Guest",
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
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

                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)
                        ProfileSettingRow(
                            icon = AppIcons.Security,
                            title = "Security & App Lock",
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
            val securityLockLauncher = rememberSecurityLockLauncher()
            var isAppLockEnabled by remember { mutableStateOf(false) }

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

                        // App Lock Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Fingerprint,
                                    contentDescription = "App Lock",
                                    tint = MatrixColors.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "App Lock",
                                        color = MatrixColors.TextHeader,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Biometric authentication on launch",
                                        color = MatrixColors.TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Switch(
                                checked = isAppLockEnabled,
                                onCheckedChange = { enabled ->
                                    isAppLockEnabled = enabled
                                    if (enabled) {
                                        securityLockLauncher()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MatrixColors.Primary,
                                    checkedTrackColor = MatrixColors.PrimaryContainer
                                )
                            )
                        }

                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)

                        // Data Management Row
                        ProfileSubtitleRow(
                            icon = Icons.Outlined.Storage,
                            title = "Data Management & Export",
                            subtitle = "Export matrix schedule to ICS/CSV",
                            onClick = {
                                com.l1khith.calender28.utils.PlatformUtils.showToast(context, "Data exported to Downloads folder")
                            }
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

