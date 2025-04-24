package com.example.androcode.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class for managing storage permissions in the application.
 */
object PermissionHelper {

    /**
     * Checks if the app has all required storage permissions.
     *
     * @param context Application context
     * @return True if all required permissions are granted, false otherwise
     */
    fun hasStoragePermissions(context: Context): Boolean {
        // For Android 11+ (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        }
        
        // For Android 10 and below
        val readPermission = ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        
        val writePermission = ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        
        return readPermission && writePermission
    }
    
    /**
     * Requests storage permissions for the app.
     *
     * @param activity Current activity
     */
    fun requestStoragePermissions(activity: Activity) {
        // For Android 11+ (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.parse("package:${activity.packageName}")
                intent.data = uri
                activity.startActivity(intent)
            } catch (e: Exception) {
                // Fallback if the above intent doesn't work
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivity(intent)
            }
        } else {
            // For Android 10 and below
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        }
    }
    
    /**
     * Launches the system's app details settings for this app.
     *
     * @param context Application context
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }
    
    // Permission request code for storage permissions
    const val STORAGE_PERMISSION_CODE = 1001
}

/**
 * Composable to handle storage permission requests with proper UX.
 * Shows a dialog when permissions are needed and provides buttons to request them.
 *
 * @param onPermissionGranted Callback when permission is granted
 * @param content Content to show when permission is granted
 */
@Composable
fun WithStoragePermission(
    onPermissionGranted: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (!PermissionHelper.hasStoragePermissions(context)) {
            showPermissionDialog = true
        } else {
            onPermissionGranted()
        }
    }
    
    if (PermissionHelper.hasStoragePermissions(context)) {
        content()
    } else {
        // Permission dialog
        if (showPermissionDialog) {
            StoragePermissionDialog(
                onDismiss = { showPermissionDialog = false },
                onRequestPermission = {
                    showPermissionDialog = false
                    PermissionHelper.requestStoragePermissions(context as Activity)
                }
            )
        }
        
        // Still show placeholder UI when waiting for permission
        PermissionRequiredContent {
            PermissionHelper.requestStoragePermissions(context as Activity)
        }
    }
}

/**
 * Dialog explaining why storage permissions are needed.
 */
@Composable
private fun StoragePermissionDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Storage Permission Required") },
        text = {
            Column {
                Text(
                    "AndroCode needs full access to your device's storage to enable terminal " +
                    "functionality. This allows commands like 'ls', 'cd', and other system " +
                    "operations to work properly."
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "You'll be redirected to the system settings page where you " +
                    "need to enable 'Allow access to manage all files'.",
                    textAlign = TextAlign.Start
                )
            }
        },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Later")
            }
        }
    )
}

/**
 * Placeholder content shown when waiting for permissions to be granted.
 */
@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Storage permission is required for terminal functionality",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Storage Permission")
        }
    }
}
