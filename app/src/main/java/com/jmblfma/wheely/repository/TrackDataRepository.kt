package com.jmblfma.wheely.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jmblfma.wheely.data.Difficulty
import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.TrackDao
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.utils.UserSessionManager

class TrackDataRepository() {
    // TODO db temporarily set to legacy
    private val roomsDB = RoomDatabaseBuilder.sharedInstance
    private val trackDao: TrackDao = roomsDB.trackDao()

    companion object {
        val sharedInstance: TrackDataRepository by lazy { TrackDataRepository() }
    }

    // TRACK DATA SERVICE LAYER
    private val trackPoints = mutableListOf<TrackPoint>()
    private val _trackPointsLiveData = MutableLiveData<List<TrackPoint>>()
    val trackPointsLiveData: LiveData<List<TrackPoint>> = _trackPointsLiveData
    fun addTrackPoint(trackPoint: TrackPoint) {
        trackPoints.add(trackPoint)
        _trackPointsLiveData.postValue(trackPoints)
    }

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> = _elapsedTime
    fun updateElapsedTime(time: Long) {
        _elapsedTime.postValue(time)
    }

    fun clearBuffer() {
        trackPoints.clear()
        _elapsedTime.postValue(0)
        _trackPointsLiveData.postValue(trackPoints)
    }

    // REPO DB METHODS
    suspend fun saveCurrentTrack(
        name: String,
        description: String,
        selectedVehicleId: Int?,
        selectedDifficulty: Difficulty
    ): Boolean {
        val trackPointBatch = trackPoints
        val currentUser = UserSessionManager.getCurrentUser()
        val currentUserId = currentUser?.userId ?: 0

        // new Track gets build and initialized
        val newTrack = Track(
            name = name,
            drivenByUserId = currentUserId,
            vehicleUsedId = selectedVehicleId,
            description = description,
            difficultyValue = selectedDifficulty
        )
        newTrack.computeTrackData(trackPointBatch)

        val success = trackDao.insertTrackWithPoints(newTrack)

        return if (success) {
            clearBuffer()
            true
        } else {
            false
        }
    }

    suspend fun fetchLastTrack(): Track? = trackDao.getLastTrackWithPoints()
    suspend fun fetchTrackByID(trackId: Int): Track? = trackDao.getTrackWithPoints(trackId)

    suspend fun fetchTrackListByUser(userId: Int): List<Track> = trackDao.getAllTracksForUser(userId)
    suspend fun deleteTrackById(trackId: Int): Boolean {
        return trackDao.deleteTrackById(trackId) > 0
    }
    suspend fun fetchTrackCount(): Int = trackDao.countDistinctTracks()
    suspend fun fetchTrackIDList(): List<Int> = trackDao.getAllTrackIds()
    suspend fun fetchAllTracks(): List<Track> = trackDao.getAllTracks()
}