package com.example.photodiary

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photodiary.databinding.ActivityGalleryBinding
import com.example.photodiary.viewmodel.PhotoAdapter
import com.example.photodiary.viewmodel.PhotoViewModel

class GalleryActivity : AppCompatActivity() {

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
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 columns for grid
        photoAdapter = PhotoAdapter()
        recyclerView.adapter = photoAdapter

        // Observe photos from ViewModel
        photoViewModel.allPhotos.observe(this, Observer { photos ->
            photos?.let {
                photoAdapter.submitList(it) // Update adapter with new data
            }
        })
    }
}