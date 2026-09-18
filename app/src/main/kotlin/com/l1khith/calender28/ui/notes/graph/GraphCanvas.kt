package com.l1khith.calender28.ui.notes.graph

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.l1khith.calender28.domain.model.GraphData
import com.l1khith.calender28.domain.model.GraphNode
import com.l1khith.calender28.ui.theme.MatrixColors
import kotlin.math.floor
import kotlin.math.sqrt

@Composable
fun GraphCanvas(
    graphData: GraphData,
    searchFilter: String = "",
    selectedTagFilter: String? = null,
    nodeSizeScale: Float = 1.0f,
    onNodeClick: (GraphNode) -> Unit = {},
    onNodeDrag: (nodeId: String, worldX: Float, worldY: Float) -> Unit = { _, _, _ -> },
    onNodeRelease: (nodeId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.25f, 4.0f)
        offset += offsetChange
    }

    val dashedPathEffect = remember { PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) }

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val nodes = graphData.nodes
    val edges = graphData.edges
    val nodeMap = remember(nodes) { nodes.associateBy { it.id } }

    var draggedNodeId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformState, enabled = draggedNodeId == null)
            .pointerInput(nodes, scale, offset, nodeSizeScale) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val worldX = (startOffset.x - center.x - offset.x) / scale
                        val worldY = (startOffset.y - center.y - offset.y) / scale

                        val hitNode = nodes.findLast { node ->
                            val radius = (10f + (node.degree * 2.2f)) * nodeSizeScale
                            val dx = worldX - node.x
                            val dy = worldY - node.y
                            sqrt(dx * dx + dy * dy) <= radius * 1.8f
                        }

                        if (hitNode != null) {
                            draggedNodeId = hitNode.id
                        }
                    },
                    onDrag = { change, dragAmount ->
                        val currentId = draggedNodeId
                        if (currentId != null) {
                            change.consume()
                            val node = nodeMap[currentId]
                            if (node != null) {
                                val newWorldX = node.x + (dragAmount.x / scale)
                                val newWorldY = node.y + (dragAmount.y / scale)
                                onNodeDrag(currentId, newWorldX, newWorldY)
                            }
                        } else {
                            // Pan the canvas if no node was hit
                            offset += dragAmount
                        }
                    },
                    onDragEnd = {
                        val currentId = draggedNodeId
                        if (currentId != null) {
                            onNodeRelease(currentId)
                            draggedNodeId = null
                        }
                    },
                    onDragCancel = {
                        val currentId = draggedNodeId
                        if (currentId != null) {
                            onNodeRelease(currentId)
                            draggedNodeId = null
                        }
                    }
                )
            }
            .pointerInput(nodes, scale, offset, nodeSizeScale) {
                detectTapGestures { tapOffset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val worldX = (tapOffset.x - center.x - offset.x) / scale
                    val worldY = (tapOffset.y - center.y - offset.y) / scale

                    val hitNode = nodes.findLast { node ->
                        val radius = (10f + (node.degree * 2.2f)) * nodeSizeScale
                        val dx = worldX - node.x
                        val dy = worldY - node.y
                        sqrt(dx * dx + dy * dy) <= radius * 1.8f
                    }
                    if (hitNode != null) {
                        onNodeClick(hitNode)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // ── 0. Draw Dotted Background Grid (Obsidian style) ──
            val baseGridSpacing = 36f
            val dotSpacing = baseGridSpacing * scale
            val dotRadius = 1.2f * scale.coerceIn(0.7f, 1.8f)
            val dotColor = MatrixColors.TextSecondary.copy(alpha = 0.18f)

            if (dotSpacing >= 12f) {
                val startX = ((center.x + offset.x) % dotSpacing + dotSpacing) % dotSpacing
                val startY = ((center.y + offset.y) % dotSpacing + dotSpacing) % dotSpacing

                var x = startX
                while (x < size.width) {
                    var y = startY
                    while (y < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                        y += dotSpacing
                    }
                    x += dotSpacing
                }
            }

            // ── 1. Draw Edges ──
            for (edge in edges) {
                val source = nodeMap[edge.sourceId] ?: continue
                val target = nodeMap[edge.targetId] ?: continue

                val p1 = Offset(
                    x = center.x + offset.x + source.x * scale,
                    y = center.y + offset.y + source.y * scale
                )
                val p2 = Offset(
                    x = center.x + offset.x + target.x * scale,
                    y = center.y + offset.y + target.y * scale
                )

                val edgeColor = if (edge.isDashed) {
                    MatrixColors.TextSecondary.copy(alpha = 0.35f)
                } else {
                    MatrixColors.Primary.copy(alpha = 0.42f)
                }

                drawLine(
                    color = edgeColor,
                    start = p1,
                    end = p2,
                    strokeWidth = (if (edge.isDashed) 1.2f else 1.8f) * scale,
                    pathEffect = if (edge.isDashed) dashedPathEffect else null
                )
            }

            // ── 2. Draw Nodes ──
            for (node in nodes) {
                val nodeCenter = Offset(
                    x = center.x + offset.x + node.x * scale,
                    y = center.y + offset.y + node.y * scale
                )

                val baseRadius = (10f + (node.degree * 2.2f)) * nodeSizeScale
                val screenRadius = (baseRadius * scale).coerceIn(4f, 80f)

                val matchesSearch = searchFilter.isBlank() || node.title.contains(searchFilter, ignoreCase = true)
                val matchesTag = selectedTagFilter == null || node.tags.contains(selectedTagFilter)

                val isBeingDragged = node.id == draggedNodeId

                val nodeColor = when {
                    !matchesSearch || !matchesTag -> MatrixColors.OutlineVariant.copy(alpha = 0.25f)
                    isBeingDragged -> Color(0xFF67E8F9) // Cyan highlight while dragging
                    node.isStub -> Color(0xFF6E7681) // Grey stub
                    node.isSelected -> MatrixColors.Tertiary // Accent for batch selected
                    else -> MatrixColors.Primary // Emerald green for regular MD notes
                }

                // Draw outer glow when dragging
                if (isBeingDragged) {
                    drawCircle(
                        color = Color(0xFF67E8F9).copy(alpha = 0.3f),
                        radius = screenRadius + (6f * scale),
                        center = nodeCenter
                    )
                }

                drawCircle(
                    color = nodeColor,
                    radius = screenRadius,
                    center = nodeCenter
                )

                // Draw label when scale is reasonable or when highlighted
                if (scale >= 0.55f && (matchesSearch && matchesTag)) {
                    drawIntoCanvas { canvas ->
                        textPaint.textSize = (10.5f * scale * nodeSizeScale.coerceIn(0.8f, 1.4f)).coerceIn(8f, 16f) * density
                        textPaint.color = if (node.isStub) {
                            android.graphics.Color.argb(180, 160, 160, 160)
                        } else {
                            android.graphics.Color.WHITE
                        }
                        val labelY = nodeCenter.y + screenRadius + (11f * scale)
                        canvas.nativeCanvas.drawText(node.title, nodeCenter.x, labelY, textPaint)
                    }
                }
            }
        }
    }
}
