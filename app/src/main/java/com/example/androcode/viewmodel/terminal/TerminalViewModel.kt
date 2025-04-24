package com.example.androcode.viewmodel.terminal

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.androcode.permission.PermissionHelper
import androidx.lifecycle.viewModelScope
import com.example.androcode.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

/**
 * ViewModel for handling terminal operations.
 * Manages terminal process, input/output streams, and terminal state.
 */
@HiltViewModel
class TerminalViewModel @Inject constructor(
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var process: Process? = null
    private var outputWriter: BufferedWriter? = null
    private var inputReaderJob: Job? = null
    private var processWaiterJob: Job? = null // Job for waiting process exit
    
    private val _terminalOutput = MutableStateFlow<String>("")
    val terminalOutput: StateFlow<String> = _terminalOutput.asStateFlow()
    
    private val _isTerminalRunning = MutableStateFlow(false)
    val isTerminalRunning: StateFlow<Boolean> = _isTerminalRunning.asStateFlow()
    
    // Terminal has permission check delegated to the permission helper
    
    private val homeDir: String by lazy {
        context.getExternalFilesDir(null)?.absolutePath ?: "/data/data/${context.packageName}"
    }
    
    /**
     * Starts a terminal session with the specified command.
     * If no command is provided, it defaults to a shell.
     * Permission checking is now handled by PermissionHelper.
     * 
     * @param command The command to execute, or null for default shell
     */
    fun startTerminal(command: String? = null) {
        stopTerminal() // Ensure any existing terminal is stopped first
        
        // Check if we have proper permissions
        if (!PermissionHelper.hasStoragePermissions(context)) {
            _terminalOutput.value = "\nStorage permissions not granted.\n" +
                                   "Please grant permissions in the app settings.\n"
            return
        }
        
        viewModelScope.launch(ioDispatcher) {
            try {
                val processBuilder = ProcessBuilder()
                    .command(command ?: "/system/bin/sh")
                    .redirectErrorStream(true) // Merge stderr into stdout
                
                // Set working directory to a location we can access
                processBuilder.directory(File(homeDir))
                
                // Set environment variables to help with permissions
                val env = processBuilder.environment()
                env["HOME"] = homeDir
                env["TMPDIR"] = context.cacheDir.absolutePath
                env["PATH"] = System.getenv("PATH") ?: "/system/bin:/system/xbin:/system/sbin"
                
                process = processBuilder.start()
                _isTerminalRunning.value = true
                
                outputWriter = BufferedWriter(OutputStreamWriter(process!!.outputStream))
                
                // Start reading from the process output
                startProcessOutputReader()
                
                // Initial commands to help with navigation
                sendCommand("cd $homeDir")
                sendCommand("ls -la")
                
                // Start waiting for process exit
                startProcessWaiter()
            } catch (e: Exception) {
                _terminalOutput.value += "\nError starting terminal: ${e.message}\n"
                _isTerminalRunning.value = false
            }
        }
    }
    
    /**
     * Sends the given command input to the terminal process.
     * 
     * @param input The command input to send to the terminal
     */
    fun sendCommand(input: String) {
        viewModelScope.launch(ioDispatcher) {
            try {
                outputWriter?.apply {
                    write(input)
                    newLine()
                    flush()
                }
            } catch (e: Exception) {
                _terminalOutput.value += "\nError sending command: ${e.message}\n"
            }
        }
    }
    
    /**
     * Stops the currently running terminal process and cleans up resources.
     */
    fun stopTerminal() {
        viewModelScope.launch(ioDispatcher) {
            inputReaderJob?.cancel()
            processWaiterJob?.cancel()
            
            try {
                outputWriter?.close()
                process?.destroy()
            } catch (e: Exception) {
                // Ignore cleanup errors
            } finally {
                process = null
                outputWriter = null
                inputReaderJob = null
                processWaiterJob = null
                _isTerminalRunning.value = false
            }
        }
    }
    
    /**
     * Starts a coroutine to continuously read output from the process.
     */
    private fun startProcessOutputReader() {
        inputReaderJob = viewModelScope.launch(ioDispatcher) {
            process?.let { proc ->
                val reader = BufferedReader(InputStreamReader(proc.inputStream))
                try {
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        line?.let {
                            _terminalOutput.value += "$it\n"
                        }
                    }
                } catch (e: Exception) {
                    if (isTerminalRunning.value) {
                        _terminalOutput.value += "\nError reading terminal output: ${e.message}\n"
                    }
                }
            }
        }
    }
    
    /**
     * Starts a coroutine to wait for the process to exit.
     */
    private fun startProcessWaiter() {
        processWaiterJob = viewModelScope.launch(ioDispatcher) {
            try {
                val exitValue = process?.waitFor() ?: return@launch
                _terminalOutput.value += "\nProcess exited with code: $exitValue\n"
                _isTerminalRunning.value = false
            } catch (e: Exception) {
                if (isTerminalRunning.value) {
                    _terminalOutput.value += "\nError waiting for process: ${e.message}\n"
                    _isTerminalRunning.value = false
                }
            }
        }
    }
    
    /**
     * Clears the terminal output.
     */
    fun clearTerminalOutput() {
        _terminalOutput.value = ""
    }
    
    /**
     * Restarts the terminal after permissions have been granted.
     * Called to refresh the terminal state when permissions change.
     */
    fun refreshTerminal() {
        if (PermissionHelper.hasStoragePermissions(context)) {
            startTerminal()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTerminal()
    }
}
