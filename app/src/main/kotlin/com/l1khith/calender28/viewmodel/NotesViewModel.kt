package com.l1khith.calender28.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(
    application: Application,
    private val noteRepository: NoteRepository
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    val notes: StateFlow<List<Note>> = combine(_searchQuery, _selectedFilter) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        val baseFlow = if (query.isBlank()) {
            noteRepository.observeAllNotes()
        } else {
            noteRepository.searchNotes(query.trim())
        }

        baseFlow.map { list ->
            if (filter == "ALL") {
                list
            } else {
                list.filter { it.linkedEntity.equals(filter, ignoreCase = true) }
            }
        }
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun saveNote(
        title: String,
        content: String,
        existingNote: Note? = null,
        colorHex: String? = null,
        associatedDate: String? = null,
        linkedEntity: String = "NOTE",
        isMarkdown: Boolean = false
    ) {
        if (title.isBlank() && content.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val noteToSave = existingNote?.copy(
                title = title.trim(),
                content = content.trim(),
                colorHex = colorHex ?: existingNote.colorHex,
                associatedDate = associatedDate ?: existingNote.associatedDate,
                linkedEntity = linkedEntity,
                isMarkdown = isMarkdown,
                updatedAtMs = System.currentTimeMillis()
            ) ?: Note(
                title = title.trim(),
                content = content.trim(),
                colorHex = colorHex,
                associatedDate = associatedDate,
                linkedEntity = linkedEntity,
                isMarkdown = isMarkdown,
                createdAtMs = System.currentTimeMillis(),
                updatedAtMs = System.currentTimeMillis()
            )
            noteRepository.saveNote(noteToSave)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.togglePin(note.id)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(note.id)
        }
    }
}
