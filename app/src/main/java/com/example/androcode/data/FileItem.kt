package com.example.androcode.data // Adjust package if needed

import android.net.Uri

/**
 * Represents an item (file or directory) in the file explorer.
 *
 * @param name The display name of the file or directory.
 * @param uri The URI identifying the item.
 * @param isDirectory True if the item is a directory, false otherwise.
 * @param lastModified Timestamp of the last modification (optional).
 * @param size Size of the file in bytes (optional, typically for files only).
 */
data class FileItem(
    val name: String,
    val uri: Uri,
    val isDirectory: Boolean,
    val lastModified: Long? = null,
    val size: Long? = null
)
