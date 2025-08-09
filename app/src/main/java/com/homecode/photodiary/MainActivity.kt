package com.homecode.photodiary

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
import com.homecode.photodiary.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var vBinding: ActivityMainBinding //Set ViewBinding Variable
    val PERMISSION_REQUEST_CODE: Int = 101 //Set Permission Request Code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        vBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Call CheckPermission Function
        checkPermissions()

        //Open Camera Activity
        vBinding.fabCamera.setOnClickListener {
            val myIntent = Intent(this, CameraActivity::class.java)
            startActivity(myIntent)
        }
    }

    // Function to check and request necessary permissions
    private fun checkPermissions() {
        // Define the permissions that you want to request (Copy from Manifest File)
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
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