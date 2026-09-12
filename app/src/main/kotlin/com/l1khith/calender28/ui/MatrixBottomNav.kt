package com.l1khith.calender28.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.l1khith.calender28.data.BottomTab
import com.l1khith.calender28.ui.theme.MatrixColors

/**
 * Bottom navigation bar component extracted from FixedCalendarApp.
 * Handles tab navigation, banner ad container, and animated separator.
 */
@Composable
fun MatrixBottomNav(
    selectedTab: Int,
    visibleTabs: List<BottomTab>,
    isProActive: Boolean,
    enableAnimations: Boolean,
    monthNavIcon: ImageVector,
    tasksNavIcon: ImageVector,
    habitNavIcon: ImageVector,
    notesNavIcon: ImageVector,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        if (!isProActive) {
            BannerAd(modifier = Modifier.fillMaxWidth())
        }
        AnimatedSparkDivider(
            baseColor = MatrixColors.OutlineVariant,
            sparkColor = MatrixColors.Primary,
            glowColor = MatrixColors.Secondary,
            height = 1.dp,
            reverseDirection = false,
            durationMillis = 4000,
            enableSparkle = enableAnimations
        )
        NavigationBar(
            containerColor = MatrixColors.Surface,
            contentColor = MatrixColors.OnSurface,
            tonalElevation = 0.dp
        ) {
            visibleTabs.forEach { tab ->
                val icon = when (tab) {
                    BottomTab.MONTH -> monthNavIcon
                    BottomTab.TASKS -> tasksNavIcon
                    BottomTab.HABIT -> habitNavIcon
                    BottomTab.NOTES -> notesNavIcon
                }
                NavigationBarItem(
                    selected = selectedTab == tab.tabIndex,
                    onClick = { onNavigateToTab(tab.tabIndex) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label
                        )
                    },
                    label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MatrixColors.Primary,
                        selectedTextColor = MatrixColors.Primary,
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MatrixColors.OnSurfaceVariant,
                        unselectedTextColor = MatrixColors.OnSurfaceVariant
                    )
                )
            }
        }
    }
}
