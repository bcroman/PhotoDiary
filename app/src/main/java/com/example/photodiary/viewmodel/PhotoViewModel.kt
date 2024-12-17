package com.example.photodiary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.photodiary.data.Photo
import com.example.photodiary.data.PhotoDatabase
import kotlinx.coroutines.launch

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository
    val allPhotos: LiveData<List<Photo>> // Expose allPhotos to the UI

    init {
        val photoDao = PhotoDatabase.getDatabase(application).photoDao()
        repository = PhotoRepository(photoDao)
        allPhotos = repository.allPhotos
    }

    // Insert photo
    fun insert(photo: Photo) = viewModelScope.launch {
        repository.insert(photo)
    }

    // Update photo
    fun update(photo: Photo) = viewModelScope.launch {
        repository.update(photo)
    }

    // Delete photo
    fun delete(photo: Photo) = viewModelScope.launch {
        repository.delete(photo)
    }

    // Delete all photos
    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
