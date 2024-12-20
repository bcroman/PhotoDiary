package com.example.photodiary

/*
Author: Ben Collins 21006366
Date: 20/12/2024
Version: 1.0
Project: PhotoDairy
 */

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.photodiary.data.Photo
import com.example.photodiary.databinding.ActivityCameraBinding
import com.example.photodiary.viewmodel.PhotoViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/*
Camera activity class
Handles Start Camera
Handles TakePhoto Camera
Handles Zoom Camera
Handles Switch Zoom Camera
Handles Toggle Flash Camera
Handles save to folder and database
 */
class CameraActivity : AppCompatActivity() {

    //Set Variables
    private lateinit var viewBinding: ActivityCameraBinding
    private var isFlashEnabled = false
    private var isUsingFrontCamera = false
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var photoViewModel: PhotoViewModel // ViewModel instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        photoViewModel = ViewModelProvider(this).get(PhotoViewModel::class.java)

        // Set up button click listeners
        viewBinding.fabCapture.setOnClickListener { takePhoto() }
        viewBinding.fabViewswitch.setOnClickListener { switchCamera() }
        viewBinding.fabFlash.setOnClickListener { toggleFlash() }

        // Set up pinch-to-zoom functionality
        setupPinchToZoom()

        cameraExecutor = Executors.newSingleThreadExecutor() // Initialize camera executor

        // Start the camera
        startCamera()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    //Lifecycle method to clean up resources when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    //Starts the camera preview and sets up image capture functionality
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Select the appropriate camera
            val cameraSelector = if (isUsingFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            // Set up preview use case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // Set up image capture use case
            imageCapture = ImageCapture.Builder()
                .setFlashMode(
                    if (isFlashEnabled) ImageCapture.FLASH_MODE_ON
                    else ImageCapture.FLASH_MODE_OFF
                )
                .build()

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to the lifecycle
                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // Initialize camera control and info
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    //Function to take photo, save img (png format) to project folder, save details to database and open preview page
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Generate a unique file name
        val filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS"
        val name = SimpleDateFormat(filenameFormat, Locale.UK).format(System.currentTimeMillis())

        // Prepare content values for saving the image
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PhotoDiary")
            }
        }

        // Configure output options
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        // Capture the image
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraActivity", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(baseContext, "Photo capture failed: ${exc.message}", Toast.LENGTH_LONG).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val fileUri = output.savedUri
                    val filePath = fileUri?.toString() ?: "Unknown Path"

                    // Create a new Photo entity
                    val newPhoto = Photo(
                        filePath = filePath,
                        title = "New Photo",
                        description = "Test Notes: Photo captured at $name"
                    )

                    // Insert into database and retrieve photoID
                    photoViewModel.insert(newPhoto) { newPhotoID ->
                        val myIntent = Intent(this@CameraActivity, ImagePreviewActivity::class.java).apply {
                            putExtra("imageUri", filePath)
                            putExtra("photoID", newPhotoID.toInt())
                        }
                        startActivity(myIntent)
                    }

                    Toast.makeText(baseContext, "Photo saved successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    //Toggles the flash state and restarts the camera to apply changes
    private fun toggleFlash() {
        if (::cameraInfo.isInitialized && cameraInfo.hasFlashUnit()) {
            isFlashEnabled = !isFlashEnabled

            // Restart the camera to apply flash mode changes
            startCamera()

            val message = if (isFlashEnabled) "Flash Enabled" else "Flash Disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Flash not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    //Switches between the front and back cameras views
    private fun switchCamera() {
        isUsingFrontCamera = !isUsingFrontCamera
        startCamera()
    }

    //Sets up pinch-to-zoom functionality for the camera
    @SuppressLint("ClickableViewAccessibility")
    private fun setupPinchToZoom() {
        val scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = cameraInfo.zoomState.value?.zoomRatio ?: 1f
                val newZoomRatio = currentZoomRatio * detector.scaleFactor
                cameraControl.setZoomRatio(newZoomRatio)
                return true
            }
        })

        viewBinding.viewFinder.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }
    }
}
