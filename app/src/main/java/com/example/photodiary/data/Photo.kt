package com.example.photodiary.data

/*
Author: Ben Collins 21006366
Date: 20/12/2024
Version: 1.0
Project: PhotoDairy
 */

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*
Defines the structure of the Photo table
 */

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val photoID: Int = 0,
    @ColumnInfo(name = "file_path") val filePath: String?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "description") val description: String?
)