package com.example.photodiary

import android.content.ContentValues
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

class CameraActivity : AppCompatActivity() {

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

        viewBinding.fabCapture.setOnClickListener { takePhoto() }
        viewBinding.fabViewswitch.setOnClickListener { switchCamera() }
        viewBinding.fabFlash.setOnClickListener { toggleFlash() }

        setupPinchToZoom()

        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val cameraSelector = if (isUsingFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(
                    if (isFlashEnabled) ImageCapture.FLASH_MODE_ON
                    else ImageCapture.FLASH_MODE_OFF
                )
                .build()

            try {
                cameraProvider.unbindAll()

                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo
            } catch (exc: Exception) {
                Log.e("CameraActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    //Function to take photo, save img to project folder, save details to database and open preview page
    private fun takePhoto() {
        // Safeguard: Ensure imageCapture is initialized before proceeding
        val imageCapture = imageCapture ?: return

        // Generate a unique file name using the current timestamp
        val filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS"
        val name = SimpleDateFormat(filenameFormat, Locale.UK)
            .format(System.currentTimeMillis())

        // Prepare content values for saving the image in MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name) // File name
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png") // File type
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                // Specify the relative path for saving the image on newer devices (API 29+)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PhotoDiary")
            }
        }

        // Set up the output options for ImageCapture
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        // Capture the image
        imageCapture.takePicture(
            outputOptions, // Output configuration
            ContextCompat.getMainExecutor(this), // Executor to run callbacks on the main thread
            object : ImageCapture.OnImageSavedCallback {

                // Called when there is an error capturing the image
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraActivity", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(baseContext, "Photo capture failed: ${exc.message}", Toast.LENGTH_LONG).show()
                }

                // Called when the image is successfully saved
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Retrieve the URI of the saved image
                    val fileUri = output.savedUri
                    val filePath = fileUri?.toString() ?: "Unknown Path"

                    // Prepare a default Photo entity for database insertion
                    val newPhoto = Photo(
                        filePath = filePath, // Path to the saved image
                        title = "New Photo", // Default title (user will edit this later)
                        description = "Test Notes: Photo captured at $name" // Default description
                    )

                    // Insert the photo into the database using ViewModel
                    photoViewModel.insert(newPhoto)

                    // Display a success message
                    val msg = "Photo saved: $filePath"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d("CameraActivity", msg)
                }
            }
        )
    }


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

    private fun switchCamera() {
        isUsingFrontCamera = !isUsingFrontCamera
        startCamera() // Reinitialize the camera with the new camera selector
    }

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
