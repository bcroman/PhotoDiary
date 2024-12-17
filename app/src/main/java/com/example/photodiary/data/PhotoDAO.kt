package com.example.photodiary.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoDAO {

    @Insert
    suspend fun insertAll(vararg photos: Photo)

    @Query("SELECT * FROM photos")
    fun getAllPhotos(): LiveData<List<Photo>>

    @Update
    suspend fun updatePhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("DELETE FROM photos")
    suspend fun deleteAll()
}
