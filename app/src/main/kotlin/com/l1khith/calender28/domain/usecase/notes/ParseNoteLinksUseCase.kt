package com.l1khith.calender28.domain.usecase.notes

import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteFormat

/**
 * Parses internal links from markdown content.
 * Strictly ignores non-Markdown (TXT) notes per user constraint.
 */
class ParseNoteLinksUseCase {

    private val wikiLinkRegex = Regex("""\[\[(.*?)\]\]""")
    private val markdownLinkRegex = Regex("""\[([^\]]+)\]\(([^)]+)\)""")
    private val tagRegex = Regex("""#([a-zA-Z0-9_-]+)""")

    data class ParsedLink(
        val target: String,
        val type: String // "WIKILINK" or "MD_LINK"
    )

    data class ParseResult(
        val links: List<ParsedLink>,
        val tags: List<String>
    )

    operator fun invoke(note: Note): ParseResult {
        // Enforce Markdown-only requirement: only notes with NoteFormat.MD participate
        if (note.format != NoteFormat.MD) {
            return ParseResult(emptyList(), emptyList())
        }

        val links = mutableListOf<ParsedLink>()

        // 1. [[wikilinks]]
        wikiLinkRegex.findAll(note.content).forEach { match ->
            val target = match.groupValues[1].trim()
            if (target.isNotBlank()) {
                links.add(ParsedLink(target = target, type = "WIKILINK"))
            }
        }

        // 2. [markdown](link)
        markdownLinkRegex.findAll(note.content).forEach { match ->
            val target = match.groupValues[2].trim()
            // Ignore external URLs (http/https)
            if (target.isNotBlank() && !target.startsWith("http://") && !target.startsWith("https://")) {
                links.add(ParsedLink(target = target, type = "MD_LINK"))
            }
        }

        // 3. #tags
        val tags = tagRegex.findAll(note.content)
            .map { it.groupValues[1].trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()

        return ParseResult(links = links, tags = tags)
    }
}
