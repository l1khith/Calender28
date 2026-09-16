package com.l1khith.calender28.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.data.BottomTab
import com.l1khith.calender28.ui.AnimatedSparkDivider
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.AppSettingsManager
import com.l1khith.calender28.utils.showPlatformToast
import com.l1khith.calender28.viewmodel.CustomizeNavViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeNavScreen(
    viewModel: CustomizeNavViewModel,
    onBack: () -> Unit
) {
    val enabledTabs by viewModel.enabledTabs.collectAsStateWithLifecycle()
    val tabOrder by viewModel.tabOrder.collectAsStateWithLifecycle()
    val enableAnimations by AppSettingsManager.enableAnimations.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var itemHeightPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(viewModel) {
        viewModel.errorEvent.collect { message ->
            showPlatformToast(message)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixColors.Surface)
            .statusBarsPadding()
    ) {
        // Custom Top Bar (without nested Scaffold to prevent double-insets)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MatrixColors.TextHeader
                )
            }
            Text(
                text = "Customize Navigation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MatrixColors.TextHeader,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header instructions
            item {
                Text(
                    text = "Drag and drop tabs using the handle to reorder them. Toggle switches to show or hide tabs (at least one tab must remain enabled).",
                    color = MatrixColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Tabs Management Section (Reorderable List)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TAB ORDER & VISIBILITY",
                        color = MatrixColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    tabOrder.forEachIndexed { index, tab ->
                        val isEnabled = enabledTabs.contains(tab)
                        val isLocked = viewModel.isTabLocked(tab)
                        val isDraggingThis = draggingIndex == index

                        val translationY = if (isDraggingThis) dragOffsetY else 0f
                        val zIndex = if (isDraggingThis) 2f else 0f
                        val scale = if (isDraggingThis) 1.03f else 1f

                        Card(
                            shape = MatrixShapes.Lg,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDraggingThis) MatrixColors.SurfaceContainerHigh else MatrixColors.SurfaceContainerLow
                            ),
                            border = BorderStroke(
                                width = if (isDraggingThis) 1.5.dp else 1.dp,
                                color = if (isDraggingThis) MatrixColors.Primary else MatrixColors.OutlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    if (itemHeightPx == 0f) {
                                        itemHeightPx = coordinates.size.height.toFloat()
                                    }
                                }
                                .graphicsLayer {
                                    this.translationY = translationY
                                    this.scaleX = scale
                                    this.scaleY = scale
                                }
                                .zIndex(zIndex)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Drag Handle with gesture detector
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .pointerInput(tab, index) {
                                            detectVerticalDragGestures(
                                                onDragStart = {
                                                    draggingIndex = index
                                                    dragOffsetY = 0f
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                },
                                                onDragEnd = {
                                                    draggingIndex = null
                                                    dragOffsetY = 0f
                                                },
                                                onDragCancel = {
                                                    draggingIndex = null
                                                    dragOffsetY = 0f
                                                },
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffsetY += dragAmount

                                                    val currentIndex = draggingIndex ?: return@detectVerticalDragGestures
                                                    val threshold = if (itemHeightPx > 0f) itemHeightPx * 0.5f else 100f

                                                    if (dragOffsetY > threshold && currentIndex < tabOrder.lastIndex) {
                                                        viewModel.reorderTabs(currentIndex, currentIndex + 1)
                                                        draggingIndex = currentIndex + 1
                                                        dragOffsetY -= itemHeightPx
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    } else if (dragOffsetY < -threshold && currentIndex > 0) {
                                                        viewModel.reorderTabs(currentIndex, currentIndex - 1)
                                                        draggingIndex = currentIndex - 1
                                                        dragOffsetY += itemHeightPx
                                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = "Drag to reorder ${tab.label}",
                                        tint = if (isDraggingThis) MatrixColors.Primary else MatrixColors.TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Tab Icon container
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isEnabled) MatrixColors.PrimaryContainer else MatrixColors.SurfaceContainerHigh
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (isEnabled) MatrixColors.Primary else MatrixColors.TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Tab Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tab.label,
                                        color = MatrixColors.TextHeader,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isLocked) "Required — cannot be disabled" else tab.description,
                                        color = if (isLocked) MatrixColors.Primary else MatrixColors.TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isLocked) FontWeight.Medium else FontWeight.Normal,
                                        lineHeight = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Up/Down Reorder Shortcuts
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    IconButton(
                                        onClick = { viewModel.moveUp(tab) },
                                        enabled = index > 0,
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Move Up",
                                            tint = if (index > 0) MatrixColors.TextSecondary else MatrixColors.OutlineVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.moveDown(tab) },
                                        enabled = index < tabOrder.lastIndex,
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Move Down",
                                            tint = if (index < tabOrder.lastIndex) MatrixColors.TextSecondary else MatrixColors.OutlineVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Toggle Switch
                                Switch(
                                    checked = isEnabled,
                                    enabled = !isLocked,
                                    onCheckedChange = { checked ->
                                        viewModel.toggleTab(tab, checked)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MatrixColors.Primary,
                                        checkedTrackColor = MatrixColors.PrimaryContainer,
                                        uncheckedThumbColor = MatrixColors.TextSecondary,
                                        uncheckedTrackColor = MatrixColors.SurfaceContainerHigh,
                                        disabledCheckedThumbColor = MatrixColors.Primary.copy(alpha = 0.6f),
                                        disabledCheckedTrackColor = MatrixColors.PrimaryContainer.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Live Preview Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LIVE PREVIEW",
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
                            AnimatedSparkDivider(
                                baseColor = MatrixColors.OutlineVariant,
                                sparkColor = MatrixColors.Primary,
                                glowColor = MatrixColors.Secondary,
                                height = 1.dp,
                                reverseDirection = false,
                                durationMillis = 4000,
                                enableSparkle = enableAnimations
                            )

                            // Renders enabled tabs in the exact custom tabOrder!
                            val visibleTabs = tabOrder.filter { enabledTabs.contains(it) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                visibleTabs.forEach { tab ->
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label,
                                            tint = MatrixColors.Primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MatrixColors.Primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top Bar Slot 1 Section
            item {
                val currentSlot by AppSettingsManager.topBarSlot1.collectAsStateWithLifecycle()
                val userName by AppSettingsManager.userName.collectAsStateWithLifecycle()
                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        com.l1khith.calender28.ui.profile.settings.TopBarSlotPicker(
                            currentSlot = currentSlot,
                            userName = userName,
                            onSlotSelected = { AppSettingsManager.setTopBarSlot1(it) }
                        )
                    }
                }
            }

            // Reset to Defaults Button
            item {
                OutlinedButton(
                    onClick = { viewModel.resetToDefaults() },
                    shape = MatrixShapes.Md,
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MatrixColors.TextHeader
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Reset to Defaults",
                        tint = MatrixColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset to Defaults",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
