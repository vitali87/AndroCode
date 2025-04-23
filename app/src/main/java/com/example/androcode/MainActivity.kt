package com.example.androcode // Use your package name

import android.net.Uri // <-- Import Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult // <-- Import rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts // <-- Import ActivityResultContracts
import androidx.compose.foundation.clickable // <-- Import clickable
import androidx.compose.foundation.layout.Arrangement // <-- Import Arrangement
import androidx.compose.foundation.layout.Box // <-- Import Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer // <-- Import Spacer
import androidx.compose.foundation.layout.fillMaxSize // <-- Import fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth // <-- Import fillMaxWidth
import androidx.compose.foundation.layout.height // <-- Import height
import androidx.compose.foundation.layout.padding // <-- Import padding
import androidx.compose.foundation.layout.size // <-- Import size
import androidx.compose.foundation.layout.width // <-- Import width
import androidx.compose.foundation.lazy.LazyColumn // <-- Import LazyColumn
import androidx.compose.foundation.lazy.items // <-- Import items
import androidx.compose.foundation.text.BasicTextField // <-- Import BasicTextField
import androidx.compose.material.icons.Icons // Base import
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Save // Correct import for Save
import androidx.compose.material.icons.filled.Search // <-- Add import
import androidx.compose.material3.Button // <-- Import Button
import androidx.compose.material3.CircularProgressIndicator // <-- Import CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider // <-- Import HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon // <-- Import Icon
import androidx.compose.material3.IconButton // <-- Import IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost // <-- Import SnackbarHost
import androidx.compose.material3.SnackbarHostState // <-- Import SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRow // <-- Import TabRow
import androidx.compose.material3.Tab // <-- Import Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // <-- Import LaunchedEffect
import androidx.compose.runtime.State // Add this import
import androidx.compose.runtime.collectAsState // <-- Import collectAsState
import androidx.compose.runtime.getValue // <-- Import getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope // <-- Import rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment // <-- Import Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview // <-- Import Preview
import androidx.compose.ui.unit.dp // <-- Import dp
import androidx.compose.ui.unit.Dp // <-- Add this import
import androidx.compose.material3.AlertDialog // <-- Import AlertDialog
import androidx.compose.material3.TextButton // <-- Import TextButton
import com.example.androcode.data.FileItem // <-- Import FileItem
import com.example.androcode.ui.theme.AndroCodeTheme // Adjust import
import com.example.androcode.viewmodels.FileExplorerViewModel // <-- Import ViewModel
import com.example.androcode.viewmodels.EditorViewModel // <-- Import EditorViewModel
import androidx.compose.foundation.border // <-- Import border
import androidx.compose.foundation.interaction.MutableInteractionSource // Required for BasicTextField
import androidx.compose.material3.LocalTextStyle // Get default text style
import androidx.compose.material3.TextFieldDefaults // For padding/colors if needed
import androidx.compose.foundation.Canvas // Import Canvas
import androidx.compose.ui.graphics.nativeCanvas // Import nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas // Import drawIntoCanvas
import android.graphics.Paint // Import Paint for drawing text
import androidx.compose.ui.platform.LocalDensity // Import LocalDensity
import android.graphics.Typeface // Required for Paint typeface
import androidx.compose.runtime.derivedStateOf // For efficient calculation
import androidx.compose.ui.draw.clipToBounds // Ensure gutter doesn't draw over border
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch // <-- Import launch
import androidx.hilt.navigation.compose.hiltViewModel // <-- Import hiltViewModel
import androidx.compose.foundation.background // <-- Import background
import androidx.compose.foundation.layout.fillMaxHeight // <-- Import fillMaxHeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily // Needed for Monospace
import androidx.compose.foundation.rememberScrollState // For scrolling
import androidx.compose.foundation.verticalScroll // For scrolling
import androidx.compose.ui.text.TextLayoutResult // Required for line height calculations
import androidx.compose.ui.unit.sp // For explicit text size
import androidx.compose.material3.OutlinedTextField // <-- Add import for Dialog usage
import androidx.compose.ui.graphics.Color // Ensure Color import is present
import androidx.compose.ui.graphics.toArgb // <-- Add this import
import androidx.compose.ui.text.TextRange // For resetting cursor on error
import androidx.compose.ui.input.pointer.pointerInput // <-- Add import
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.input.key.Key // <-- Add import
import androidx.compose.ui.input.key.KeyEvent // <-- Add import
import androidx.compose.ui.input.key.KeyEventType // <-- Correct import path
import androidx.compose.ui.input.key.isAltPressed // <-- Add import
import androidx.compose.ui.input.key.key // <-- Add import
import androidx.compose.ui.input.key.type // <-- Add import
import androidx.compose.foundation.gestures.detectTapGestures // <-- Add import
import androidx.compose.material.icons.filled.Close // <-- Add import
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.unit.min // Add import
import androidx.compose.foundation.layout.IntrinsicSize // <-- Add import
import androidx.compose.foundation.layout.PaddingValues // <-- Add import
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.foundation.text.selection.LocalTextSelectionColors // If customizing selection
import androidx.compose.foundation.text.selection.TextSelectionColors // If customizing selection
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.ScrollState // Ensure ScrollState is imported
import androidx.compose.runtime.remember // Ensure remember is imported
import androidx.compose.runtime.mutableStateOf // Ensure mutableStateOf is imported
import androidx.compose.runtime.getValue // Ensure getValue is imported
import androidx.compose.runtime.setValue // Ensure setValue is imported
import androidx.compose.material.icons.filled.FindReplace // <-- Import FindReplace
import androidx.compose.material3.ButtonDefaults // <-- Import for button colors/padding
import androidx.compose.ui.geometry.Offset // Needed for tap offset
import androidx.compose.ui.graphics.SolidColor // <-- Add import for SolidColor
import androidx.compose.foundation.text.KeyboardOptions // <-- Add import
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.draw.drawBehind // <-- Add import
import androidx.compose.ui.geometry.Rect // <-- Add import

