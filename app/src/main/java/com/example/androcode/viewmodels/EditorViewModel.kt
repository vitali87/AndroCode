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

    // --- Find Functionality State ---
    private val _isFindBarVisible = MutableStateFlow(false)
    val isFindBarVisible: StateFlow<Boolean> = _isFindBarVisible.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<IntRange>>(emptyList())
    val searchResults: StateFlow<List<IntRange>> = _searchResults.asStateFlow()

    private val _currentMatchIndex = MutableStateFlow(-1) // -1 means no current selection
    val currentMatchIndex: StateFlow<Int> = _currentMatchIndex.asStateFlow()

    // --- End Find Functionality State ---

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
        _textFieldValue.value = newValue
        _currentFileContent.value = newValue.text
        _isModified.value = _loadedFileContent.value != newValue.text
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
    }

    fun findNext() {
        val results = _searchResults.value
        if (results.isEmpty()) return

        var nextIndex = _currentMatchIndex.value + 1
        if (nextIndex >= results.size) {
            nextIndex = 0 // Wrap around to the start
        }
        _currentMatchIndex.value = nextIndex
        // TODO: Need to trigger UI update to scroll/show the new selection
    }

    fun findPrevious() {
        val results = _searchResults.value
        if (results.isEmpty()) return

        var prevIndex = _currentMatchIndex.value - 1
        if (prevIndex < 0) {
            prevIndex = results.size - 1 // Wrap around to the end
        }
        _currentMatchIndex.value = prevIndex
        // TODO: Need to trigger UI update to scroll/show the new selection
    }

    private fun resetSearchState() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _currentMatchIndex.value = -1
        // Keep _isFindBarVisible as is, toggling is handled separately
    }

    // --- End Find Functionality Methods ---

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
