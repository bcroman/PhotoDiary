package com.example.photodiary.viewmodel

/*
Author: Ben Collins 21006366
Date: 20/12/2024
Version: 1.0
Project: PhotoDairy
 */

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.photodiary.R
import com.example.photodiary.data.Photo
import com.bumptech.glide.Glide

/*
Adapter for the RecyclerView used in the gallery.
Binds photo data (title, description, and image path) to the UI
 */
class PhotoAdapter(private val onItemClick: (Photo) -> Unit) : ListAdapter<Photo, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val titleView: TextView = itemView.findViewById(R.id.titleView)
        val descView: TextView = itemView.findViewById(R.id.descView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = getItem(position)
        holder.titleView.text = photo.title
        holder.descView.text = photo.description

        // Load image using Glide or similar library
        Glide.with(holder.imageView.context)
            .load(Uri.parse(photo.filePath))
            .into(holder.imageView)

        // Set up click listener for each photo item
        holder.itemView.setOnClickListener {
            onItemClick(photo)
        }
    }
}

class PhotoDiffCallback : DiffUtil.ItemCallback<Photo>() {
    override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem.photoID == newItem.photoID
    }

    override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
        return oldItem == newItem
    }
}
