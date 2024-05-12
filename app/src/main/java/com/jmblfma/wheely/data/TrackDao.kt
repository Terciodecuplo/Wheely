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
    // TODO try/catch on repo... ?
    // less required propagation of state till it gets to viewModel/UI
    @Query("SELECT COUNT(DISTINCT trackId) FROM Tracks")
    suspend fun countDistinctTracks(): Int

    @Query("SELECT trackId FROM tracks")
    suspend fun getAllTrackIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track): Long

    @Query("SELECT * FROM tracks WHERE trackId = :trackId")
    suspend fun getTrackById(trackId: Int): Track?

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracks(): List<Track>
    @Query("SELECT * FROM tracks WHERE drivenByUserId = :userId")
    suspend fun getAllTracksForUser(userId: Int): List<Track>
    @Query("SELECT * FROM tracks WHERE vehicleUsedId = :vehicleId")
    suspend fun getAllTracksForVehicle(vehicleId: Int): List<Track>

    @Query("SELECT * FROM tracks ORDER BY trackId DESC LIMIT 1")
    suspend fun getLastTrack(): Track?

    @Insert
    suspend fun insertTrackPoint(trackPoint: TrackPoint)

    @Query("DELETE FROM tracks WHERE trackId = :trackId")
    suspend fun deleteTrackById(trackId: Int): Int

    @Transaction
    suspend fun insertTrackWithPoints(track: Track): Boolean {
        return try {
            // TODO review ID logic
            val trackId = insertTrack(track)
            Log.d("TESTING", "TrackDao/ insertTrackWithPoints/ trackId: $trackId")
            track.trackData!!.forEach {
                insertTrackPoint(it.copy(trackId = trackId.toInt()))
            }
            true
        } catch (e: Exception) {
            Log.d("TESTING", "TrackDao/ insertTrackWithPoints/ failed", e)
            false
        }
    }

    @Transaction
    suspend fun getTrackWithPoints(trackId: Int): Track? {
        val candidateTrack = getTrackById(trackId)
        val trackPoints = getTrackPointsForTrack(trackId)
        candidateTrack?.let {
            if (trackPoints.isNotEmpty()) {
                candidateTrack.trackData = trackPoints
                return candidateTrack
            }
        }
        return null
    }

    @Transaction
    suspend fun getLastTrackWithPoints(): Track? {
        val lastTrackId = getLastTrack()?.trackId
        return lastTrackId?.let { getTrackWithPoints(it) }
    }

    @Query("SELECT * FROM trackpoints WHERE trackId = :trackId")
    suspend fun getTrackPointsForTrack(trackId: Int): List<TrackPoint>
}