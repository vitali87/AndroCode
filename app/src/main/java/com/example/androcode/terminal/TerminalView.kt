package com.example.androcode.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.androcode.permission.PermissionHelper
import com.example.androcode.viewmodel.terminal.TerminalViewModel

/**
 * Composable that renders a terminal interface with output display and command input.
 * 
 * @param modifier The modifier to be applied to the terminal container
 * @param viewModel The terminal view model to handle command execution
 */
@Composable
fun TerminalView(
    modifier: Modifier = Modifier,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val terminalOutput by viewModel.terminalOutput.collectAsState()
    val isRunning by viewModel.isTerminalRunning.collectAsState()
    
    // Get permission state from central helper
    val hasPermissions = PermissionHelper.hasStoragePermissions(androidx.compose.ui.platform.LocalContext.current)
    
    // Terminal content is now simpler since permissions are handled at MainActivity level
    TerminalContent(
        modifier = modifier,
        terminalOutput = terminalOutput,
        isRunning = isRunning,
        onCommandSubmit = viewModel::sendCommand,
        onClearOutput = viewModel::clearTerminalOutput,
        onStartTerminal = viewModel::startTerminal,
        onStopTerminal = viewModel::stopTerminal
    )
    
    // If we just got permissions, refresh the terminal
    LaunchedEffect(hasPermissions) {
        if (hasPermissions && !isRunning) {
            viewModel.refreshTerminal()
        }
    }
}

/**
 * Internal implementation of terminal content display and interaction.
 */
@Composable
private fun TerminalContent(
    modifier: Modifier = Modifier,
    terminalOutput: String,
    isRunning: Boolean,
    onCommandSubmit: (String) -> Unit,
    onClearOutput: () -> Unit,
    onStartTerminal: (String?) -> Unit,
    onStopTerminal: () -> Unit
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val outputLines = terminalOutput.split("\n")
    
    // Automatically scroll to the bottom when new output is added
    LaunchedEffect(outputLines.size) {
        if (outputLines.isNotEmpty()) {
            listState.animateScrollToItem(maxOf(0, outputLines.size - 1))
        }
    }
    
    // Auto-start terminal when the composable is created
    LaunchedEffect(Unit) {
        if (!isRunning) {
            onStartTerminal(null)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF282c34))
            .padding(8.dp)
    ) {
        // Terminal control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TerminalControlButton(
                text = if (isRunning) "Restart" else "Start",
                onClick = { onStartTerminal(null) }
            )
            
            TerminalControlButton(
                text = "Stop",
                onClick = onStopTerminal,
                enabled = isRunning
            )
            
            TerminalControlButton(
                text = "Clear",
                onClick = onClearOutput
            )
            
            Spacer(Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Terminal output display
        TerminalOutputDisplay(
            outputLines = outputLines,
            listState = listState,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Command input field
        OutlinedTextField(
            value = commandInput,
            onValueChange = { commandInput = it },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = Color.White
            ),
            placeholder = { Text("Enter command...") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Send
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (commandInput.isNotEmpty()) {
                        onCommandSubmit(commandInput)
                        commandInput = ""
                    }
                }
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1E1E1E),
                unfocusedContainerColor = Color(0xFF1E1E1E),
                disabledContainerColor = Color(0xFF1E1E1E),
                cursorColor = Color.White,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.Gray,
            ),
            enabled = isRunning
        )
    }
}

/**
 * Displays terminal output in a scrollable list.
 */
@Composable
private fun TerminalOutputDisplay(
    outputLines: List<String>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF1E1E1E))
            .padding(4.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(outputLines.size) { index ->
                Text(
                    text = outputLines[index],
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}



/**
 * Button for terminal control operations.
 */
@Composable
private fun TerminalControlButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = text,
            color = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
