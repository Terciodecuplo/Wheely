package com.jmblfma.wheely.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.TrackDao
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.repository.TrackDataRepository
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class TrackDataViewModel : ViewModel() {
    private val trackDao: TrackDao = RoomDatabaseBuilder.database.trackDao()
    private val repository = TrackDataRepository.sharedInstance

    val trackPoints: LiveData<List<TrackPoint>> = repository.getCurrentTrack()
    // val latestTrackPoint: LiveData<TrackPoint> = repository.latestTrackPoint


    private val _lastTrack = MutableLiveData<Track>()
    val lastTrack: LiveData<Track> = _lastTrack
    fun saveCurrentTrack() {
        viewModelScope.launch {
            repository.saveCurrentTrack()
        }
    }

    fun fetchLastTrack() {
        viewModelScope.launch {
            _lastTrack.postValue(repository.fetchLastTrack())
        }
    }

    fun convertTrackPointsToGeoPoints(trackPoints: List<TrackPoint>): List<GeoPoint> {
        return trackPoints.map { GeoPoint(it.latitude, it.longitude) }
    }
}