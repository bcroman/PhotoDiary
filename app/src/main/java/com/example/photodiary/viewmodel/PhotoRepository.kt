package com.example.photodiary.viewmodel

/*
Author: Ben Collins 21006366
Date: 20/12/2024
Version: 1.0
Project: PhotoDairy
 */

import androidx.lifecycle.LiveData
import com.example.photodiary.data.Photo
import com.example.photodiary.data.PhotoDAO

/*
Acts as a bridge between the ViewModel and DAO.
Contains logic for database operations
 */
class PhotoRepository(private val photoDao: PhotoDAO) {

    val allPhotos: LiveData<List<Photo>> = photoDao.getAllPhotos()

    suspend fun insert(photo: Photo): Long {
        return photoDao.insert(photo)
    }

    suspend fun update(photo: Photo) {
        photoDao.updatePhoto(photo)
    }

    suspend fun deletePhoto(photo: Photo) {
        photoDao.deletePhoto(photo)
    }

    suspend fun deleteAll() {
        photoDao.deleteAll()
    }
}
