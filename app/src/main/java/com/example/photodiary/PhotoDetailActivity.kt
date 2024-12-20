package com.example.photodiary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.photodiary.data.Photo
import com.example.photodiary.databinding.ActivityPhotoDetailBinding
import com.example.photodiary.viewmodel.PhotoViewModel

class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityPhotoDetailBinding
    private lateinit var photoViewModel: PhotoViewModel
    private var photoID: Int = 0
    private var filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize ViewModel
        photoViewModel = ViewModelProvider(this).get(PhotoViewModel::class.java)

        // Retrieve data from intent
        filePath = intent.getStringExtra("filePath")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        photoID = intent.getIntExtra("photoID", 0)

        // Display photo details
        viewBinding.detailTitleView.text = title ?: "No Title"
        viewBinding.detailDescView.text = description ?: "No Description"

        // Load the photo using Glide
        Glide.with(this)
            .load(Uri.parse(filePath))
            //.placeholder(R.drawable.placeholder)
            //.error(R.drawable.error_placeholder)
            .into(viewBinding.detailImageView)

        // Set up delete button
        viewBinding.fabDelete.setOnClickListener {
            deletePhoto()
        }

        //FAB Go Function to open Gallery Page
        viewBinding.fabGalleryPage.setOnClickListener{
            val myIntent = Intent(this, GalleryActivity::class.java) //Create Intent Variable to open Gallery activity
            startActivity(myIntent) //Do MyIntent to open page
        }
    }

    //Function to delete image and database record
    private fun deletePhoto() {
        // Delete the photo file from device storage
        try {
            filePath?.let { path ->
                val uri = Uri.parse(path)
                contentResolver.delete(uri, null, null)
                Toast.makeText(this, "Photo file deleted", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error deleting photo file: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        // Delete the photo record from the database
        val photo = Photo(photoID = photoID, filePath = filePath, title = "", description = "")
        photoViewModel.delete(photo)

        Toast.makeText(this, "Photo deleted successfully!", Toast.LENGTH_SHORT).show()
        finish() // Close the activity
    }
}