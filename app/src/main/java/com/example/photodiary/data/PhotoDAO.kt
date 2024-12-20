package com.example.photodiary.data

/*
Author: Ben Collins 21006366
Date: 20/12/2024
Version: 1.0
Project: PhotoDairy
 */

import androidx.lifecycle.LiveData
import androidx.room.*

//Data Access Object for interacting with the database

@Dao
interface PhotoDAO {

    @Insert
    suspend fun insert(photo: Photo): Long

    @Query("SELECT * FROM photos")
    fun getAllPhotos(): LiveData<List<Photo>>

    @Update
    suspend fun updatePhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("DELETE FROM photos")
    suspend fun clearAllPhotos()
}
