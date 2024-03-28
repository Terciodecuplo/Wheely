package com.jmblfma.wheely.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.repository.TrackPointsRepositoryImpl
import org.osmdroid.util.GeoPoint
class TrackPointsViewModel : ViewModel() {
    private val repository = TrackPointsRepositoryImpl.instance

    val trackPoints: LiveData<List<TrackPoint>> = repository.getCurrentTrack()
    // val latestTrackPoint: LiveData<TrackPoint> = repository.latestTrackPoint

    fun convertTrackPointsToGeoPoints(trackPoints: List<TrackPoint>): List<GeoPoint> {
        return trackPoints.map { GeoPoint(it.latitude, it.longitude) }
    }
}