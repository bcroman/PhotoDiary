package com.example.photodiary

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.photodiary.R
import com.example.photodiary.databinding.ActivityPhotoDetailBinding

class PhotoDetailActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityPhotoDetailBinding //Set ViewBinding Variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityPhotoDetailBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get data from intent
        val filePath = intent.getStringExtra("filePath")
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")

        // Bind views
        val imageView = findViewById<ImageView>(R.id.detailImageView)
        val titleView = findViewById<TextView>(R.id.detailTitleView)
        val descriptionView = findViewById<TextView>(R.id.detailDescView)

        // Display details
        titleView.text = title
        descriptionView.text = description

        // Load image using Glide
        Glide.with(this)
            .load(Uri.parse(filePath))
            //.placeholder(R.drawable.placeholder)
            //.error(R.drawable.error_placeholder)
            .into(imageView)
    }
}