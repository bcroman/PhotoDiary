package com.example.photodiary

/*
Author: Ben Collins 21006366
Date: 20/12/2024
Version: 1.0
Project: PhotoDairy
 */

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photodiary.data.Photo
import com.example.photodiary.databinding.ActivityGalleryBinding
import com.example.photodiary.viewmodel.PhotoAdapter
import com.example.photodiary.viewmodel.PhotoViewModel

/*
Gallery activity class
Handles image loading
 */
class GalleryActivity : AppCompatActivity() {

    //Set Variables
    private lateinit var viewBinding: ActivityGalleryBinding //Set ViewBinding Variable
    private val photoViewModel: PhotoViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 columns
        photoAdapter = PhotoAdapter { photo ->
            onPhotoClick(photo)
        }
        recyclerView.adapter = photoAdapter

        // Observe photos from ViewModel
        photoViewModel.allPhotos.observe(this, Observer { photos ->
            photos?.let {
                photoAdapter.submitList(it) // Update adapter with new data
            }
        })
    }

    private fun onPhotoClick(photo: Photo) {
        val intent = Intent(this, PhotoDetailActivity::class.java).apply {
            putExtra("photoID", photo.photoID)
            putExtra("filePath", photo.filePath)
            putExtra("title", photo.title)
            putExtra("description", photo.description)
        }
        startActivity(intent)
    }
}