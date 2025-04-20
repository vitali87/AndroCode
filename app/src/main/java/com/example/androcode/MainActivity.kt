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

// --- Moved EditorView to top-level --- //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorView(
    modifier: Modifier = Modifier,
    editorViewModel: EditorViewModel // Pass the whole ViewModel
) {
    val textState by editorViewModel.textFieldValue.collectAsState()
    val isLoading by editorViewModel.isLoadingContent.collectAsState()
    val openedFileUri by editorViewModel.openedFileUri.collectAsState()
    val scrollState = rememberScrollState() // Shared scroll state for editor + gutter
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val interactionSource = remember { MutableInteractionSource() } // Keep interaction source
    val coroutineScope = rememberCoroutineScope() // Needed for pointerInput
    var isAltKeyPressed by remember { mutableStateOf(false) } // State for Alt key
    // Hoist gutterWidth state
    var gutterWidth by remember { mutableStateOf(0.dp) }

    // Code Folding State
    val foldableRegions by editorViewModel.foldableRegions.collectAsState()
    val foldedLines by editorViewModel.foldedLines.collectAsState()
    // Bracket Matching State
    val matchingBrackets by editorViewModel.matchingBracketPair.collectAsState()

    // Define editor text style centrally - no need for remember here,
    // Compose handles recomposition based on theme changes.
    val editorTextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface // Directly use theme color
    )

    // Effect to scroll to the selection when triggered by the ViewModel
    LaunchedEffect(Unit) {
        editorViewModel.scrollToSelectionEvent.collect {
            coroutineScope.launch {
                textLayoutResult?.let { layoutResult ->
                    val selection = textState.selection
                    if (selection.collapsed) { // Only scroll if it's a cursor
                        val cursorRect = layoutResult.getCursorRect(selection.start)
                        // Basic scroll logic: scroll vertically to bring the cursor into view
                        // More sophisticated logic could check if it's already visible.
                        val line = layoutResult.getLineForOffset(selection.start)
                        val lineTop = layoutResult.getLineTop(line)
                        // Approximate scroll position - might need adjustment
                        scrollState.animateScrollTo(lineTop.toInt())
                    }
                    // TODO: Add logic for non-collapsed selections if needed (e.g., scroll to start)
                }
            }
        }
    }

    Column(modifier = modifier) {
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
            // Core editor layout using BasicTextField and decorationBox
            BasicTextField(
                value = textState,
                onValueChange = {
                    // Basic check to prevent potential crash with invalid TextLayoutResult
                    // In a real scenario, more robust error handling might be needed
                    if (it.text.length <= 100000) { // Arbitrary limit, adjust as needed
                         editorViewModel.onTextFieldValueChange(it)
                    } else {
                        // Handle oversized text - perhaps show a message or trim
                        // For now, just reset to previous valid state to avoid crash
                        editorViewModel.onTextFieldValueChange(
                            textState.copy(text = textState.text.substring(0, 100000))
                        )
                         println("Text length limit reached.") // Log or show snackbar
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ensure it takes available vertical space
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                    .onKeyEvent { keyEvent: KeyEvent -> // Handle key events for Alt
                        if (keyEvent.key == Key.AltLeft || keyEvent.key == Key.AltRight) {
                            isAltKeyPressed = keyEvent.type == KeyEventType.KeyDown
                            true // Consume the event
                        } else {
                            false // Don't consume other key events
                        }
                    }
                    .drawBehind { // <-- Add drawBehind modifier
                        textLayoutResult?.let { layoutResult ->
                            matchingBrackets?.let { (range1, range2) ->
                                val highlightColor = Color.Gray.copy(alpha = 0.3f) // Subtle highlight
                                // Calculate the horizontal offset caused by gutter and spacer
                                val horizontalOffsetPx = gutterWidth.toPx() + 8.dp.toPx()

                                // Validate ranges before attempting to get bounding boxes
                                val textLength = layoutResult.layoutInput.text.length
                                if (range1.end <= textLength && range2.end <= textLength) { // Use range.end for safety
                                    val rect1 = layoutResult.getBoundingBox(range1.start)
                                    val rect2 = layoutResult.getBoundingBox(range2.start)
                                    // Draw slightly larger rects, shifted by the horizontal offset
                                    drawRect(
                                        highlightColor,
                                        topLeft = rect1.topLeft.copy(x = rect1.topLeft.x + horizontalOffsetPx),
                                        size = rect1.size.copy(width = rect1.size.width + 1.sp.toPx())
                                    )
                                    drawRect(
                                        highlightColor,
                                        topLeft = rect2.topLeft.copy(x = rect2.topLeft.x + horizontalOffsetPx),
                                        size = rect2.size.copy(width = rect2.size.width + 1.sp.toPx())
                                    )
                                } else {
                                     println("Warn: Bracket range outside text length, skipping draw.")
                                }
                            }
                        }
                    },
                textStyle = editorTextStyle,
                onTextLayout = { result ->
                    textLayoutResult = result // Capture layout result for gutter and scrolling
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None), // <-- Disable auto-capitalization
                interactionSource = interactionSource, // Pass interaction source
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), // <-- Explicitly set cursor color
                visualTransformation = CodeFoldingTransformation(foldableRegions, foldedLines), // <-- Apply folding transformation
                decorationBox = { innerTextField -> // The crucial decorationBox lambda
                    Row(
                        Modifier
                            .padding(vertical = 4.dp)
                    ) {
                        // Line Number Gutter Composable
                        gutterWidth = LineNumberGutterInternal(
                            modifier = Modifier
                                .fillMaxHeight(), // Gutter should fill height
                            textLayoutResult = textLayoutResult,
                            textStyle = editorTextStyle, // Pass text style for metrics
                            scrollState = scrollState, // Pass the *shared* scroll state
                            foldableRegions = foldableRegions, // Pass folding state
                            foldedLines = foldedLines,       // Pass folding state
                            onToggleFold = { lineIndex -> // Pass toggle callback
                                editorViewModel.toggleFold(lineIndex)
                            }
                        )

                        // Explicit spacer between gutter and text field
                        Spacer(modifier = Modifier.width(8.dp))

                        // The actual text field content, wrapped for scrolling
                        Box(modifier = Modifier
                            .weight(1f) // Take remaining width
                            .verticalScroll(scrollState) // Use the *shared* scroll state
                            .clipToBounds() // Clip content within the Box
                            .drawBehind { // <-- Draw indentation guides behind text
                                textLayoutResult?.let { layoutResult ->
                                    // Get Paint object to measure text accurately
                                    val textPaint = Paint().apply {
                                        isAntiAlias = true
                                        typeface = Typeface.MONOSPACE // Ensure consistent font
                                        textSize = editorTextStyle.fontSize.toPx() // Use editor's font size
                                    }
                                    val spaceWidthPx = textPaint.measureText(" ")
                                    val indentSizeSpaces = 4 // TODO: Make configurable
                                    val indentStepPx = spaceWidthPx * indentSizeSpaces
                                    val guideColor = Color.Gray.copy(alpha = 0.2f)

                                    if (indentStepPx <= 0) return@drawBehind // Avoid issues if space width is zero

                                    // Optimize: Calculate visible line range
                                    val firstVisibleLine = layoutResult.getLineForVerticalPosition(0f)
                                    val lastVisibleLine = layoutResult.getLineForVerticalPosition(size.height)
                                    // Add a buffer in case of partial lines, ensure range is valid
                                    val startLine = maxOf(0, firstVisibleLine - 1)
                                    val endLine = minOf(layoutResult.lineCount - 1, lastVisibleLine + 1)

                                    // Draw guides only for the visible lines
                                    for (lineIndex in startLine..endLine) {
                                        val lineStartOffset = layoutResult.getLineStart(lineIndex)
                                        val lineEndOffset = layoutResult.getLineEnd(lineIndex)
                                        // Check for invalid range before substring
                                        if (lineStartOffset >= lineEndOffset) continue 
                                        val lineText = textState.text.substring(lineStartOffset, lineEndOffset)
                                        val leadingSpaces = lineText.takeWhile { it == ' ' }.count()
                                        // Calculate indent level based on configured size
                                        val indentLevel = leadingSpaces / indentSizeSpaces 

                                        val lineTop = layoutResult.getLineTop(lineIndex)
                                        val lineBottom = layoutResult.getLineBottom(lineIndex)

                                        for (level in 1..indentLevel) {
                                            val xOffset = level * indentStepPx
                                            drawLine(
                                                color = guideColor,
                                                start = Offset(xOffset, lineTop),
                                                end = Offset(xOffset, lineBottom),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                        }
                                    }
                                }
                            }
                        ) {
                            innerTextField() // **MUST call innerTextField() here**
                        }
                    }
                }
            )
        }

        // Placeholder for FindBar if needed later
        val isFindBarVisible by editorViewModel.isFindBarVisible.collectAsState()
        if (isFindBarVisible) {
            FindBar(
                editorViewModel = editorViewModel,
                onClose = { editorViewModel.toggleFindBarVisibility() }
            )
        }
    }
}

