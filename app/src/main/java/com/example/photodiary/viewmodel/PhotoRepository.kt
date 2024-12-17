package com.example.photodiary.viewmodel

import androidx.lifecycle.LiveData
import com.example.photodiary.data.Photo
import com.example.photodiary.data.PhotoDAO

class PhotoRepository(private val photoDao: PhotoDAO) {

    val allPhotos: LiveData<List<Photo>> = photoDao.getAllPhotos()

    suspend fun insert(photo: Photo) {
        photoDao.insertAll(photo)
    }

    suspend fun update(photo: Photo) {
        photoDao.updatePhoto(photo)
    }

    suspend fun delete(photo: Photo) {
        photoDao.deletePhoto(photo)
    }

    suspend fun deleteAll() {
        photoDao.deleteAll()
    }
}
