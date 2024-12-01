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
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.photodiary.databinding.ActivityCameraBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityCameraBinding //Set ViewBinding Variable
    private var isFlashEnabled = false // Track flash state
    private var isUsingFrontCamera = false // Track the camera state (default is back camera)

    private lateinit var cameraControl: CameraControl // To control zoom
    private lateinit var cameraInfo: CameraInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize viewBinding
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        //UI Buttons Actions
        viewBinding.fabCapture.setOnClickListener { takePhoto() }
        viewBinding.fabViewswitch.setOnClickListener { switchCamera() }
        viewBinding.fabFlash.setOnClickListener { toggleFlash() }

        //Call PinchZoom Function
        setupPinchToZoom()

        // Initialize Camera Executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        //Call Start Camera
        startCamera()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    //Function to Take Photo
    private fun takePhoto(){
        val imageCapture = imageCapture ?: return
        val filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS"

        val name = SimpleDateFormat(filenameFormat, Locale.UK)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext, "err1", Toast.LENGTH_LONG).show()
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }


    //Function to open the Camera
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Select the camera (front or back)
            val cameraSelector = if (isUsingFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            // Create the Preview Use Case
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // Create the ImageCapture Use Case
            imageCapture = ImageCapture.Builder()
                .setFlashMode(
                    if (isFlashEnabled) ImageCapture.FLASH_MODE_ON
                    else ImageCapture.FLASH_MODE_OFF
                )
                .build()

            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind the lifecycle of cameras to the lifecycle owner
                val camera: Camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                // Retrieve CameraControl and CameraInfo for zoom
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo

            } catch (exc: Exception) {
                Log.e("TAG", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Function to shut down the Camera
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    //Function to enable flash
    private fun toggleFlash() {
        isFlashEnabled = !isFlashEnabled // Toggle flash state

        // Update ImageCapture flash mode
        imageCapture = ImageCapture.Builder()
            .setFlashMode(
                if (isFlashEnabled) ImageCapture.FLASH_MODE_ON
                else ImageCapture.FLASH_MODE_OFF
            )
            .build()

        // Enable or disable torch for live preview
        if (::cameraControl.isInitialized) {
            cameraControl.enableTorch(isFlashEnabled)
        }

        // Optional: Provide user feedback
        val message = if (isFlashEnabled) "Flash Enabled" else "Flash Disabled"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //function to switch camera view
    private fun switchCamera() {
        isUsingFrontCamera = !isUsingFrontCamera // Toggle between front and back cameras

        // Reinitialize the camera
        val cameraSelector = if (isUsingFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            try {
                cameraProvider.unbindAll() // Unbind previous use cases

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                    }

                // Include the current image capture use case
                imageCapture = ImageCapture.Builder().build()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("TAG", "Camera switch failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupPinchToZoom() {
        val scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // Get the current zoom ratio
                val currentZoomRatio = cameraInfo.zoomState.value?.zoomRatio ?: 1f

                // Calculate the new zoom ratio
                val scaleFactor = detector.scaleFactor
                val newZoomRatio = currentZoomRatio * scaleFactor

                // Apply the zoom ratio using CameraControl
                cameraControl.setZoomRatio(newZoomRatio)

                return true
            }
        })

        // Attach the gesture detector to the PreviewView
        viewBinding.viewFinder.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }
    }
}