// --- Refactored LineNumberGutterInternal ---
@Composable
private fun LineNumberGutterInternal(
    modifier: Modifier = Modifier,
    textLayoutResult: TextLayoutResult?,
    textStyle: TextStyle, // Receive textStyle for accurate metrics
    scrollState: ScrollState, // Receive the *shared* scroll state
    foldableRegions: Map<Int, IntRange>, // Receive foldable regions
    foldedLines: Set<Int>,               // Receive set of folded lines
    onToggleFold: (Int) -> Unit         // Callback to toggle fold state
): Dp { // <-- Return the calculated Dp width
    val density = LocalDensity.current
    val gutterColor = MaterialTheme.colorScheme.outline
    val markerColor = MaterialTheme.colorScheme.primary // Use a distinct color for testing

    // --- Calculate context-dependent values DIRECTLY (no unnecessary remember) --- //
    val textSizePx: Float
    val lineNumPaddingPx: Float
    val gutterPaddingPx: Float
    val gutterWidthDp: Dp
    val markerSizePx: Float
    val markerPaddingPx: Float
    with(density) {
        textSizePx = textStyle.fontSize.toPx()
        lineNumPaddingPx = 4.dp.toPx() // Keep right padding inside gutter
        gutterPaddingPx = 2.dp.toPx()  // Reduced left padding further (from 4.dp)
        // Calculate marker size and padding based on font size
        markerSizePx = textSizePx * 0.7f // Slightly larger marker
        markerPaddingPx = 4.dp.toPx() // More padding around the marker
    }
    val gutterColorArgb = gutterColor.toArgb()
    val markerColorArgb = markerColor.toArgb()

    // Remember Paint object, keyed by primitive values
    val lineNumberPaint = remember(textSizePx, gutterColorArgb) {
        Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
            typeface = Typeface.MONOSPACE
            textSize = textSizePx
            color = gutterColorArgb
        }
    }

    val maxLineNumberText = remember(textLayoutResult?.lineCount) {
        (textLayoutResult?.lineCount ?: 1).toString()
    }
    val maxLineNumberWidthPx = remember(lineNumberPaint, maxLineNumberText) {
        lineNumberPaint.measureText(maxLineNumberText)
    }

    // Separate Paint for marker for potential styling later
    val markerPaint = remember(textSizePx, markerColorArgb) { // Keyed by marker color
         Paint().apply {
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER // Center align marker
            typeface = Typeface.MONOSPACE
            textSize = markerSizePx // Use marker size
            color = markerColorArgb // Use marker color
        }
    }

    // Calculate final gutter width in Dp, accounting for marker and line number areas
    with(density) {
        // val lineNumberWidthPx = lineNumberPaint.measureText(maxLineNumberText)
        // Width = LeftPadding + MarkerWidth + SpaceBetween + LineNumberWidth + RightPadding
        // Simplified: GutterPadding + MarkerWidth + Space + LineNumberWidth + Padding
        val spaceBetweenMarkerAndNumber = 4.dp.toPx()
        val totalWidthPx = gutterPaddingPx + markerSizePx + spaceBetweenMarkerAndNumber + maxLineNumberWidthPx + lineNumPaddingPx
        gutterWidthDp = totalWidthPx.toDp()
    }

    Canvas(
        modifier = modifier
            .width(gutterWidthDp) // Use NEW calculated Dp width
            .clipToBounds() // <-- Moved clipToBounds AFTER pointerInput
            .pointerInput(Unit) { // <-- Moved pointerInput HERE
                detectTapGestures { offset ->
                    textLayoutResult?.let { layoutResult ->
                        val lineNumber = getLineForPosition(offset.y, layoutResult)
                        if (lineNumber != -1 && foldableRegions.containsKey(lineNumber)) {
                            // Use the variables directly available in this scope
                            val markerCenterX = gutterPaddingPx + markerSizePx / 2f
                            // Increase hit area slightly for easier tapping
                            val hitAreaPadding = 4.dp.toPx()
                            val markerHitAreaStartX = markerCenterX - (markerSizePx / 2f) - hitAreaPadding
                            val markerHitAreaEndX = markerCenterX + (markerSizePx / 2f) + hitAreaPadding
                            println("[Gutter Click] OffsetX: ${offset.x}, HitArea: $markerHitAreaStartX..$markerHitAreaEndX (Line: $lineNumber)")
                            // Check if the tap is within the calculated hit area
                            if (offset.x >= markerHitAreaStartX && offset.x <= markerHitAreaEndX) {
                                println("[Gutter Click] Hit detected! Calling onToggleFold for line $lineNumber")
                                onToggleFold(lineNumber) // Call the passed lambda
                            } else {
                                println("[Gutter Click] Missed hit area for line $lineNumber")
                            }
                        }
                    }
                }
            }
    ) {
        // Marker X is positioned relative to the left edge now
        val markerDrawX = gutterPaddingPx + markerSizePx / 2f
        // Line number X is positioned relative to the right edge
        val lineNumDrawX = size.width - lineNumPaddingPx

        // Log the sizes and positions
        println("[Gutter Draw] Canvas size.width: ${size.width}, gutterWidthDp: ${gutterWidthDp.toPx()}")
        println("[Gutter Draw] MarkerDrawX: $markerDrawX, LineNumDrawX: $lineNumDrawX")

        // Log the received foldable regions map once per draw
        println("[Gutter Debug] Received Foldable Regions: $foldableRegions") // <-- Log received map

        drawIntoCanvas { canvas ->
            textLayoutResult?.let { layoutResult ->
                val lineCount = layoutResult.lineCount
                val fontMetrics = lineNumberPaint.fontMetrics // Get metrics once

                for (lineIndex in 0 until lineCount) {
                    val lineNum = (lineIndex + 1).toString()
                    val lineTop = layoutResult.getLineTop(lineIndex)
                    val lineBottom = layoutResult.getLineBottom(lineIndex)

                    // Center the text vertically
                    val lineCenterY = lineTop + (lineBottom - lineTop) / 2f
                    val textBaselineY = lineCenterY - (fontMetrics.descent + fontMetrics.ascent) / 2f

                    // Draw Fold Marker if applicable
                    val isFoldableStart = foldableRegions.containsKey(lineIndex)
                    // Log check for each line
                    // println(\"[Gutter Debug] Line $lineIndex: isFoldableStart = $isFoldableStart\")

                    if (isFoldableStart) {
                        println("[Gutter] Drawing marker for line $lineIndex") // Add log here
                        val isFolded = foldedLines.contains(lineIndex)
                        val markerChar = if (isFolded) "+" else "-"
                        // Draw the marker slightly left of the line number
                        canvas.nativeCanvas.drawText(
                            markerChar,
                            markerDrawX,
                            textBaselineY, // Align marker baseline with number baseline
                            markerPaint // Use separate marker paint
                        )
                    }

                    // Draw the line number
                    canvas.nativeCanvas.drawText(
                        lineNum,
                        lineNumDrawX, // Use pre-calculated X position
                        textBaselineY,
                        lineNumberPaint
                    )
                }
            }
        }
    }

    return gutterWidthDp // <-- Return the calculated width
}

