package com.jmblfma.wheely.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.TrackDao
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint

class TrackDataRepository() {
    private val roomsDB = RoomDatabaseBuilder.sharedInstance
    private val trackDao: TrackDao = roomsDB.trackDao()

    private val trackPoints = mutableListOf<TrackPoint>()
    private val trackPointsLiveData = MutableLiveData<List<TrackPoint>>()

    companion object {
        val sharedInstance: TrackDataRepository by lazy { TrackDataRepository() }
    }

    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> = _elapsedTime

    fun updateElapsedTime(time: Long) {
        _elapsedTime.postValue(time)
    }

    fun addTrackPoint(trackPoint: TrackPoint) {
        Log.d("TESTING","TrackDataRepository/ ...trackPoint added!")
        trackPoints.add(trackPoint)
        trackPointsLiveData.postValue(trackPoints)
    }

    fun getTrackPointBuffer(): List<TrackPoint> {
        return trackPoints.toList()
    }

    fun getCurrentTrack(): LiveData<List<TrackPoint>> = trackPointsLiveData

    suspend fun saveCurrentTrack() {
        Log.d("TESTING","TrackDataRepository/ savingTrack")
        val trackPointBatch = trackPoints
        val newTrack = Track.build(trackData = trackPoints)
        // trackPoints.clear()
        trackPointsLiveData.postValue(trackPoints)
        trackDao.insertTrackWithPoints(newTrack)
        Log.d("TESTING","TrackDataRepository/ saved")
    }
    suspend fun fetchLastTrack(): Track? = trackDao.getLastTrackWithPoints()

    suspend fun fetchTrackByID(trackId: Int): Track? = trackDao.getTrackWithPoints(trackId)

    suspend fun fetchTrackCount(): Int = trackDao.countDistinctTracks()
}