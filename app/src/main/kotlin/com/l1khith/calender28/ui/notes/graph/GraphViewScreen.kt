package com.l1khith.calender28.ui.notes.graph

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.domain.model.GraphNode
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.viewmodel.AppViewModelProvider
import com.l1khith.calender28.viewmodel.GraphViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphViewScreen(
    onBack: () -> Unit,
    onOpenNote: (noteId: String) -> Unit,
    batchSelectedIds: Set<String>? = null,
    modifier: Modifier = Modifier,
    viewModel: GraphViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory)
) {
    LaunchedEffect(batchSelectedIds) {
        if (batchSelectedIds.isNullOrEmpty()) {
            viewModel.loadGlobalGraph()
        } else {
            viewModel.loadBatchGraph(batchSelectedIds)
        }
    }

    val graphData by viewModel.graphData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isPhysicsRunning by viewModel.isPhysicsRunning.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val nodeSizeScale by viewModel.nodeSizeScale.collectAsStateWithLifecycle()

    var showControls by remember { mutableStateOf(false) }
    var activeNodeDetails by remember { mutableStateOf<GraphNode?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatrixColors.Surface)
            .statusBarsPadding()
    ) {
        // ── Compact Top Bar (Zero redundant vertical padding) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MatrixColors.TextHeader
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(
                    text = if (batchSelectedIds != null) "Selected Notes Graph" else "Notes Knowledge Graph",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MatrixColors.TextHeader,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = "${graphData.nodes.size} nodes · ${graphData.edges.size} connections",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MatrixColors.TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            // Size / Tuning Controls Toggle
            IconButton(
                onClick = { showControls = !showControls },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Graph Settings",
                    tint = if (showControls) MatrixColors.Primary else MatrixColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Physics Toggle
            IconButton(
                onClick = { viewModel.togglePhysics() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isPhysicsRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPhysicsRunning) "Pause Physics" else "Resume Physics",
                    tint = MatrixColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Compact Search Field ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Filter graph nodes...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MatrixColors.Primary, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MatrixColors.TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MatrixColors.Primary,
                    unfocusedBorderColor = MatrixColors.OutlineVariant.copy(alpha = 0.6f),
                    focusedTextColor = MatrixColors.TextHeader,
                    unfocusedTextColor = MatrixColors.TextHeader,
                    focusedContainerColor = MatrixColors.SurfaceContainerLow,
                    unfocusedContainerColor = MatrixColors.SurfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            )
        }

        // ── Collapsible Node Size Slider Bar ──
        AnimatedVisibility(visible = showControls) {
            Surface(
                color = MatrixColors.SurfaceContainerLow,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Node Size",
                        color = MatrixColors.TextHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = nodeSizeScale,
                        onValueChange = { viewModel.setNodeSizeScale(it) },
                        valueRange = 0.5f..2.5f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MatrixColors.Primary,
                            activeTrackColor = MatrixColors.Primary,
                            inactiveTrackColor = MatrixColors.OutlineVariant
                        )
                    )
                    Text(
                        text = "%.1fx".format(nodeSizeScale),
                        color = MatrixColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── Tag Chips Strip ──
        if (allTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allTags.forEach { tag ->
                    val isSelected = selectedTag == tag
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectTag(tag) },
                        label = { Text("#$tag", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MatrixColors.Primary.copy(alpha = 0.2f),
                            selectedLabelColor = MatrixColors.Primary,
                            containerColor = MatrixColors.SurfaceContainerLow,
                            labelColor = MatrixColors.TextSecondary
                        )
                    )
                }
            }
        }

        // ── Graph Canvas Body ──
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MatrixColors.Primary
                )
            } else if (graphData.nodes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Markdown notes found with links.\nCreate notes in .md format with [[wikilinks]].",
                        color = MatrixColors.TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                GraphCanvas(
                    graphData = graphData,
                    searchFilter = searchQuery,
                    selectedTagFilter = selectedTag,
                    nodeSizeScale = nodeSizeScale,
                    onNodeClick = { node ->
                        if (!node.isStub && !node.id.startsWith("stub_") && !node.id.startsWith("unsel_")) {
                            activeNodeDetails = node
                        }
                    },
                    onNodeDrag = { nodeId, worldX, worldY ->
                        viewModel.updateNodePosition(nodeId, worldX, worldY)
                    },
                    onNodeRelease = { nodeId ->
                        viewModel.releaseNode(nodeId)
                    }
                )
            }

            // Node Details Bottom Card overlay
            activeNodeDetails?.let { node ->
                Surface(
                    color = MatrixColors.SurfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = node.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixColors.TextHeader
                                )
                            )
                            Text(
                                text = "${node.degree} links",
                                color = MatrixColors.Primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (node.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = node.tags.joinToString(" ") { "#$it" },
                                color = MatrixColors.Secondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { activeNodeDetails = null }) {
                                Text("Close", color = MatrixColors.TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val id = node.id
                                    activeNodeDetails = null
                                    onOpenNote(id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary)
                            ) {
                                Text("Open Note", color = MatrixColors.OnPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
