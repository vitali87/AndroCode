package com.example.androcode.viewmodels

import android.app.Application
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    // State for the URI of the currently opened file
    private val _openedFileUri = MutableStateFlow<Uri?>(null)
    val openedFileUri: StateFlow<Uri?> = _openedFileUri.asStateFlow()

    // State for the content *as loaded from the file*
    private val _loadedFileContent = MutableStateFlow<String?>(null)

    // State for the *current content* in the editor
    private val _currentFileContent = MutableStateFlow<String?>(null)
    val currentFileContent: StateFlow<String?> = _currentFileContent.asStateFlow()

    // State to track if the current content has been modified
    private val _isModified = MutableStateFlow(false)
    val isModified: StateFlow<Boolean> = _isModified.asStateFlow()

    // State for loading indicator specific to file reading
    private val _isLoadingContent = MutableStateFlow(false)
    val isLoadingContent: StateFlow<Boolean> = _isLoadingContent.asStateFlow()

    // State for potential file reading errors
    private val _errorLoadingContent = MutableStateFlow<String?>(null)
    val errorLoadingContent: StateFlow<String?> = _errorLoadingContent.asStateFlow()

    // State for saving indicator
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // State for potential file saving errors
    private val _errorSaving = MutableStateFlow<String?>(null)
    val errorSaving: StateFlow<String?> = _errorSaving.asStateFlow()

    // State for the TextFieldValue (includes text, selection, composition)
    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue: StateFlow<TextFieldValue> = _textFieldValue.asStateFlow()

    // State for additional selections (for multi-cursor)
    private val _additionalSelections = MutableStateFlow<List<TextRange>>(emptyList())
    val additionalSelections: StateFlow<List<TextRange>> = _additionalSelections.asStateFlow()

    // SharedFlow to signal the UI to scroll to the current selection
    private val _scrollToSelectionEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scrollToSelectionEvent = _scrollToSelectionEvent.asSharedFlow()

    // --- Find Functionality State ---
    private val _isFindBarVisible = MutableStateFlow(false)
    val isFindBarVisible: StateFlow<Boolean> = _isFindBarVisible.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<IntRange>>(emptyList())
    val searchResults: StateFlow<List<IntRange>> = _searchResults.asStateFlow()

    private val _currentMatchIndex = MutableStateFlow(-1) // -1 means no current selection
    val currentMatchIndex: StateFlow<Int> = _currentMatchIndex.asStateFlow()

    // --- Replace Functionality State ---
    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery: StateFlow<String> = _replaceQuery.asStateFlow()

    // --- Code Folding State ---
    // Maps the starting line index of a foldable block to its full line range
    private val _foldableRegions = MutableStateFlow<Map<Int, IntRange>>(emptyMap())
    val foldableRegions: StateFlow<Map<Int, IntRange>> = _foldableRegions.asStateFlow()

    // Set of starting line indices that are currently folded
    private val _foldedLines = MutableStateFlow<Set<Int>>(emptySet())
    val foldedLines: StateFlow<Set<Int>> = _foldedLines.asStateFlow()
    // --- End Code Folding State ---

    // --- End Find/Replace Functionality State ---

    /**
     * Attempts to open and read the content of a file identified by its URI.
     * Updates the ViewModel's state with the URI, content, loading status, and errors.
     */
    fun openFile(uri: Uri) {
        // Prevent reloading the same file unnecessarily
        if (uri == _openedFileUri.value && !_isModified.value) { 
             println("File already open and unmodified: $uri")
             return
        }

        viewModelScope.launch {
            _isLoadingContent.value = true
            _errorLoadingContent.value = null // Clear previous errors
            try {
                val content = readFileContent(uri)
                _loadedFileContent.value = content
                _currentFileContent.value = content
                _textFieldValue.value = TextFieldValue(content) // Initialize TextFieldValue
                _openedFileUri.value = uri
                _isModified.value = false // Reset modified state on open
            } catch (e: Exception) {
                println("Error reading file content for $uri: ${e.message}")
                _errorLoadingContent.value = "Error loading file: ${e.message}"
                 _openedFileUri.value = null 
                 _loadedFileContent.value = null
                 _currentFileContent.value = null
                 _isModified.value = false
            } finally {
                _isLoadingContent.value = false
            }
        }
    }

    // Call this when the text content changes in the UI
    // Needs to handle TextFieldValue for BasicTextField
    fun onTextFieldValueChange(newValue: TextFieldValue) {
        val oldText = _textFieldValue.value.text
        _textFieldValue.value = newValue
        // Only update current content and modified flag if text actually changed
        if (oldText != newValue.text) {
            _currentFileContent.value = newValue.text
            _isModified.value = _loadedFileContent.value != newValue.text
            // Trigger foldable region analysis when text changes
            updateFoldableRegions(newValue.text)
        }
    }

    /**
     * Saves the current editor content back to the originally opened file URI.
     */
    fun saveFile() {
        val uriToSave = _openedFileUri.value
        val contentToSave = _currentFileContent.value

        // Check if there's content and a URI to save to
        if (uriToSave != null && contentToSave != null) { 
            viewModelScope.launch {
                _isSaving.value = true
                _errorSaving.value = null
                try {
                    writeFileContent(uriToSave, contentToSave)
                    // Update loaded content state after successful save
                    _loadedFileContent.value = contentToSave
                    _isModified.value = false
                    println("File saved successfully: $uriToSave")
                } catch (e: Exception) {
                    println("Error saving file $uriToSave: ${e.message}")
                    _errorSaving.value = "Error saving file: ${e.message}"
                } finally {
                    _isSaving.value = false
                }
            }
        } else {
            _errorSaving.value = "No file open or no content to save."
        }
    }

    // --- Find Functionality Methods ---

    fun toggleFindBarVisibility() {
        val becomingVisible = !_isFindBarVisible.value
        _isFindBarVisible.value = becomingVisible
        if (!becomingVisible) {
            // Reset search state when hiding the bar
            resetSearchState()
        }
    }

    fun setSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
            performSearch()
        }
    }

    private fun performSearch() {
        val query = _searchQuery.value
        val content = _textFieldValue.value.text
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _currentMatchIndex.value = -1
            return
        }

        val results = mutableListOf<IntRange>()
        var startIndex = 0
        while (startIndex < content.length) {
            val foundIndex = content.indexOf(query, startIndex, ignoreCase = true) // Simple case-insensitive search
            if (foundIndex == -1) break
            results.add(foundIndex..(foundIndex + query.length))
            startIndex = foundIndex + 1 // Move past the current find to find next
        }

        _searchResults.value = results
        _currentMatchIndex.value = if (results.isNotEmpty()) 0 else -1
        highlightCurrentMatch()
        // After initially performing search, try to scroll to the first match
        if (_currentMatchIndex.value != -1) {
            _scrollToSelectionEvent.tryEmit(Unit)
        }
        _additionalSelections.value = emptyList()
    }

    private fun highlightCurrentMatch() {
        val results = _searchResults.value
        val index = _currentMatchIndex.value
        if (index in results.indices) {
            val range = results[index]
            // Update TextFieldValue selection to highlight the match
            _textFieldValue.value = _textFieldValue.value.copy(
                selection = TextRange(range.first, range.last)
            )
        } else {
            // Clear selection if no match is highlighted
            _textFieldValue.value = _textFieldValue.value.copy(
                selection = TextRange.Zero
            )
        }
        // Always try to signal scroll after highlighting, even if selection cleared
        _scrollToSelectionEvent.tryEmit(Unit)
    }

    fun findNext() {
        val results = _searchResults.value
        if (results.isEmpty()) return

        var nextIndex = _currentMatchIndex.value + 1
        if (nextIndex >= results.size) {
            nextIndex = 0 // Wrap around to the start
        }
        _currentMatchIndex.value = nextIndex
        highlightCurrentMatch()
        // TODO: Need to trigger UI update to scroll/show the new selection
        // Scrolling is now triggered by highlightCurrentMatch via SharedFlow
    }

    fun findPrevious() {
        val results = _searchResults.value
        if (results.isEmpty()) return

        var prevIndex = _currentMatchIndex.value - 1
        if (prevIndex < 0) {
            prevIndex = results.size - 1 // Wrap around to the end
        }
        _currentMatchIndex.value = prevIndex
        highlightCurrentMatch()
        // TODO: Need to trigger UI update to scroll/show the new selection
        // Scrolling is now triggered by highlightCurrentMatch via SharedFlow
    }

    private fun resetSearchState() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _currentMatchIndex.value = -1
        // Keep _isFindBarVisible as is, toggling is handled separately
    }

    // Function to add a new cursor/selection at a specific offset
    fun addSelection(offset: Int) {
        val newSelection = TextRange(offset) // Create a collapsed selection (cursor)
        // Avoid adding duplicate cursors at the exact same spot
        if (!_additionalSelections.value.contains(newSelection) &&
            _textFieldValue.value.selection != newSelection) {
            _additionalSelections.value = _additionalSelections.value + newSelection
        }
    }

    /**
     * Clears all selections except the primary one managed by TextFieldValue.
     */
    fun clearAdditionalSelections() {
        _additionalSelections.value = emptyList()
    }

    // --- End Find Functionality Methods ---

    // --- Code Folding Methods ---

    fun toggleFold(startLineIndex: Int) {
        val currentFolded = _foldedLines.value
        println("[ViewModel] toggleFold called for line $startLineIndex. Current folded: $currentFolded")
        if (currentFolded.contains(startLineIndex)) {
            _foldedLines.value = currentFolded - startLineIndex
        } else {
            _foldedLines.value = currentFolded + startLineIndex
        }
        println("[ViewModel] toggleFold finished for line $startLineIndex. New folded: ${_foldedLines.value}")
        // Note: In this phase, we are not modifying the text yet.
        // In a full implementation, this would trigger text update.
    }

    private fun updateFoldableRegions(text: String) {
        viewModelScope.launch(Dispatchers.Default) { // Run analysis off the main thread
            val lines = text.lines()
            val regions = mutableMapOf<Int, IntRange>()
            if (lines.size < 2) {
                _foldableRegions.value = emptyMap()
                return@launch
            }

            val indentations = lines.map { getIndentationLevel(it) }

            for (i in lines.indices) {
                val currentIndent = indentations[i]
                if (i + 1 < lines.size && indentations[i+1] > currentIndent) {
                    // Potential start of a foldable block
                    var endLine = i + 1
                    while (endLine + 1 < lines.size && indentations[endLine + 1] > currentIndent) {
                        endLine++
                    }
                    // Ensure the block is not empty and has valid indentation
                    if (endLine > i) {
                        // Check if the line *after* the block returns to the same or lesser indent
                        val nextLineIndent = if (endLine + 1 < lines.size) indentations[endLine + 1] else -1
                        if (nextLineIndent <= currentIndent) {
                             regions[i] = i..endLine
                        }
                         // More sophisticated logic could handle nested blocks better
                    }
                }
            }
            println("[EditorViewModel] Detected Foldable Regions: $regions") // Add logging
            _foldableRegions.value = regions
        }
    }

    // Simple indentation calculation (spaces or tabs)
    // TODO: Make this more robust (configurable indent size, mixed spaces/tabs)
    private fun getIndentationLevel(line: String): Int {
        val tabSize = 4 // Assume tab width is 4 for now
        var count = 0
        for (char in line) {
            when (char) {
                ' ' -> count++
                '\t' -> count += tabSize - (count % tabSize) // Align to next tab stop
                else -> break // Stop at first non-whitespace character
            }
        }
        return count / tabSize // Return level based on assumed tab size
    }

    // --- End Code Folding Methods ---

    // --- Replace Functionality Methods ---
    fun setReplaceQuery(query: String) {
        _replaceQuery.value = query
    }

    fun replaceCurrent() {
        val results = _searchResults.value
        val index = _currentMatchIndex.value
        val replaceWith = _replaceQuery.value
        val currentText = _textFieldValue.value.text

        if (index in results.indices) {
            val range = results[index]
            val before = currentText.substring(0, range.first)
            val after = currentText.substring(range.last)
            val newText = before + replaceWith + after

            // Calculate the new cursor position after the replacement
            val newCursorPos = range.first + replaceWith.length

            // Update TextFieldValue
            _textFieldValue.value = TextFieldValue(
                text = newText,
                selection = TextRange(newCursorPos)
            )
            // Mark as modified
            _isModified.value = _loadedFileContent.value != newText

            // Important: Re-run the search on the modified text
            // This updates results and potentially moves the current match index
            performSearch()

            // After replacing, try to find the *next* logical match based on the original index
            // This is tricky because performSearch resets the index.
            // A simple approach: if there are matches left, try to go to the one
            // that *would have been* next.
            val oldResultCount = results.size
            val newResults = _searchResults.value
            if (newResults.isNotEmpty()) {
                // Try to find the first match *after* the replaced text's end position
                val nextMatchIndex = newResults.indexOfFirst { it.first >= newCursorPos }
                if (nextMatchIndex != -1) {
                    _currentMatchIndex.value = nextMatchIndex
                } else {
                    // If no match after, wrap around or stay at first/last
                    _currentMatchIndex.value = 0 // Or size - 1, depending on desired wrap behavior
                }
                highlightCurrentMatch() // Highlight the potentially new current match
            } else {
                // No matches left after replacement
                _currentMatchIndex.value = -1
                // Optionally clear selection or keep cursor at end of replacement
                 _textFieldValue.value = _textFieldValue.value.copy(selection = TextRange(newCursorPos))
            }
        }
    }

    fun replaceAll() {
        val findQuery = _searchQuery.value
        val replaceWith = _replaceQuery.value
        val currentText = _textFieldValue.value.text

        if (findQuery.isBlank() || !currentText.contains(findQuery, ignoreCase = true)) {
            return // Nothing to replace
        }

        // Simple replaceAll, case-insensitive
        val newText = currentText.replace(findQuery, replaceWith, ignoreCase = true)

        if (newText != currentText) {
             _textFieldValue.value = TextFieldValue(
                 text = newText,
                 // Reset selection to the beginning after replace all
                 selection = TextRange(0)
             )
             _isModified.value = _loadedFileContent.value != newText
             // Reset search results as they are now invalid
             resetSearchState()
             // Optionally, hide the find bar after replace all
             // _isFindBarVisible.value = false
        }
    }

    // --- End Replace Functionality Methods ---

    /**
     * Reads the content of a file URI using ContentResolver and DocumentFile.
     * Runs on Dispatchers.IO.
     */
    private suspend fun readFileContent(fileUri: Uri): String = withContext(Dispatchers.IO) {
        val contentResolver = application.contentResolver
        contentResolver.openInputStream(fileUri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } ?: throw Exception("Could not open input stream for URI.")
    }

    /**
     * Writes the given content to the specified file URI using ContentResolver.
     * Runs on Dispatchers.IO.
     */
    private suspend fun writeFileContent(fileUri: Uri, content: String) = withContext(Dispatchers.IO) {
        try {
            application.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(content)
                }
            } ?: throw Exception("Could not open output stream for URI.")
        } catch (e: SecurityException) {
            throw Exception("Permission denied when writing to file: ${e.message}", e)
        } catch (e: Exception) {
            throw Exception("Failed to write file content: ${e.message}", e)
        }
    }
}
