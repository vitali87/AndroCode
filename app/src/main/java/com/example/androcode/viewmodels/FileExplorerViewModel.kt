package com.example.androcode.viewmodels // Adjust package name if needed

import android.app.Application // Import Application
import android.content.ContentResolver // Import ContentResolver
import android.content.Intent // Import Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile // <-- Import DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androcode.data.FileItem // <-- Import FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers // <-- Import Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow // Import StateFlow
import kotlinx.coroutines.flow.StateFlow // Import StateFlow
import kotlinx.coroutines.flow.asStateFlow // Import asStateFlow
import kotlinx.coroutines.launch // Import launch
import kotlinx.coroutines.withContext // <-- Import withContext
import java.io.IOException // <-- Import IOException
import javax.inject.Inject

@HiltViewModel
class FileExplorerViewModel @Inject constructor(
    private val application: Application // Inject Application context
) : ViewModel() {

    // StateFlow to hold the initially selected ROOT directory URI
    private val _rootDirectoryUri = MutableStateFlow<Uri?>(null)
    val rootDirectoryUri: StateFlow<Uri?> = _rootDirectoryUri.asStateFlow()

    // StateFlow to hold the CURRENT directory URI being viewed
    private val _currentDirectoryUri = MutableStateFlow<Uri?>(null)
    val currentDirectoryUri: StateFlow<Uri?> = _currentDirectoryUri.asStateFlow()

    // StateFlow to hold the list of files/directories in the selected URI
    private val _fileList = MutableStateFlow<List<FileItem>>(emptyList())
    val fileList: StateFlow<List<FileItem>> = _fileList.asStateFlow()

    // StateFlow for loading indicator
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // StateFlow for potential file/directory creation errors
    private val _errorCreatingFile = MutableStateFlow<String?>(null)
    val errorCreatingFile: StateFlow<String?> = _errorCreatingFile.asStateFlow()

    // StateFlow for potential directory selection errors
    private val _errorSelectingDirectory = MutableStateFlow<String?>(null)
    val errorSelectingDirectory: StateFlow<String?> = _errorSelectingDirectory.asStateFlow()

    fun openDirectoryPicker(launcher: ActivityResultLauncher<Uri?>) {
        // The actual launching logic will be triggered from the Composable
        // using the launcher passed in. This ViewModel might hold the state
        // related to the chosen URI later.
        // For ACTION_OPEN_DOCUMENT_TREE, we don't pass an initial URI.
        launcher.launch(null)
    }

    fun onDirectorySelected(uri: Uri?) {
        if (uri != null) {
            // Get ContentResolver from Application context
            val contentResolver = application.contentResolver
            try {
                // Persist read/write permissions
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)

                // Update ViewModel state
                _errorSelectingDirectory.value = null // Clear previous selection error
                _rootDirectoryUri.value = uri // Store the root
                _currentDirectoryUri.value = uri // Set initial current directory
                println("Persisted access. Root URI: $uri") // Placeholder
                loadDirectoryContents(uri) // Load contents after setting URI

            } catch (e: SecurityException) {
                // Handle error: Could not take persistent permission
                val errorMsg = "Failed to get permissions for folder: ${e.message}"
                println(errorMsg) // Keep console log
                _errorSelectingDirectory.value = errorMsg // Set the new state
                _rootDirectoryUri.value = null // Clear root on error
                _currentDirectoryUri.value = null // Clear current on error
                _fileList.value = emptyList() // Clear list on error
            }
        } else {
            // Handle the case where the user cancelled the picker
            println("Directory selection cancelled.") // Placeholder
            _rootDirectoryUri.value = null // Clear root on cancellation
            _currentDirectoryUri.value = null // Clear current on cancellation
            _fileList.value = emptyList() // Clear list on cancellation
        }
    }

    // Navigate into a subdirectory
    fun navigateToDirectory(directoryUri: Uri) {
        // Check if we can read this directory (should already have permission if it's a child)
        val tree = DocumentFile.fromTreeUri(application, directoryUri)
        if (tree != null && tree.isDirectory && tree.canRead()) {
            _currentDirectoryUri.value = directoryUri // Update the CURRENT directory
            loadDirectoryContents(directoryUri)      // Load its contents
        } else {
            // Handle error - cannot navigate or not a directory
            println("Error: Cannot navigate to $directoryUri. Not a readable directory.")
            // TODO: Show error message to user (e.g., via a StateFlow event)
        }
    }

    // Navigate back to the root directory
    fun navigateToRoot() {
        _rootDirectoryUri.value?.let { rootUri ->
            if (_currentDirectoryUri.value != rootUri) { // Avoid reloading if already at root
                 _currentDirectoryUri.value = rootUri
                 loadDirectoryContents(rootUri)
            }
        }
    }

    /**
     * Creates a new, empty file in the current directory.
     * @param fileName The desired name for the new file.
     */
    fun createFile(fileName: String) {
        val currentDir = _currentDirectoryUri.value ?: run {
            _errorCreatingFile.value = "Cannot create file: No directory selected."
            return
        }

        if (fileName.isBlank()) {
            _errorCreatingFile.value = "Cannot create file: File name cannot be empty."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true // Indicate activity
            _errorCreatingFile.value = null // Clear previous error
            try {
                val parentDocument = DocumentFile.fromTreeUri(application, currentDir)
                if (parentDocument == null || !parentDocument.isDirectory || !parentDocument.canWrite()) {
                    throw IOException("Cannot write to the selected directory.")
                }

                // Check for existing file/directory with the same name
                if (parentDocument.findFile(fileName) != null) {
                    throw IOException("File or directory '$fileName' already exists.")
                }

                // Create the file (use a generic MIME type, or determine based on extension later)
                val newFile = parentDocument.createFile("*/*", fileName)
                if (newFile != null) {
                    println("File created successfully: ${newFile.uri}")
                    // Refresh the list to show the new file
                    loadDirectoryContents(currentDir)
                } else {
                    throw IOException("Failed to create file '$fileName'.")
                }
            } catch (e: Exception) {
                println("Error creating file '$fileName': ${e.message}")
                _errorCreatingFile.value = "Error creating file: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates a new directory within the current directory.
     * @param dirName The desired name for the new directory.
     */
    fun createDirectory(dirName: String) {
        val currentDir = _currentDirectoryUri.value ?: run {
            _errorCreatingFile.value = "Cannot create directory: No directory selected."
            return
        }

        if (dirName.isBlank()) {
            _errorCreatingFile.value = "Cannot create directory: Name cannot be empty."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true // Indicate activity
            _errorCreatingFile.value = null // Clear previous error
            try {
                val parentDocument = DocumentFile.fromTreeUri(application, currentDir)
                if (parentDocument == null || !parentDocument.isDirectory || !parentDocument.canWrite()) {
                    throw IOException("Cannot write to the selected directory.")
                }

                // Check for existing file/directory with the same name
                if (parentDocument.findFile(dirName) != null) {
                    throw IOException("File or directory '$dirName' already exists.")
                }

                // Create the directory
                val newDir = parentDocument.createDirectory(dirName)
                if (newDir != null) {
                    println("Directory created successfully: ${newDir.uri}")
                    // Refresh the list to show the new directory
                    loadDirectoryContents(currentDir)
                } else {
                    throw IOException("Failed to create directory '$dirName'.")
                }
            } catch (e: Exception) {
                println("Error creating directory '$dirName': ${e.message}")
                _errorCreatingFile.value = "Error creating directory: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to load directory contents
    private fun loadDirectoryContents(targetDirectoryUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _fileList.value = emptyList() // Clear previous list while loading

            try {
                val documents = withContext(Dispatchers.IO) { // Perform I/O on IO dispatcher
                    val tree = DocumentFile.fromTreeUri(application, targetDirectoryUri)
                    tree?.listFiles()?.mapNotNull { docFile ->
                        // Skip nulls or files that somehow lack names
                        docFile.name?.let { name ->
                            FileItem(
                                name = name,
                                uri = docFile.uri,
                                isDirectory = docFile.isDirectory,
                                lastModified = docFile.lastModified(),
                                size = if (docFile.isFile) docFile.length() else null
                            )
                        }
                    } ?: emptyList() // Return empty list if tree is null or listFiles fails
                }
                // Sort: Directories first, then alphabetically by name
                _fileList.value = documents.sortedWith(
                    compareBy<FileItem> { !it.isDirectory } // Directories first
                        .thenBy { it.name.lowercase() } // Then by name (case-insensitive)
                )
            } catch (e: Exception) {
                // Handle potential exceptions during file listing
                println("Error loading directory contents for $targetDirectoryUri: ${e.message}")
                _fileList.value = emptyList() // Ensure list is empty on error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