// Helper function to determine line number from Y-offset
// Removed FontMetrics parameter as it's not needed for this logic
private fun getLineForPosition(y: Float, layoutResult: TextLayoutResult): Int {
    for (lineIndex in 0 until layoutResult.lineCount) {
        val lineTop = layoutResult.getLineTop(lineIndex)
        val lineBottom = layoutResult.getLineBottom(lineIndex)
        if (y >= lineTop && y < lineBottom) {
            return lineIndex
        }
    }
    // If no line contains the Y-coordinate (e.g., click below last line)
    return -1
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

// --- Data class for mapping info ---
data class FoldMappingInfo(
    val originalFoldStartOffset: Int,
    val originalFoldEndOffset: Int,
    val transformedPlaceholderStartOffset: Int,
    val transformedPlaceholderEndOffset: Int
)

// --- Custom OffsetMapping ---
class FoldingOffsetMapping(private val mappingInfoList: List<FoldMappingInfo>) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        var currentDelta = 0
        for (info in mappingInfoList) {
            val foldDelta = (info.transformedPlaceholderEndOffset - info.transformedPlaceholderStartOffset) -
                            (info.originalFoldEndOffset - info.originalFoldStartOffset)

            if (offset <= info.originalFoldStartOffset) {
                // Offset is before this folded region, apply accumulated delta and we're done
                return offset + currentDelta
            }

            if (offset > info.originalFoldStartOffset && offset < info.originalFoldEndOffset) {
                // Offset is strictly *inside* the folded region in the original text.
                // Map it to the start of the placeholder in the transformed text.
                return info.transformedPlaceholderStartOffset + currentDelta
            }

            // Offset is at or after the end of this folded region in the original text.
            // Accumulate the delta from this fold and continue.
            currentDelta += foldDelta
        }
        // Offset is after all folded regions
        return offset + currentDelta
    }

    override fun transformedToOriginal(offset: Int): Int {
        var currentDelta = 0
        for (info in mappingInfoList) {
            val foldDelta = (info.transformedPlaceholderEndOffset - info.transformedPlaceholderStartOffset) -
                            (info.originalFoldEndOffset - info.originalFoldStartOffset)
            val transformedStart = info.transformedPlaceholderStartOffset + currentDelta
            val transformedEnd = info.transformedPlaceholderEndOffset + currentDelta

            if (offset <= transformedStart) {
                // Offset is before this placeholder, apply accumulated delta (in reverse) and we're done
                return offset - currentDelta
            }

            if (offset > transformedStart && offset < transformedEnd) {
                // Offset is strictly *inside* the placeholder in the transformed text.
                // Map it to the start of the original folded region.
                return info.originalFoldStartOffset
            }

            // Offset is at or after the end of this placeholder.
            // Accumulate the delta and continue.
            currentDelta += foldDelta
        }
        // Offset is after all placeholders
        return offset - currentDelta
    }
}

