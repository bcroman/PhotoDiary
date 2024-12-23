package com.example.photodiary

/*
Author: Ben Collins 21006366
Date: 20/12/2024
Version: 1.0
Project: PhotoDairy
 */

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.photodiary.databinding.ActivityMainBinding
import com.example.photodiary.viewmodel.PhotoViewModel

/*
Main activity class, is landing page
Handles app permissions
Handles creating 'PhotoDairy' folder
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding //Set ViewBinding Variable
    val PERMISSION_REQUEST_CODE: Int = 101 //Set Permission Request Code
    private lateinit var photoViewModel: PhotoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        photoViewModel = ViewModelProvider(this).get(PhotoViewModel::class.java)

        // Check if this is the first time the app is running
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isFirstInstall = sharedPreferences.getBoolean("isFirstInstall", true)

        if (isFirstInstall) {
            // Clear the database
            photoViewModel.clearDatabase()

            // Update SharedPreferences to mark the first run as completed
            sharedPreferences.edit().putBoolean("isFirstInstall", false).apply()
        }

        //Call CheckPermission Function
        checkPermissions()

        // Create FolderUtils instance
        val folderUtils = FolderUtils()

        //Try and Create 'PhotoDairy' Folder in Photo folder
        try {
            folderUtils.createFolder(this)
        } catch (e: Exception) { //Catch Error Message
            e.printStackTrace()
            Toast.makeText(this, "An error occurred: " + e.message, Toast.LENGTH_SHORT).show()
        }

        //FAB Go Function to open Camera Page
        viewBinding.btnCamera.setOnClickListener{
            val myIntent = Intent(this, CameraActivity::class.java) //Create Intent Variable to open Camera activity
            startActivity(myIntent) //Do MyIntent to open page
        }

        //FAB Go Function to open Gallery Page
        viewBinding.btnGallery.setOnClickListener{
            val myIntent = Intent(this, GalleryActivity::class.java) //Create Intent Variable to open Gallery activity
            startActivity(myIntent) //Do MyIntent to open page
        }
    }

    // Function to check and request necessary permissions
    private fun checkPermissions() {
        // Define the permissions that you want to request (Copy from Manifest File)
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        var allGranted = true // Flag to track if all permissions are already granted

        // Loop through each permission and check if it is granted
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false // If any permission is not granted, set the flag to false
                break // No need to check further, break the loop
            }
        }

        if (!allGranted) {
            // If at least one permission is not granted, request all the permissions
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        } else {
            // If all permissions are already granted, notify the user
            Toast.makeText(this, "All permissions are already granted!", Toast.LENGTH_SHORT).show()
        }
    }


}