// --- Compose Code Editor Imports (Reverting to original) --- //
import com.wakaztahir.codeeditor.prettify.PrettifyParser // Original import
import com.wakaztahir.codeeditor.theme.CodeThemeType // Original import
import com.wakaztahir.codeeditor.utils.parseCodeAsAnnotatedString // Original import
// --- End Compose Code Editor Imports --- //

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroCodeTheme {
                // Inject ViewModels here if needed at Activity level, or use hiltViewModel() lower down
                val editorViewModel: EditorViewModel = hiltViewModel()
                val fileExplorerViewModel: FileExplorerViewModel = hiltViewModel() // Also get FileExplorer VM
                val isModified by editorViewModel.isModified.collectAsState()
                val openedFileUri by editorViewModel.openedFileUri.collectAsState()
                val currentContent by editorViewModel.currentFileContent.collectAsState() // Get current content
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Launcher for "Save As..." (creating a new file)
                val createFileLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("*/*") // Allow any MIME type initially
                ) { uri: Uri? ->
                    if (uri != null) {
                        // Call the ViewModel function to perform the actual save
                        editorViewModel.saveFileAs(uri)
                    } else {
                        // Handle cancellation or failure
                        scope.launch {
                            snackbarHostState.showSnackbar("Save As cancelled.")
                        }
                    }
                }

                // Handle Save Errors (including potential Save As errors via saveFileAs)
                val errorSaving by editorViewModel.errorSaving.collectAsState()
                LaunchedEffect(errorSaving) {
                    errorSaving?.let {
                        scope.launch {
                            snackbarHostState.showSnackbar("Save Error: $it")
                            editorViewModel.clearSaveError() // Optional: Clear error in VM
                        }
                    }
                }

                // Handle File/Directory Creation Errors
                val errorCreating by fileExplorerViewModel.errorCreatingFile.collectAsState()
                LaunchedEffect(errorCreating) {
                    errorCreating?.let {
                        scope.launch {
                            snackbarHostState.showSnackbar("Creation Error: $it")
                            // Optional: Reset error in VM after showing
                        }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }, // Add SnackbarHost
                    topBar = {
                        TopAppBar(
                            title = { Text("AndroCode") },
                            actions = {
                                // Helper function for save logic
                                fun handleSaveClick() {
                                    if (openedFileUri != null) {
                                        // Existing file, just save
                                        editorViewModel.saveFile()
                                    } else {
                                        // New file, trigger "Save As..."
                                        // Suggest a default filename (e.g., "untitled.txt")
                                        // We could make this smarter later based on language detection
                                        val suggestedName = "untitled.txt"
                                        createFileLauncher.launch(suggestedName)
                                    }
                                }

                                // Save Button
                                IconButton(
                                    onClick = { handleSaveClick() }, // Call the helper function
                                    // Enabled whenever content is modified.
                                    // Allows "Save As..." for new (null URI) files.
                                    enabled = isModified
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Save,
                                        contentDescription = "Save File",
                                        tint = if (isModified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // Adjust tint based on enabled state
                                    )
                                }
                                // Find Button
                                IconButton(
                                    onClick = { editorViewModel.toggleFindBarVisibility() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Find in File"
                                    )
                                }
                                // Add other actions like settings, etc. later
                            }
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Pass the ViewModels down
                        AndroCodeShell(
                            fileExplorerViewModel = fileExplorerViewModel,
                            editorViewModel = editorViewModel
                        )
                    }
                }
            }
        }
    }
}

