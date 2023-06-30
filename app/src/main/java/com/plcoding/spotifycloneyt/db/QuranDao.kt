package com.plcoding.spotifycloneyt.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.plcoding.spotifycloneyt.data.entities.QuranModel

@Dao
interface QuranDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(quranModel: QuranModel): Long

    @Query("SELECT * FROM quranTable")
    fun getAllQuran(): LiveData<List<QuranModel>>

    @Delete
    suspend fun deleteQuran(quranModel: QuranModel)

}