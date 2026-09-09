package com.l1khith.calender28.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.utils.AppSettingsManager
import com.l1khith.calender28.ui.theme.MatrixColors

/**
 * Enumeration of the primary navigation destinations.
 */
enum class NavigationTab(
    val title: String,
    val contentDescription: String
) {
    MONTH("Month", "Calendar Month View"),
    TASKS("Tasks", "Tasks Checklist"),
    HABIT("Habit", "28-Day Habit Tracker"),
    NOTES("Notes", "Notes and Reflections");

    companion object {
        fun fromIndex(index: Int): NavigationTab = entries.getOrElse(index) { MONTH }
    }
}

/**
 * Configuration model for an individual navigation tab item.
 */
data class NavigationTabItem(
    val tab: NavigationTab,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val label: String = tab.title,
    val contentDescription: String = tab.contentDescription
)

/**
 * Default tab item configurations using vector icons (zero text emojis).
 */
fun defaultNavigationTabItems(): List<NavigationTabItem> = listOf(
    NavigationTabItem(
        tab = NavigationTab.MONTH,
        icon = Icons.Outlined.CalendarMonth,
        label = "Month",
        contentDescription = "Month"
    ),
    NavigationTabItem(
        tab = NavigationTab.TASKS,
        icon = Icons.Outlined.CheckCircle,
        label = "Tasks",
        contentDescription = "Tasks"
    ),
    NavigationTabItem(
        tab = NavigationTab.HABIT,
        icon = Icons.Outlined.Whatshot,
        label = "Habit",
        contentDescription = "Habit"
    ),
    NavigationTabItem(
        tab = NavigationTab.NOTES,
        icon = Icons.Outlined.Description,
        label = "Notes",
        contentDescription = "Notes"
    )
)

/**
 * Floating, pill-shaped bottom navigation bar with a detached circular Floating Action Button (FAB).
 *
 * @param selectedTab The currently active [NavigationTab].
 * @param onTabSelected Callback invoked when a tab in the pill is tapped.
 * @param onAddClicked Callback invoked when the detached circular FAB is tapped.
 * @param modifier Modifier for external positioning (margins, insets).
 * @param items List of tab definitions (default is 4 items: Month, Tasks, Habit, Notes).
 * @param containerColor Background color of the navigation pill container (default #1E1E1E).
 * @param borderColor Subtle outline border color for the pill container.
 * @param activeIndicatorColor Highlight background color for the active tab pill.
 * @param selectedColor Accent color for selected tab icon and label.
 * @param unselectedColor Color for unselected tab icon and label.
 * @param fabContainerColor Color for the circular FAB container.
 * @param fabContentColor Color for the FAB's plus icon.
 * @param elevation Drop shadow elevation for both the pill and FAB.
 */
@Composable
fun FloatingBottomNavBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    onAddClicked: () -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavigationTabItem> = remember { defaultNavigationTabItems() },
    containerColor: Color = Color(0xFF1E1E1E),
    borderColor: Color = Color(0xFF2E323D),
    activeIndicatorColor: Color = MatrixColors.Primary.copy(alpha = 0.18f),
    selectedColor: Color = MatrixColors.Primary,
    unselectedColor: Color = Color(0xFF94A3B8),
    fabContainerColor: Color = MatrixColors.Primary,
    fabContentColor: Color = MatrixColors.OnPrimary,
    elevation: Dp = 8.dp
) {
    val enableAnimations by AppSettingsManager.enableAnimations.collectAsStateWithLifecycle()
    val transitionDuration = if (enableAnimations) 250 else 0

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Navigation Pill Container
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(percent = 50),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.6f)
                ),
            shape = RoundedCornerShape(percent = 50),
            color = containerColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.tab == selectedTab
                    NavigationPillItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = { onTabSelected(item.tab) },
                        activeIndicatorColor = activeIndicatorColor,
                        selectedColor = selectedColor,
                        unselectedColor = unselectedColor,
                        transitionDuration = transitionDuration,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Detached Circular Action Button (FAB)
        Surface(
            onClick = onAddClicked,
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = elevation,
                    shape = CircleShape,
                    ambientColor = MatrixColors.Primary.copy(alpha = 0.3f),
                    spotColor = MatrixColors.Primary.copy(alpha = 0.5f)
                ),
            shape = CircleShape,
            color = fabContainerColor,
            contentColor = fabContentColor,
            border = BorderStroke(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = fabContentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * An individual navigation tab item inside the navigation pill.
 * Features an animated capsule indicator, icon on top, and concise label below.
 */
@Composable
private fun NavigationPillItem(
    item: NavigationTabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeIndicatorColor: Color,
    selectedColor: Color,
    unselectedColor: Color,
    transitionDuration: Int,
    modifier: Modifier = Modifier
) {
    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) activeIndicatorColor else Color.Transparent,
        animationSpec = tween(durationMillis = transitionDuration),
        label = "tab_bg"
    )
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(durationMillis = transitionDuration),
        label = "tab_content_color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(animatedBgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = selectedColor),
                onClick = onClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.icon,
                contentDescription = item.contentDescription,
                tint = animatedColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = animatedColor,
                maxLines = 1
            )
        }
    }
}
