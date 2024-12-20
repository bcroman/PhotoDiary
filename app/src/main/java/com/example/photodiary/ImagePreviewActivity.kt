package com.example.photodiary

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.photodiary.data.Photo
import com.example.photodiary.databinding.ActivityImagePreviewBinding
import com.example.photodiary.viewmodel.PhotoViewModel

class ImagePreviewActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityImagePreviewBinding
    private lateinit var photoViewModel: PhotoViewModel
    private var imageUri: String? = null
    private var photoID: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityImagePreviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize ViewModel
        photoViewModel = ViewModelProvider(this).get(PhotoViewModel::class.java)

        // Retrieve data from the Intent
        imageUri = intent.getStringExtra("imageUri")
        photoID = intent.getIntExtra("photoID", 0) // Retrieve photoID (default to 0 if not passed)

        // Validate retrieved data
        if (imageUri == null || photoID == 0) {
            Toast.makeText(this, "Error loading image or photo ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load the image into the ImageView
        Glide.with(this)
            .load(Uri.parse(imageUri))
            //.placeholder(R.drawable.placeholder) // Optional placeholder image
            .into(viewBinding.imagePreview)

        // Save button click listener
        viewBinding.fabSave.setOnClickListener {
            updatePhoto()
        }

        //Delete button click listener
        viewBinding.fabDelete.setOnClickListener {
            // Call deletePhoto function
            deletePhoto(
                Photo(
                    photoID = photoID,
                    filePath = imageUri,
                    title = viewBinding.txtTitle.text.toString(),
                    description = viewBinding.txtDec.text.toString()
                )
            )
        }
    }

    //Function to update database records
    private fun updatePhoto() {
        val title = viewBinding.txtTitle.text.toString().trim()
        val description = viewBinding.txtDec.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please enter title and description", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare the updated Photo object
        val updatedPhoto = Photo(
            photoID = photoID, // Use the existing ID to update the record
            filePath = imageUri,
            title = title,
            description = description
        )

        // Update the photo in the database
        photoViewModel.update(updatedPhoto)

        Toast.makeText(this, "Photo updated successfully!", Toast.LENGTH_SHORT).show()
        finish() // Close the activity
    }

    //Function to delete image and database record
    private fun deletePhoto(photo: Photo) {
        // Delete file from device storage
        val filePath = photo.filePath
        if (filePath != null) {
            val fileUri = Uri.parse(filePath)
            try {
                contentResolver.delete(fileUri, null, null) // Deletes the file from MediaStore
                Toast.makeText(this, "Photo file deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error deleting file: ${e.message}", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Delete the photo record from the database
        photoViewModel.delete(photo)
        Toast.makeText(this, "Photo deleted successfully!", Toast.LENGTH_SHORT).show()

        finish() // Close the activity
    }
}