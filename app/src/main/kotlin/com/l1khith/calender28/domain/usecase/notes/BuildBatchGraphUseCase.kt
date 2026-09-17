package com.l1khith.calender28.domain.usecase.notes

import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.domain.model.GraphData
import com.l1khith.calender28.domain.model.GraphEdge
import com.l1khith.calender28.domain.model.GraphNode
import com.l1khith.calender28.repository.NoteRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

/**
 * Builds a GraphData specifically for a batch of selected Markdown notes.
 * Unselected notes directly connected to selected notes are rendered as dashed stub nodes.
 * Non-Markdown (TXT) notes are strictly excluded.
 */
class BuildBatchGraphUseCase(
    private val noteRepository: NoteRepository,
    private val parseNoteLinksUseCase: ParseNoteLinksUseCase
) {

    suspend operator fun invoke(selectedNoteIds: Set<String>): GraphData {
        val allNotes = noteRepository.getAllNotes().firstOrNull() ?: emptyList()

        // Filter strictly to Markdown notes
        val mdNotes = allNotes.filter { it.format == NoteFormat.MD }
        val selectedMdNotes = mdNotes.filter { selectedNoteIds.contains(it.id) }

        if (selectedMdNotes.isEmpty()) {
            return GraphData()
        }

        val allMdByTitle = mdNotes.associateBy { it.displayTitle.lowercase() }
        val allMdById = mdNotes.associateBy { it.id }

        val nodesMap = mutableMapOf<String, GraphNode>()
        val edges = mutableListOf<GraphEdge>()
        val degrees = mutableMapOf<String, Int>()

        // Add selected notes as primary nodes
        selectedMdNotes.forEach { note ->
            val parsed = parseNoteLinksUseCase(note)
            nodesMap[note.id] = GraphNode(
                id = note.id,
                title = note.displayTitle,
                isMarkdown = true,
                isStub = false,
                degree = 0,
                isSelected = true,
                tags = parsed.tags,
                x = (Random.nextFloat() - 0.5f) * 600f,
                y = (Random.nextFloat() - 0.5f) * 600f
            )
        }

        // Connect selected notes to each other, and show unselected connections as dashed stubs
        selectedMdNotes.forEach { sourceNote ->
            val parsed = parseNoteLinksUseCase(sourceNote)
            parsed.links.forEach { link ->
                val targetKey = link.target.lowercase()
                val targetNote = allMdByTitle[targetKey] ?: allMdById[link.target]

                if (targetNote != null) {
                    val isTargetSelected = selectedNoteIds.contains(targetNote.id)
                    if (isTargetSelected) {
                        // Edge between two selected nodes: solid edge
                        val edgeId = "${sourceNote.id}->${targetNote.id}"
                        if (edges.none { it.id == edgeId }) {
                            edges.add(
                                GraphEdge(
                                    id = edgeId,
                                    sourceId = sourceNote.id,
                                    targetId = targetNote.id,
                                    isDashed = false,
                                    linkType = link.type
                                )
                            )
                            degrees[sourceNote.id] = (degrees[sourceNote.id] ?: 0) + 1
                            degrees[targetNote.id] = (degrees[targetNote.id] ?: 0) + 1
                        }
                    } else {
                        // Unselected external note: dashed stub node & dashed edge
                        val unselectedStubId = "unsel_${targetNote.id}"
                        if (!nodesMap.containsKey(unselectedStubId)) {
                            nodesMap[unselectedStubId] = GraphNode(
                                id = unselectedStubId,
                                title = targetNote.displayTitle,
                                isMarkdown = true,
                                isStub = true,
                                degree = 0,
                                isSelected = false,
                                tags = emptyList(),
                                x = (Random.nextFloat() - 0.5f) * 600f,
                                y = (Random.nextFloat() - 0.5f) * 600f
                            )
                        }
                        val edgeId = "${sourceNote.id}->${unselectedStubId}"
                        if (edges.none { it.id == edgeId }) {
                            edges.add(
                                GraphEdge(
                                    id = edgeId,
                                    sourceId = sourceNote.id,
                                    targetId = unselectedStubId,
                                    isDashed = true,
                                    linkType = link.type
                                )
                            )
                            degrees[sourceNote.id] = (degrees[sourceNote.id] ?: 0) + 1
                            degrees[unselectedStubId] = (degrees[unselectedStubId] ?: 0) + 1
                        }
                    }
                }
            }
        }

        val updatedNodes = nodesMap.values.map { node ->
            node.copy(degree = degrees[node.id] ?: 0)
        }

        return GraphData(nodes = updatedNodes, edges = edges)
    }
}
