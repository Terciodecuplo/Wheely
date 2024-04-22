package com.jmblfma.wheely.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jmblfma.wheely.model.Track

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: Track)

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracks(): List<Track>

    @Query("SELECT * FROM tracks ORDER BY trackId DESC LIMIT 1")
    suspend fun getLastTrack(): Track?
}