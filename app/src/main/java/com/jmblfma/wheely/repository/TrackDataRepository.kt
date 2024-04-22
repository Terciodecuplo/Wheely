package com.jmblfma.wheely.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.TrackDao
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint

class TrackDataRepository() {
    private val roomsDB = RoomDatabaseBuilder.getInstance()
    private val trackDao: TrackDao = roomsDB.trackDao()

    private val trackPoints = mutableListOf<TrackPoint>()
    private val trackPointsLiveData = MutableLiveData<List<TrackPoint>>()

    companion object {
        private val instance: TrackDataRepository by lazy { TrackDataRepository() }
        fun getInstance(): TrackDataRepository = instance
    }

    fun addTrackPoint(trackPoint: TrackPoint) {
        Log.d("LocationTest","...trackPoint added!")
        trackPoints.add(trackPoint)
        trackPointsLiveData.postValue(trackPoints)
    }

    fun getTrackPointBuffer(): List<TrackPoint> {
        return trackPoints.toList()
    }

    fun getCurrentTrack(): LiveData<List<TrackPoint>> = trackPointsLiveData

    suspend fun saveCurrentTrack() {
        val trackPointBatch = trackPoints
        val newTrack = Track(trackPointBatch)
        trackPoints.clear()
        trackPointsLiveData.postValue(trackPoints)
        trackDao.insert(newTrack)
    }
    suspend fun fetchLastTrack(): Track? = trackDao.getLastTrack()
    suspend fun fetchAllTracks(): List<Track> = trackDao.getAllTracks()
}