// Function to display a single file/directory item
@Composable
fun FileListItem(fileItem: FileItem, onClick: (FileItem) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(fileItem) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (fileItem.isDirectory) Icons.Filled.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = if (fileItem.isDirectory) "Directory" else "File",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = fileItem.name, style = MaterialTheme.typography.bodyLarge)
    }
    HorizontalDivider(modifier = Modifier.padding(start = 56.dp)) // Indent divider
}

// --- EditorView Refactored for compose-code-editor --- //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorView(
    modifier: Modifier = Modifier,
    editorViewModel: EditorViewModel
) {
    // ViewModel states
    val textState by editorViewModel.textFieldValue.collectAsState()
    val isLoading by editorViewModel.isLoadingContent.collectAsState()
    val openedFileUri by editorViewModel.openedFileUri.collectAsState()
    val detectedLanguage by editorViewModel.detectedLanguage.collectAsState()
    // States for features potentially removed/reimplemented later:
    // val foldableRegions by editorViewModel.foldableRegions.collectAsState()
    // val foldedLines by editorViewModel.foldedLines.collectAsState()
    // val matchingBrackets by editorViewModel.matchingBracketPair.collectAsState()

    // --- Compose Code Editor Setup --- //
    val parser = remember { PrettifyParser() }
    // TODO: Make theme configurable later via Settings
    var themeState by remember { mutableStateOf(CodeThemeType.Monokai) }
    val theme = remember(themeState) { themeState.theme() }
    // --- End Setup --- //

    // Define editor text style (might be partially overridden by library theme)
    val editorTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        // color = MaterialTheme.colorScheme.onSurface // Color might come from theme
    )

    Column(modifier = modifier) {
        // File name header
        Text(
            text = openedFileUri?.lastPathSegment ?: "Editor",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // --- Use OutlinedTextField with library's parser --- //
            OutlinedTextField(
                value = textState, // Use the TextFieldValue from ViewModel
                onValueChange = {
                    // Update ViewModel with new TextFieldValue
                    // Parsing happens here to update the AnnotatedString within TextFieldValue
                    editorViewModel.onTextFieldValueChange(
                        it.copy(
                            annotatedString = parseCodeAsAnnotatedString(
                                parser = parser,
                                theme = theme,
                                lang = detectedLanguage,
                                code = it.text
                            )
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp), // Add some padding
                textStyle = editorTextStyle,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                // REMOVED: onTextLayout, interactionSource (default is fine),
                // REMOVED: visualTransformation (folding)
                // REMOVED: decorationBox (gutter, drawing)
                // REMOVED: cursorBrush (use default or theme's)
            )
        }

        // Find/Replace Bar (kept as is for now)
        val isFindBarVisible by editorViewModel.isFindBarVisible.collectAsState()
        if (isFindBarVisible) {
            FindBar(
                editorViewModel = editorViewModel,
                onClose = { editorViewModel.toggleFindBarVisibility() }
            )
        }
    }
}

// --- REMOVED LineNumberGutterInternal and getLineForPosition --- //
// These were part of the previous custom editor implementation.
// The compose-code-editor library handles line rendering internally.
// A custom gutter might be reintroduced later if needed.
// @Composable private fun LineNumberGutterInternal(...) { ... }
// private fun getLineForPosition(...) { ... }

// ... (TerminalView, AndroCodeShell, FileExplorerView, Dialogs, Previews remain) ...

// --- REMOVED FoldingOffsetMapping and CodeFoldingTransformation --- //
// These implemented code folding in the previous custom editor.
// The compose-code-editor library does not currently support code folding.
// This feature is temporarily removed until folding support is available
// or implemented separately.
// data class FoldMappingInfo(...) { ... }
// class FoldingOffsetMapping(...) : OffsetMapping { ... }
// class CodeFoldingTransformation(...) : VisualTransformation { ... }

// ... (Rest of the file: FindBar, etc.) ...

@Composable
fun TerminalView(modifier: Modifier = Modifier) {
    // Placeholder for Terminal UI
    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Terminal View (Not Implemented)")
    }
}

