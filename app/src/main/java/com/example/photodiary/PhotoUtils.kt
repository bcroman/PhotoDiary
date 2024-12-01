package com.example.photodiary

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File

object PhotoUtils {
    // Create or retrieve the custom album
    fun getPhotoAlbum(contentResolver: ContentResolver, albumName: String): Uri {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android Q (API 29) and above, use MediaStore to get the album
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        // Add the album directory to MediaStore as a collection
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, albumName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$albumName") // Use relative path
        }

        val albumUri = contentResolver.insert(collection, contentValues)
        return albumUri ?: throw Exception("Failed to create or access album")
    }


    // Add a photo to the gallery
    fun addPhotoToGallery(contentResolver: ContentResolver, photoFile: File, albumName: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.name) // Set the photo file name
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // MIME type
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$albumName") // Define a folder (relative path)
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000) // Date added (in seconds)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis()) // Date taken
        }

        // Insert the image into the MediaStore and get the URI
        try {
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                // If URI is successfully returned, write the photo data
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    photoFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } else {
                Log.e("Gallery", "Failed to add photo to gallery.")
            }
        } catch (e: Exception) {
            Log.e("Gallery", "Error adding photo to gallery", e)
        }
    }

    // Utility function to get the real path from URI
    // Utility function to get the real path from URI
    fun getRealPathFromURI(contentResolver: ContentResolver, uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return ""
    }
}