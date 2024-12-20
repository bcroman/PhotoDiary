package com.example.photodiary

/*
Author: Ben Collins 21006366
Date: 20/12/2024
Version: 1.0
Project: PhotoDairy
 */

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/*
FolderUtils Class
Handles folder creation:
- For Android 10+ using Scoped Storage.
- For older Android versions using legacy external storage.
- Uses App-Specific Storage as a fallback if Scoped Storage fails.
*/
class FolderUtils {

    private val folderName = "PhotoDairy"

    // Main function to create a folder
    fun createFolder(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val success = createPhotoDairyFolderScoped(context!!)
            if (!success) {
                println("Scoped Storage failed. Falling back to App-Specific Storage.")
                createAppSpecificFolder(context, folderName)
            }
        } else {
            createPhotoDairyFolderLegacy()
        }
    }

    // Function for Android 10+ Scoped Storage
    private fun createPhotoDairyFolderScoped(context: Context): Boolean {
        return try {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, folderName)
                put(MediaStore.MediaColumns.MIME_TYPE, "vnd.android.document/directory")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/$folderName" // Use "Downloads" to avoid Samsung restriction
                )
            }

            val uri = context.contentResolver.insert(
                MediaStore.Files.getContentUri("external"), values
            )

            if (uri != null) {
                println("Folder created successfully in Scoped Storage: Downloads/$folderName")
                true
            } else {
                println("Failed to create folder in Scoped Storage.")
                false
            }
        } catch (e: Exception) {
            println("Error in Scoped Storage: ${e.message}")
            false
        }
    }

    // Function to create app-specific storage folder
    private fun createAppSpecificFolder(context: Context, folderName: String) {
        val folder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), folderName)
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                println("Folder created successfully in App-Specific Storage!")
            } else {
                println("Failed to create folder in App-Specific Storage.")
            }
        } else {
            println("Folder already exists in App-Specific Storage.")
        }
    }

    // Function for Android versions below 10
    private fun createPhotoDairyFolderLegacy() {
        val photosDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val photoDairyDir = File(photosDir, folderName)

        if (!photoDairyDir.exists()) {
            if (photoDairyDir.mkdirs()) {
                println("Folder created successfully in Public Pictures directory!")
            } else {
                println("Failed to create folder in Public Pictures directory.")
            }
        } else {
            println("Folder already exists in Public Pictures directory.")
        }
    }
}