@Composable
fun AndroCodeShell(
    modifier: Modifier = Modifier,
    fileExplorerViewModel: FileExplorerViewModel, // Receive FileExplorerViewModel
    editorViewModel: EditorViewModel // Receive EditorViewModel
) {
    Row(modifier = modifier.fillMaxSize()) {
        FileExplorerView(
            modifier = Modifier.weight(0.2f).fillMaxHeight(),
            viewModel = fileExplorerViewModel,
            editorViewModel = editorViewModel
        )
        Column(modifier = Modifier.weight(0.8f).fillMaxSize()) {
            EditorView(
                modifier = Modifier.weight(0.7f).fillMaxWidth(),
                editorViewModel = editorViewModel
            )
            HorizontalDivider() // <-- Add divider here
            TerminalView(
                modifier = Modifier.weight(0.3f).fillMaxWidth()
            )
        }
    }
}

@Composable
fun FileExplorerView(
    modifier: Modifier = Modifier,
    viewModel: FileExplorerViewModel, // Receive ViewModel
    editorViewModel: EditorViewModel // Needed to trigger file opening
) {
    // Collect state from ViewModel
    val files by viewModel.fileList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentPath by viewModel.currentDirectoryUri.collectAsState()
    val rootUri by viewModel.rootDirectoryUri.collectAsState()
    val errorCreatingFile by viewModel.errorCreatingFile.collectAsState() // Collect error state
    val errorSelectingDirectory by viewModel.errorSelectingDirectory.collectAsState() // Collect new error state

    // State for controlling dialogs
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showCreateDirDialog by remember { mutableStateOf(false) }

    // Activity Result Launcher for picking the root directory
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        viewModel.onDirectorySelected(uri)
        // TODO: Persist permission for the selected URI
        // context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Always-visible Select Root button at the top
            Button(
                onClick = { directoryPickerLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Root")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween, // Adjust arrangement if needed
                modifier = Modifier.fillMaxWidth()
            ) {
                // Group Title and Up Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateToRoot() },
                        enabled = currentPath != null && rootUri != null && currentPath != rootUri // Correct null check
                    ) {
                        Icon(imageVector = Icons.Filled.Home, contentDescription = "Go to Root")
                    }
                    Spacer(Modifier.width(8.dp)) // Space between icon and text
                    Text(
                        text = "File Explorer",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Create Directory Button
                IconButton(
                    onClick = { showCreateDirDialog = true },
                    enabled = currentPath != null // Enable only when a directory is selected
                ) {
                    Icon(
                        imageVector = Icons.Filled.CreateNewFolder,
                        contentDescription = "Create New Folder",
                        tint = if (currentPath != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                // Create File Button
                IconButton(
                    onClick = { showCreateFileDialog = true },
                    enabled = currentPath != null // Enable only when a directory is selected
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.NoteAdd, // Use AutoMirrored version
                        contentDescription = "Create New File",
                        tint = if (currentPath != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }


            // Display error message if any
            if (errorCreatingFile != null) {
                Text(
                    text = "Error: $errorCreatingFile",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider() // Separator after error
            }

            // Display directory selection error message if any
            if (errorSelectingDirectory != null) {
                Text(
                    text = "Error: $errorSelectingDirectory",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider() // Separator after error
            }

            // Display selected path (optional, very basic)
            // Display current path
            val currentDisplayPath = currentPath?.path?.let {
                it.substringAfterLast(':').ifEmpty { it }
            }
            if (currentDisplayPath != null) {
                Text(
                    text = "Path: $currentDisplayPath", // <-- Display shortened path
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider() // Use HorizontalDivider
            }

            // Display root path
            val rootDisplayPath = rootUri?.path?.let {
                it.substringAfterLast(':').ifEmpty { it }
            }
            if (rootDisplayPath != null) {
                Text(
                    text = "Root: $rootDisplayPath", // Use rootUri
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider() // Use HorizontalDivider
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loading indicator or File list
            Box(
                modifier = Modifier.weight(1f) // Takes remaining space
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    // Simplified empty state logic
                    if (currentPath == null) {
                        Text(
                            "Please open a folder.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (files.isEmpty()) {
                        Text(
                            "Folder is empty.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        if (files.isEmpty() && rootUri != null) { // Use ROOT .value here
                            Text(
                                "Folder is empty or no files found.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(files, key = { it.uri }) { fileItem ->
                                    FileListItem(fileItem = fileItem) { clickedItem ->
                                        if (clickedItem.isDirectory) {
                                            viewModel.navigateToDirectory(clickedItem.uri) // Call the new function
                                        } else {
                                            // Call EditorViewModel to open the file
                                            editorViewModel.openFile(clickedItem.uri)
                                            println("Attempting to open FILE: ${clickedItem.name}") // Keep for logging
                                            // TODO: Implement file opening logic
                                            // For now, just print
                                            println("Clicked FILE: ${clickedItem.name}")
                                        }
                                        // TODO: Handle item click
                                        // If directory, load its contents
                                        // If file, open it in the editor
                                        println("Clicked: ${clickedItem.name} (Is Directory: ${clickedItem.isDirectory})")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs for Creation ---

    // Create File Dialog
    if (showCreateFileDialog) {
        CreateItemDialog(
            dialogTitle = "Create New File",
            label = "File Name",
            onDismiss = { showCreateFileDialog = false },
            onCreate = { name ->
                viewModel.createFile(name)
                showCreateFileDialog = false
            }
        )
    }

    // Create Directory Dialog
    if (showCreateDirDialog) {
        CreateItemDialog(
            dialogTitle = "Create New Folder",
            label = "Folder Name",
            onDismiss = { showCreateDirDialog = false },
            onCreate = { name ->
                viewModel.createDirectory(name)
                showCreateDirDialog = false
            }
        )
    }
}

// --- Reusable Dialog Composable ---
@Composable
fun CreateItemDialog(
    dialogTitle: String,
    label: String,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var textValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (textValue.isNotBlank()) {
                        onCreate(textValue)
                    }
                },
                enabled = textValue.isNotBlank() // Enable button only if name is not empty
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AndroCodePreview() {
    Text("AndroCode Preview (Editor needs ViewModel)")
}

// Preview for FileListItem (if needed, ensure FileItem is accessible or mocked)
@Preview(showBackground = true)
@Composable
fun FileListItemPreview() {
    val dummyFile = FileItem("example.kt", Uri.parse("file://example.kt"), false)
    val dummyFolder = FileItem("src", Uri.parse("file://src"), true)
    AndroCodeTheme {
        Column {
            FileListItem(fileItem = dummyFile) {}
            FileListItem(fileItem = dummyFolder) {}
        }
    }
}

@Preview(showBackground = true, widthDp = 300)
@Composable
fun FileExplorerViewPreview() {
    // Simplified Preview
    AndroCodeTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
            Text("File Explorer View Preview", modifier = Modifier.align(Alignment.Center))
            // Basic structure if needed for layout testing
            Column {
                Row(Modifier.fillMaxWidth().padding(8.dp)) { Text("Header Area")}
                HorizontalDivider()
                LazyColumn(Modifier.weight(1f)) { /* Items would go here */ }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
fun EditorViewPreview() {
    Text("Editor Preview needs ViewModel")
}

@Composable
fun FindBar(
    editorViewModel: EditorViewModel,
    onClose: () -> Unit
) {
    val searchQuery by editorViewModel.searchQuery.collectAsState()
    val replaceQuery by editorViewModel.replaceQuery.collectAsState() // Collect replace query
    val searchResults by editorViewModel.searchResults.collectAsState()
    val currentMatchIndex by editorViewModel.currentMatchIndex.collectAsState()

    val hasResults = searchResults.isNotEmpty()
    val currentMatchDisplay = if (hasResults) currentMatchIndex + 1 else 0
    val totalMatches = searchResults.size
    val canReplace = hasResults && currentMatchIndex != -1
    val canReplaceAll = hasResults

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp, // Add some elevation
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) { // Use Column
            // --- Find Row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp) // Reduced spacing
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { editorViewModel.setSearchQuery(it) },
                    placeholder = { Text("Find") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium // Smaller text
                )

                // Display match count
                if (searchQuery.isNotEmpty()) {
                    Text(
                        text = if (hasResults) "$currentMatchDisplay/$totalMatches" else "0/0", // Shorter format
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }

                // Previous Button
                IconButton(onClick = { editorViewModel.findPrevious() }, enabled = hasResults) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Previous Match")
                }

                // Next Button
                IconButton(onClick = { editorViewModel.findNext() }, enabled = hasResults) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Next Match")
                }

                // Close Button
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Find/Replace Bar")
                }
            }
            // --- Replace Row ---
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                 modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = replaceQuery,
                    onValueChange = { editorViewModel.setReplaceQuery(it) },
                    placeholder = { Text("Replace with") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                // Replace Button (Current)
                Button(
                    onClick = { editorViewModel.replaceCurrent() },
                    enabled = canReplace,
                    contentPadding = PaddingValues(horizontal = 8.dp) // Less padding
                    // Consider adding an icon here e.g., Icons.Filled.FindReplace
                ) {
                    Text("Replace", style = MaterialTheme.typography.labelSmall) // Smaller text
                }

                // Replace All Button
                 Button(
                    onClick = { editorViewModel.replaceAll() },
                    enabled = canReplaceAll,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("All", style = MaterialTheme.typography.labelSmall) // Smaller text
                }
            }
        }
    }
}
