package com.l1khith.calender28.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel managing Notes list state, real-time search, and editor draft canvas.
 */
class NotesViewModel(
    application: Application,
    private val noteRepository: NoteRepository,
    private val generateDefaultNoteTitleUseCase: com.l1khith.calender28.domain.usecase.notes.GenerateDefaultNoteTitleUseCase? = null
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Base flow of all notes from DB
    private val _allNotes = noteRepository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered notes by search query (matching content)
    val notes: StateFlow<List<Note>> = combine(_allNotes, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { note ->
                note.content.contains(query, ignoreCase = true) ||
                    note.displayTitle.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ─── Active Note / Editor Canvas State ───
    private val _activeNote = MutableStateFlow<Note?>(null)
    val activeNote: StateFlow<Note?> = _activeNote.asStateFlow()

    private val _draftTitle = MutableStateFlow("")
    val draftTitle: StateFlow<String> = _draftTitle.asStateFlow()

    private val _draftContent = MutableStateFlow("")
    val draftContent: StateFlow<String> = _draftContent.asStateFlow()

    private val _draftFormat = MutableStateFlow(NoteFormat.TXT)
    val draftFormat: StateFlow<NoteFormat> = _draftFormat.asStateFlow()

    private val _isPreviewMode = MutableStateFlow(false)
    val isPreviewMode: StateFlow<Boolean> = _isPreviewMode.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun updateTitle(title: String) {
        _draftTitle.value = title
    }

    /**
     * Opens a new blank note canvas.
     */
    fun createNewNote(
        context: com.l1khith.calender28.domain.model.NoteContext = com.l1khith.calender28.domain.model.NoteContext.Standalone,
        defaultFormat: NoteFormat = NoteFormat.TXT
    ) {
        _draftContent.value = ""
        _draftFormat.value = defaultFormat
        _isPreviewMode.value = false

        viewModelScope.launch {
            val autoTitle = generateDefaultNoteTitleUseCase?.invoke(context) ?: ""
            _draftTitle.value = autoTitle
            val newNote = Note(
                title = autoTitle,
                content = "",
                format = defaultFormat
            )
            _activeNote.value = newNote
        }
    }

    /**
     * Opens an existing note in the editor canvas.
     */
    fun openNoteForEdit(note: Note) {
        _activeNote.value = note
        _draftTitle.value = note.title
        _draftContent.value = note.content
        _draftFormat.value = note.format
        _isPreviewMode.value = false
    }

    fun updateContent(content: String) {
        _draftContent.value = content
    }

    fun setFormat(format: NoteFormat) {
        _draftFormat.value = format
        if (format == NoteFormat.TXT) {
            _isPreviewMode.value = false
        }
    }

    fun toggleFormat() {
        val newFormat = if (_draftFormat.value == NoteFormat.MD) NoteFormat.TXT else NoteFormat.MD
        setFormat(newFormat)
    }

    fun togglePreview() {
        if (_draftFormat.value == NoteFormat.MD) {
            _isPreviewMode.value = !_isPreviewMode.value
        }
    }

    fun setPreviewMode(enabled: Boolean) {
        if (_draftFormat.value == NoteFormat.MD) {
            _isPreviewMode.value = enabled
        } else {
            _isPreviewMode.value = false
        }
    }

    /**
     * Saves the draft and closes the editor canvas.
     * Blank/empty notes are automatically discarded.
     */
    fun saveAndCloseEditor(onSaved: (() -> Unit)? = null) {
        val current = _activeNote.value ?: return
        val finalTitle = _draftTitle.value.trim()
        val finalContent = _draftContent.value.trimEnd()
        val finalFormat = _draftFormat.value

        if (finalContent.isBlank() && finalTitle.isBlank()) {
            // Delete or discard blank note
            viewModelScope.launch {
                noteRepository.deleteNote(current)
                _activeNote.value = null
                _draftTitle.value = ""
                _draftContent.value = ""
                _isPreviewMode.value = false
                onSaved?.invoke()
            }
            return
        }

        val updatedNote = current.copy(
            title = finalTitle,
            content = finalContent,
            format = finalFormat,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            noteRepository.insertNote(updatedNote)
            _activeNote.value = null
            _draftTitle.value = ""
            _draftContent.value = ""
            _isPreviewMode.value = false
            onSaved?.invoke()
        }
    }

    fun closeEditorWithoutSaving() {
        _activeNote.value = null
        _draftTitle.value = ""
        _draftContent.value = ""
        _isPreviewMode.value = false
    }

    fun deleteNote(note: Note, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            noteRepository.deleteNote(note)
            if (_activeNote.value?.id == note.id) {
                _activeNote.value = null
                _draftTitle.value = ""
                _draftContent.value = ""
                _isPreviewMode.value = false
            }
            onDeleted?.invoke()
        }
    }

    fun deleteActiveNote(onDeleted: (() -> Unit)? = null) {
        val current = _activeNote.value ?: return
        deleteNote(current, onDeleted)
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            val updated = note.copy(
                isPinned = !note.isPinned,
                updatedAt = System.currentTimeMillis()
            )
            noteRepository.updateNote(updated)
        }
    }
}
