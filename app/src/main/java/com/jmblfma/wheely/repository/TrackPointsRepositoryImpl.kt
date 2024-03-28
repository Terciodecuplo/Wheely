package com.jmblfma.wheely.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jmblfma.wheely.model.TrackPoint

class TrackPointsRepositoryImpl : TrackPointsRepository {
    private val trackPoints = mutableListOf<TrackPoint>()
    private val trackPointsLiveData = MutableLiveData<List<TrackPoint>>()

    companion object {
        val instance: TrackPointsRepositoryImpl by lazy { TrackPointsRepositoryImpl() }
    }

    override fun addTrackPoint(trackPoint: TrackPoint) {
        Log.d("LocationTest","...trackPoint added!")
        trackPoints.add(trackPoint)
        trackPointsLiveData.postValue(trackPoints)

    }

    override fun getCurrentTrack(): LiveData<List<TrackPoint>> = trackPointsLiveData

}