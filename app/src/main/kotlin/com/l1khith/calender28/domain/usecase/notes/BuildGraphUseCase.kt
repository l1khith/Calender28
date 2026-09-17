package com.l1khith.calender28.domain.usecase.notes

import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.data.NoteLinkDao
import com.l1khith.calender28.domain.model.GraphData
import com.l1khith.calender28.domain.model.GraphEdge
import com.l1khith.calender28.domain.model.GraphNode
import com.l1khith.calender28.repository.NoteRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlin.random.Random

/**
 * Builds a complete GraphData for all Markdown notes in the vault.
 * Non-Markdown (TXT) notes are strictly excluded.
 */
class BuildGraphUseCase(
    private val noteRepository: NoteRepository,
    private val noteLinkDao: NoteLinkDao,
    private val parseNoteLinksUseCase: ParseNoteLinksUseCase
) {

    suspend operator fun invoke(): GraphData {
        val allNotes = noteRepository.getAllNotes().firstOrNull() ?: emptyList()

        // Markdown only constraint
        val mdNotes = allNotes.filter { it.format == NoteFormat.MD }
        if (mdNotes.isEmpty()) {
            return GraphData()
        }

        val noteByTitle = mdNotes.associateBy { it.displayTitle.lowercase() }
        val noteById = mdNotes.associateBy { it.id }

        val nodesMap = mutableMapOf<String, GraphNode>()
        val edges = mutableListOf<GraphEdge>()
        val degrees = mutableMapOf<String, Int>()

        // Initialize nodes for all MD notes
        mdNotes.forEach { note ->
            val parsed = parseNoteLinksUseCase(note)
            nodesMap[note.id] = GraphNode(
                id = note.id,
                title = note.displayTitle,
                isMarkdown = true,
                isStub = false,
                degree = 0,
                isSelected = false,
                tags = parsed.tags,
                x = (Random.nextFloat() - 0.5f) * 600f,
                y = (Random.nextFloat() - 0.5f) * 600f
            )
        }

        // Resolve links
        mdNotes.forEach { sourceNote ->
            val parsed = parseNoteLinksUseCase(sourceNote)
            parsed.links.forEach { link ->
                val targetKey = link.target.lowercase()
                val targetNote = noteByTitle[targetKey] ?: noteById[link.target]

                val targetNodeId = if (targetNote != null) {
                    targetNote.id
                } else {
                    // Unresolved note link -> create a Stub grey node
                    val stubId = "stub_${link.target.hashCode()}"
                    if (!nodesMap.containsKey(stubId)) {
                        nodesMap[stubId] = GraphNode(
                            id = stubId,
                            title = link.target,
                            isMarkdown = true,
                            isStub = true,
                            degree = 0,
                            isSelected = false,
                            tags = emptyList(),
                            x = (Random.nextFloat() - 0.5f) * 600f,
                            y = (Random.nextFloat() - 0.5f) * 600f
                        )
                    }
                    stubId
                }

                val edgeId = "${sourceNote.id}->${targetNodeId}"
                if (edges.none { it.id == edgeId }) {
                    edges.add(
                        GraphEdge(
                            id = edgeId,
                            sourceId = sourceNote.id,
                            targetId = targetNodeId,
                            isDashed = false,
                            linkType = link.type
                        )
                    )
                    degrees[sourceNote.id] = (degrees[sourceNote.id] ?: 0) + 1
                    degrees[targetNodeId] = (degrees[targetNodeId] ?: 0) + 1
                }
            }
        }

        // Update degrees
        val updatedNodes = nodesMap.values.map { node ->
            node.copy(degree = degrees[node.id] ?: 0)
        }

        return GraphData(nodes = updatedNodes, edges = edges)
    }
}
