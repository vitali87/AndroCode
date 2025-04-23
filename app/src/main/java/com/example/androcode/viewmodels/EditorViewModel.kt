package com.example.androcode.viewmodels

import android.app.Application
import android.net.Uri
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
import java.util.ArrayDeque
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.Job
import androidx.compose.ui.text.AnnotatedString
import com.wakaztahir.codeeditor.highlight.model.CodeLang

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    // State for the URI of the currently opened file
    private val _openedFileUri = MutableStateFlow<Uri?>(null)
    val openedFileUri: StateFlow<Uri?> = _openedFileUri.asStateFlow()

    // State for the content *as loaded from the file*
    private val _loadedFileContent = MutableStateFlow<String?>(null)

    // State for the *current content* in the editor (raw text)
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

    // --- DETECTED LANGUAGE STATE --- //
    private val _detectedLanguage = MutableStateFlow<CodeLang>(CodeLang.SQL) // Using SQL as default since it has highlighting for most syntax
    val detectedLanguage: StateFlow<CodeLang> = _detectedLanguage.asStateFlow()
    // --- END DETECTED LANGUAGE STATE --- //

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

    // --- Bracket Matching State ---
    private val _matchingBracketPair = MutableStateFlow<Pair<TextRange, TextRange>?>(null)
    val matchingBracketPair: StateFlow<Pair<TextRange, TextRange>?> = _matchingBracketPair.asStateFlow()
    // --- End Bracket Matching State ---

    // Debounce job for bracket matching
    private var bracketMatchDebounceJob: Job? = null

    init {
        // Debounced bracket matching (remains the same)
        viewModelScope.launch {
            textFieldValue
                .debounce(300L)
                .collect { value ->
                    updateBracketMatching(value)
                }
        }
    }

    /**
     * Attempts to open and read the content of a file identified by its URI.
     */
    fun openFile(uri: Uri) {
        if (uri == _openedFileUri.value && !_isModified.value) { return }
        viewModelScope.launch {
            _isLoadingContent.value = true
            _errorLoadingContent.value = null
            try {
                val content = readFileContent(uri)
                _loadedFileContent.value = content
                _currentFileContent.value = content
                _openedFileUri.value = uri
                _isModified.value = false

                // Detect language and update state
                detectLanguage(uri)

                // Initial TextFieldValue needs to be set AFTER language detection
                // The UI will now be responsible for creating the initial AnnotatedString
                // using the detected language and the parseCodeAsAnnotatedString function.
                // We only provide the raw text and the language.
                _textFieldValue.value = TextFieldValue(content)

                // Update folding regions based on raw content
                updateFoldableRegions(content)

            } catch (e: Exception) {
                 println("Error reading file content for $uri: ${e.message}")
                 _errorLoadingContent.value = "Error loading file: ${e.message}"
                 // Reset state on error
                 _openedFileUri.value = null
                 _loadedFileContent.value = null
                 _currentFileContent.value = null
                 _textFieldValue.value = TextFieldValue("")
                 _detectedLanguage.value = CodeLang.SQL
                 _isModified.value = false
                 _foldableRegions.value = emptyMap()
                 _foldedLines.value = emptySet()
            } finally {
                _isLoadingContent.value = false
            }
        }
    }

    // Call this when the text content changes in the UI
    // NOTE: The UI (MainActivity) will now handle parsing the text for highlighting
    //       based on the detected language and updating the TextFieldValue's AnnotatedString.
    //       This ViewModel just manages the raw text and the selected language.
    fun onTextFieldValueChange(newValue: TextFieldValue) {
        val oldText = _textFieldValue.value.text
        // Update the TextFieldValue state directly. UI listener handles parsing.
        _textFieldValue.value = newValue

        if (oldText != newValue.text) {
            val newText = newValue.text
            _currentFileContent.value = newText // Update raw content
            _isModified.value = _loadedFileContent.value != newText
            // Update folding regions based on raw text change
            // TODO: Consider if folding needs debouncing/sampling again
            updateFoldableRegions(newText)
        }
        // Bracket matching is handled by its own debounced collector in init
    }

    /**
     * Detects the language based on the file extension and updates the state.
     */
    private fun detectLanguage(uri: Uri?) {
        val extension = uri?.lastPathSegment?.substringAfterLast('.', "")
        _detectedLanguage.value = when (extension?.lowercase()) {
            "kt", "java" -> CodeLang.Java
            "js" -> CodeLang.JavaScript
            "py" -> CodeLang.Python
            "css" -> CodeLang.CSS
            "html", "xml" -> CodeLang.XML
            "json" -> CodeLang.JSON
            "md" -> CodeLang.Markdown
            "c" -> CodeLang.C
            "cpp", "cc" -> CodeLang.CPP
            "cs" -> CodeLang.CSharp
            "rb" -> CodeLang.Ruby
            "go" -> CodeLang.Go
            "rs" -> CodeLang.Rust
            "sql" -> CodeLang.SQL
            "sh" -> CodeLang.Bash
            "dart" -> CodeLang.Dart
            "scala" -> CodeLang.Scala
            "yml", "yaml" -> CodeLang.YAML
            else -> CodeLang.SQL // SQL as fallback since it handles general syntax well
        }
        println("Detected language: ${_detectedLanguage.value} for extension: $extension")
    }

    /**
     * Saves the current editor content back to the originally opened file URI.
     * Assumes _openedFileUri is not null.
     */
    fun saveFile() {
        val uriToSave = _openedFileUri.value
        val contentToSave = _currentFileContent.value

        // Check if there's content and a URI to save to
        if (uriToSave != null && contentToSave != null) {
            performSave(uriToSave, contentToSave)
        } else {
            // This path should ideally not be hit if the UI logic is correct
            _errorSaving.value = "Save Error: No file open or no content."
            println("[Save Error] saveFile() called with null URI or content.")
        }
    }

    /**
     * Saves the current editor content to a *new* file URI, typically obtained
     * from an ACTION_CREATE_DOCUMENT intent (Save As).
     * Updates the internal state to reflect the newly saved file.
     */
    fun saveFileAs(newUri: Uri) {
        val contentToSave = _currentFileContent.value
        if (contentToSave != null) {
            performSave(newUri, contentToSave, isSaveAs = true)
        } else {
            _errorSaving.value = "Save As Error: No content to save."
            println("[Save Error] saveFileAs() called with null content.")
        }
    }

    /**
     * Internal helper function to perform the actual file writing and state update.
     * Handles both regular Save and Save As scenarios.
     */
    private fun performSave(uri: Uri, content: String, isSaveAs: Boolean = false) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorSaving.value = null // Clear previous error
            try {
                writeFileContent(uri, content)
                _loadedFileContent.value = content
                _openedFileUri.value = uri
                _isModified.value = false
                if (isSaveAs) {
                    detectLanguage(uri) // Re-detect language on Save As
                    // No need to call updateHighlighting - UI handles it
                }
                println("File ${if (isSaveAs) "saved as" else "saved"} successfully: $uri")
            } catch (e: Exception) {
                val action = if (isSaveAs) "Save As" else "Save"
                println("Error during $action for $uri: ${e.message}")
                _errorSaving.value = "Error during $action: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * Clears the save error message. Can be called from UI after displaying the error.
     */
    fun clearSaveError() {
        _errorSaving.value = null
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

    fun toggleFold(lineIndex: Int) {
        val currentFolded = _foldedLines.value
        // Check if the line is actually foldable before toggling
        if (_foldableRegions.value.containsKey(lineIndex)) {
            if (currentFolded.contains(lineIndex)) {
                _foldedLines.value = currentFolded - lineIndex
                // println("[Folding] Unfolded line: $lineIndex") // Removed log
            } else {
                _foldedLines.value = currentFolded + lineIndex
                // println("[Folding] Folded line: $lineIndex") // Removed log
            }
            // VisualTransformation will react to state change, main TODO is OffsetMapping
        } else {
             println("[Folding] Warn: Attempted to toggle non-foldable line: $lineIndex") // Keep as warning
        }
    }

    /**
     * Analyzes the text content to find foldable regions (multi-line blocks based on {}).
     * Updates the foldableRegions StateFlow.
     */
    private fun updateFoldableRegions(text: String?) {
        if (text == null) {
            _foldableRegions.value = emptyMap()
            return
        }

        viewModelScope.launch(Dispatchers.Default) { // Parse on a background thread
            val regions = mutableMapOf<Int, IntRange>()
            val braceStack = ArrayDeque<Pair<Int, Int>>() // Pair: lineIndex, columnIndex
            val lines = text.lines()

            lines.forEachIndexed { lineIndex, line ->
                line.forEachIndexed { columnIndex, char ->
                    when (char) {
                        '{' -> braceStack.addLast(Pair(lineIndex, columnIndex))
                        '}' -> {
                            if (braceStack.isNotEmpty()) {
                                val (startLine, _) = braceStack.removeLast()
                                val endLine = lineIndex
                                // Add if it spans multiple lines
                                if (endLine > startLine) {
                                    regions[startLine] = startLine..endLine
                                }
                            } else {
                                // Handle unmatched closing braces
                                println("[Folding] Warn: Unmatched closing brace at Line $lineIndex Col $columnIndex")
                            }
                        }
                    }
                }
            }
            // Handle unmatched opening braces left in stack
            if (braceStack.isNotEmpty()) {
                 println("[Folding] Warn: Unmatched opening braces remain at end of file: ${braceStack.size}")
            }

            // Switch back to main thread to update StateFlows safely
            withContext(Dispatchers.Main) {
                val currentRegions = _foldableRegions.value
                if (currentRegions != regions) { // Only update if changed
                    _foldableRegions.value = regions
                     // println("[Folding] Updated foldableRegions: ${regions.size} regions found.") // Removed log
                    // Prune folded lines that no longer correspond to a valid region
                    val validFolded = _foldedLines.value.filter { regions.containsKey(it) }.toSet()
                    if (validFolded != _foldedLines.value) {
                        _foldedLines.value = validFolded
                        // println("[Folding] Pruned folded lines to: ${validFolded.size}") // Removed log
                    }
                }
            }
        }
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

    // --- Bracket Matching Logic ---

    private fun updateBracketMatching(currentValue: TextFieldValue) {
        val selection = currentValue.selection
        val text = currentValue.text
        _matchingBracketPair.value = null // Reset by default

        if (!selection.collapsed || text.isEmpty()) {
            return // Only match based on cursor position
        }

        val cursorPosition = selection.start

        // Check character BEFORE the cursor
        findMatchingBracket(cursorPosition - 1, text)?.let {
            _matchingBracketPair.value = it
            return
        }

        // If no match before, check character AT the cursor
        findMatchingBracket(cursorPosition, text)?.let {
            _matchingBracketPair.value = it
            return
        }
    }

    private fun findMatchingBracket(checkPosition: Int, text: String): Pair<TextRange, TextRange>? {
        if (checkPosition < 0 || checkPosition >= text.length) return null

        val charAtCursor = text[checkPosition]
        val brackets = mapOf('(' to ')', '{' to '}', '[' to ']', ')' to '(', '}' to '{', ']' to '[')

        if (!brackets.containsKey(charAtCursor)) return null // Not a bracket character

        val targetChar = brackets[charAtCursor]!!
        val searchDirection = if ("({[".contains(charAtCursor)) 1 else -1 // Forward for open, backward for close
        var balance = searchDirection
        var currentPosition = checkPosition + searchDirection

        while (currentPosition >= 0 && currentPosition < text.length) {
            val currentChar = text[currentPosition]
            if (currentChar == charAtCursor) {
                balance += searchDirection // Found same type bracket
            } else if (currentChar == targetChar) {
                balance -= searchDirection // Found target bracket
                if (balance == 0) {
                    // Found the match!
                    val range1 = TextRange(checkPosition, checkPosition + 1)
                    val range2 = TextRange(currentPosition, currentPosition + 1)
                    // Ensure consistent order (e.g., opening first)
                    return if (range1.start < range2.start) range1 to range2 else range2 to range1
                }
            }
            currentPosition += searchDirection
        }

        return null // No match found
    }

    // --- End Bracket Matching Logic ---

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
            val contentResolver = application.contentResolver
            // Use "wt" mode to truncate and write (overwrite)
            contentResolver.openOutputStream(fileUri, "wt")?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(content)
                }
            } ?: throw Exception("Could not open output stream for URI.")
        } catch (e: Exception) {
            // Log the specific exception for better debugging
            println("writeFileContent Error: ${e::class.simpleName} - ${e.message}")
            throw e // Re-throw to be caught by the caller (performSave)
        }
    }
}
