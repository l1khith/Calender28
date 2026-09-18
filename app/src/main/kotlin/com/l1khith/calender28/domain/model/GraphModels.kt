package com.l1khith.calender28.domain.model

import androidx.compose.runtime.Immutable

/**
 * Node model representing a note or linked entity in the force-directed graph.
 */
@Immutable
data class GraphNode(
    val id: String,
    val title: String,
    val isMarkdown: Boolean = true,
    val isStub: Boolean = false,
    val degree: Int = 0,
    val isSelected: Boolean = false,
    val tags: List<String> = emptyList(),
    // Physical coordinates and velocity for simulation
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isPinned: Boolean = false
)

/**
 * Directed / undirected edge connecting two nodes.
 */
@Immutable
data class GraphEdge(
    val id: String,
    val sourceId: String,
    val targetId: String,
    val isDashed: Boolean = false,
    val linkType: String = "WIKILINK"
)

/**
 * Complete graph representation.
 */
@Immutable
data class GraphData(
    val nodes: List<GraphNode> = emptyList(),
    val edges: List<GraphEdge> = emptyList()
)
