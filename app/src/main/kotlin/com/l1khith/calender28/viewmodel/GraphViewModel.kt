package com.l1khith.calender28.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.domain.model.GraphData
import com.l1khith.calender28.domain.model.GraphNode
import com.l1khith.calender28.domain.usecase.notes.BuildBatchGraphUseCase
import com.l1khith.calender28.domain.usecase.notes.BuildGraphUseCase
import com.l1khith.calender28.repository.NoteRepository
import com.l1khith.calender28.ui.notes.graph.ForceDirectedLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GraphViewModel(
    application: Application,
    private val noteRepository: NoteRepository,
    private val buildGraphUseCase: BuildGraphUseCase,
    private val buildBatchGraphUseCase: BuildBatchGraphUseCase
) : AndroidViewModel(application) {

    private val _graphData = MutableStateFlow(GraphData())
    val graphData: StateFlow<GraphData> = _graphData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isPhysicsRunning = MutableStateFlow(true)
    val isPhysicsRunning: StateFlow<Boolean> = _isPhysicsRunning.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _allTags = MutableStateFlow<List<String>>(emptyList())
    val allTags: StateFlow<List<String>> = _allTags.asStateFlow()

    private val layout = ForceDirectedLayout()
    private var simulationJob: Job? = null

    fun loadGlobalGraph() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val data = buildGraphUseCase()
            _graphData.value = data
            _isLoading.value = false

            // Extract tags
            val tags = data.nodes.flatMap { it.tags }.distinct().sorted()
            _allTags.value = tags

            startSimulation()
        }
    }

    fun loadBatchGraph(selectedIds: Set<String>) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.Default) {
            val data = buildBatchGraphUseCase(selectedIds)
            _graphData.value = data
            _isLoading.value = false

            val tags = data.nodes.flatMap { it.tags }.distinct().sorted()
            _allTags.value = tags

            startSimulation()
        }
    }

    fun togglePhysics() {
        val next = !_isPhysicsRunning.value
        _isPhysicsRunning.value = next
        if (next) {
            startSimulation()
        } else {
            simulationJob?.cancel()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        if (!_isPhysicsRunning.value) return

        simulationJob = viewModelScope.launch(Dispatchers.Default) {
            var iterations = 0
            val maxIterations = 200 // Auto-pause after simulation settles (~3s)

            while (isActive && _isPhysicsRunning.value && iterations < maxIterations) {
                val currentData = _graphData.value
                val maxMove = layout.step(currentData)
                _graphData.value = currentData.copy() // Trigger state change for Canvas redraw

                iterations++
                if (maxMove < 0.2f && iterations > 20) {
                    // Settled early
                    break
                }
                delay(16) // ~60fps step
            }
            _isPhysicsRunning.value = false
        }
    }

    private val _nodeSizeScale = MutableStateFlow(1.0f)
    val nodeSizeScale: StateFlow<Float> = _nodeSizeScale.asStateFlow()

    fun setNodeSizeScale(scale: Float) {
        _nodeSizeScale.value = scale.coerceIn(0.5f, 2.5f)
    }

    fun updateNodePosition(nodeId: String, worldX: Float, worldY: Float) {
        val current = _graphData.value
        val target = current.nodes.find { it.id == nodeId } ?: return
        target.x = worldX
        target.y = worldY
        target.vx = 0f
        target.vy = 0f
        target.isPinned = true
        _graphData.value = current.copy()
    }

    fun releaseNode(nodeId: String) {
        val current = _graphData.value
        val target = current.nodes.find { it.id == nodeId } ?: return
        target.isPinned = false
        // Briefly nudge simulation if physics is enabled
        if (_isPhysicsRunning.value) {
            startSimulation()
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
