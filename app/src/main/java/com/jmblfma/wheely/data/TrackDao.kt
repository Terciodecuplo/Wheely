package com.jmblfma.wheely.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint

@Dao
interface TrackDao {
    @Query("SELECT COUNT(DISTINCT trackId) FROM Tracks")
    suspend fun countDistinctTracks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track): Long

    @Query("SELECT * FROM tracks WHERE trackId = :trackId")
    suspend fun getTrackById(trackId: Int): Track?

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracks(): List<Track>

    @Query("SELECT * FROM tracks ORDER BY trackId DESC LIMIT 1")
    suspend fun getLastTrack(): Track?

    @Insert
    suspend fun insertTrackPoint(trackPoint: TrackPoint)

    @Transaction
    suspend fun insertTrackWithPoints(track: Track) {
        Log.d("TESTING","TrackDao/ IN insertTrackWithPoints")
        val trackId = insertTrack(track)
        track.trackData.forEach {
            insertTrackPoint(it.copy(trackId = trackId.toInt()))
        }
        Log.d("TESTING","TrackDao/ DONE: insertTrackWithPoints")
    }

    // TODO fix for when getTrackById is NULL etc use ?
    @Transaction
    suspend fun getTrackWithPoints(trackId: Int): Track? {
        Log.d("TESTING","TrackDao/ IN: getTrackWithPoints")
        val trackTemplate = getTrackById(trackId)
        val trackPoints = getTrackPointsForTrack(trackId)
        Log.d("TESTING","TrackDao/ DONE: getTrackWithPoints")
        return if (trackTemplate != null) {
            Track.build(trackTemplate.trackId, trackTemplate.name, trackData = trackPoints)
        } else {
            null
        }
    }

    @Transaction
    suspend fun getLastTrackWithPoints(): Track? {
        val lastTrackId = getLastTrack()?.trackId
        return lastTrackId?.let { getTrackWithPoints(it) }
    }

    @Query("SELECT * FROM trackpoints WHERE trackId = :trackId")
    suspend fun getTrackPointsForTrack(trackId: Int): List<TrackPoint>
}