// --- Visual Transformation for Code Folding ---
class CodeFoldingTransformation(
    val foldableRegions: Map<Int, IntRange>,
    val foldedLines: Set<Int>
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        if (foldedLines.isEmpty() || foldableRegions.isEmpty()) {
            // No folding needed, return original text and identity mapping
            return TransformedText(text, OffsetMapping.Identity)
        }

        val originalText = text.text
        val lines = originalText.lines()
        val transformedText = StringBuilder()
        val foldMappings = mutableListOf<FoldMappingInfo>()
        var currentOriginalOffset = 0
        var currentTransformedOffset = 0

        var currentLineIndex = 0
        while (currentLineIndex < lines.size) {
            val line = lines[currentLineIndex]
            val lineLengthWithNewline = line.length + 1 // Account for newline

            if (foldedLines.contains(currentLineIndex) && foldableRegions.containsKey(currentLineIndex)) {
                // This line starts a folded block
                val foldRange = foldableRegions[currentLineIndex]!!
                val placeholder = " {...}\n"
                val placeholderLength = placeholder.length

                // 1. Append the first line (visible part)
                transformedText.append(line)
                transformedText.append('\n') // Add newline for the first visible line
                val firstLineTransformedEndOffset = currentTransformedOffset + lineLengthWithNewline

                // Calculate original offsets for the entire folded block
                val originalFoldStartOffset = currentOriginalOffset
                var originalFoldEndOffset = originalFoldStartOffset + lineLengthWithNewline
                val linesToSkip = foldRange.last - foldRange.first
                for (i in 1..linesToSkip) {
                    if (currentLineIndex + i < lines.size) {
                        originalFoldEndOffset += lines[currentLineIndex + i].length + 1
                    }
                }
                 // Adjust if last line doesn't end with newline in original
                if (currentLineIndex + linesToSkip == lines.size - 1 && !originalText.endsWith('\n')) {
                    originalFoldEndOffset--
                }

                // 2. Append the placeholder
                transformedText.append(placeholder.removeSuffix("\n")) // Append placeholder without newline yet
                val transformedPlaceholderStartOffset = firstLineTransformedEndOffset
                val transformedPlaceholderEndOffset = transformedPlaceholderStartOffset + placeholderLength -1 // -1 for newline

                 // Add newline for placeholder only if block isn't last line OR original ends with newline
                if (foldRange.last < lines.size - 1 || originalText.endsWith('\n')) {
                    transformedText.append('\n')
                    // transformedPlaceholderEndOffset++ // End offset includes newline if added
                }

                // Store mapping info
                foldMappings.add(
                    FoldMappingInfo(
                        originalFoldStartOffset = originalFoldStartOffset,
                        originalFoldEndOffset = originalFoldEndOffset,
                        transformedPlaceholderStartOffset = transformedPlaceholderStartOffset,
                        transformedPlaceholderEndOffset = transformedPlaceholderEndOffset + (if(transformedText.endsWith('\n')) 1 else 0) // Adjust end offset based on newline
                    )
                )

                // Update offsets and skip lines
                currentOriginalOffset = originalFoldEndOffset
                currentTransformedOffset = transformedPlaceholderStartOffset + placeholderLength // Use actual length added
                currentLineIndex += linesToSkip + 1

            } else {
                // This line is not folded - append normally
                transformedText.append(line)
                 // Add newline unless it's the very last line and original text didn't end with one
                if (currentLineIndex < lines.size - 1 || originalText.endsWith('\n')) {
                     transformedText.append('\n')
                     currentOriginalOffset += lineLengthWithNewline
                     currentTransformedOffset += lineLengthWithNewline
                } else {
                    currentOriginalOffset += line.length
                    currentTransformedOffset += line.length
                }
                currentLineIndex++
            }
        }

        // Build the OffsetMapping instance (still has placeholder logic inside)
        val offsetMapping = FoldingOffsetMapping(foldMappings)

        // println("[Folding Transform] Original Length: ${originalText.length}, Transformed Length: ${transformedText.length}") // Removed log
        // TODO: Replace OffsetMapping.Identity with actual mapping logic
        return TransformedText(AnnotatedString(transformedText.toString()), offsetMapping)
    }
}
