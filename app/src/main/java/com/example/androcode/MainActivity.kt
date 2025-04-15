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
// import androidx.compose.foundation.text.BasicTextField // No longer needed here
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
import androidx.compose.ui.tooling.preview.Preview // <-- Import Preview
import androidx.compose.ui.unit.dp // <-- Import dp
import androidx.compose.material3.AlertDialog // <-- Import AlertDialog
import androidx.compose.material3.TextButton // <-- Import TextButton
import com.example.androcode.data.FileItem // <-- Import FileItem
import com.example.androcode.ui.theme.AndroCodeTheme // Adjust import
import com.example.androcode.viewmodels.FileExplorerViewModel // <-- Import ViewModel
import com.example.androcode.viewmodels.EditorViewModel // <-- Import EditorViewModel
import androidx.compose.material3.OutlinedTextField // <-- Import OutlinedTextField
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch // <-- Import launch
import androidx.hilt.navigation.compose.hiltViewModel // <-- Import hiltViewModel
import androidx.compose.foundation.background // <-- Import background
import androidx.compose.foundation.layout.fillMaxHeight // <-- Import fillMaxHeight

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
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Handle Save Errors
                val errorSaving by editorViewModel.errorSaving.collectAsState()
                LaunchedEffect(errorSaving) {
                    errorSaving?.let {
                        scope.launch {
                            snackbarHostState.showSnackbar("Save Error: $it")
                            // Optional: Reset error in VM after showing
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
                                // Save Button
                                IconButton(
                                    onClick = { editorViewModel.saveFile() },
                                    enabled = isModified && openedFileUri != null
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Save,
                                        contentDescription = "Save File",
                                        tint = if (isModified && openedFileUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) // Adjust tint based on enabled state
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

// --- Moved EditorView to top-level --- //
@Composable
fun EditorView(
    modifier: Modifier = Modifier,
    editorViewModel: EditorViewModel // Pass the whole ViewModel
) {
    val textState by editorViewModel.textFieldValue.collectAsState()
    val isLoading by editorViewModel.isLoadingContent.collectAsState() // <-- Use isLoadingContent
    val openedFileUri by editorViewModel.openedFileUri.collectAsState()
    val isFindBarVisible by editorViewModel.isFindBarVisible.collectAsState()

    Column(modifier = modifier) {
        // Loading indicator or content
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Use a simple text field for now, without syntax highlighting
            OutlinedTextField(
                value = textState, // Use the TextFieldValue from ViewModel
                onValueChange = { editorViewModel.onContentChange(it) },
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                label = { Text(openedFileUri?.lastPathSegment ?: "Editor") }
            )
        }
        // Find Bar (conditionally displayed)
        if (isFindBarVisible) {
            FindBar(
                editorViewModel = editorViewModel, // Correct parameter name here
                onClose = { editorViewModel.toggleFindBarVisibility() }
            )
        }
    }
}

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
            modifier = Modifier.weight(0.3f), // File Explorer takes 30% width
            viewModel = fileExplorerViewModel,
            editorViewModel = editorViewModel
        )
        // Pane for Editor / Terminal / etc.
        Box(modifier = Modifier.weight(0.7f)) { // Editor takes 70% width
            // Display the Editor View, passing the ViewModel
            EditorView(editorViewModel = editorViewModel) // Correct parameter name here
            // TODO: Add logic to switch between EditorView and TerminalView later
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

                Button(onClick = { directoryPickerLauncher.launch(null) }) {
                    Text("Select Root")
                }
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
    AndroCodeTheme {
        // --- Simplified Preview --- 
        // Avoid complex ViewModel setup for preview, show basic structure
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("File Explorer Area", modifier = Modifier.align(Alignment.Center))
            }
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Text("Editor Area", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
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
    // Simplified Preview
    AndroCodeTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Text("Editor View Preview", modifier = Modifier.align(Alignment.Center))
            // Basic TextField if layout/appearance testing is needed
            OutlinedTextField(
                value = "Sample code...",
                onValueChange = {},
                modifier = Modifier.fillMaxSize().padding(8.dp),
                label = { Text("sample.kt") }
            )
        }
    }
}

@Composable
fun FindBar(
    editorViewModel: EditorViewModel,
    onClose: () -> Unit
) {
    // Basic FindBar structure to use parameters and remove warnings
    Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)) {
        Text("Find: ${editorViewModel.searchQuery.collectAsState().value}", modifier = Modifier.weight(1f))
        Button(onClick = onClose) { Text("Close") }
    }
}
