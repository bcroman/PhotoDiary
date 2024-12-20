package com.example.photodiary.viewmodel

/*
Author: Ben Collins 21006366
Date: 20/12/2024
Version: 1.0
Project: PhotoDairy
 */

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.photodiary.data.Photo
import com.example.photodiary.data.PhotoDatabase
import kotlinx.coroutines.launch

//Exposes database operations to the UI layer
class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository
    val allPhotos: LiveData<List<Photo>> // Expose allPhotos to the UI

    init {
        val photoDao = PhotoDatabase.getDatabase(application).photoDao()
        repository = PhotoRepository(photoDao)
        allPhotos = repository.allPhotos
    }

    // Insert photo
    fun insert(photo: Photo, callback: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.insert(photo)
            callback(id) // Pass the new ID back
        }
    }

    // Update photo
    fun update(photo: Photo) = viewModelScope.launch {
        repository.update(photo)
    }

    // Delete photo
    fun delete(photo: Photo) = viewModelScope.launch {
        repository.deletePhoto(photo)
    }

    // Delete all photos
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
