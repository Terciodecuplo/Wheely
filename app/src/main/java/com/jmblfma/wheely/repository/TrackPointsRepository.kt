package com.jmblfma.wheely.repository

import androidx.lifecycle.LiveData
import com.jmblfma.wheely.model.TrackPoint

interface TrackPointsRepository {
    fun addTrackPoint(trackPoint: TrackPoint)
    fun getCurrentTrack(): LiveData<List<TrackPoint>>
}