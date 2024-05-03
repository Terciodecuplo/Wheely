package com.jmblfma.wheely.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.repository.TrackDataRepository
import kotlinx.coroutines.launch

class TrackDataViewModel : ViewModel() {
    private val repository = TrackDataRepository.sharedInstance

    companion object {
        val sharedInstance: TrackDataViewModel by lazy { TrackDataViewModel() }
    }

    val trackPoints: LiveData<List<TrackPoint>> = repository.getCurrentTrack()
    val elapsedTime: LiveData<Long> = repository.elapsedTime

    private val _trackLoader = MutableLiveData<Track>()
    val trackLoader: LiveData<Track> = _trackLoader

    private val _totalNumberOfTracks = MutableLiveData<Int>()
    val totalNumberOfTracks: LiveData<Int> = _totalNumberOfTracks

    fun saveCurrentTrack() {
        viewModelScope.launch {
            repository.saveCurrentTrack()
            fetchTrackCount()
        }
    }

    fun pauseTrackRecording() {

    }

    fun resumeTrackRecording() {

    }

    fun fetchLastTrack() {
        viewModelScope.launch {
            _trackLoader.postValue(repository.fetchLastTrack())
        }
    }

    fun fetchTrackByID(trackId: Int) {
        viewModelScope.launch {
            _trackLoader.postValue(repository.fetchTrackByID(trackId))
        }
    }

    fun fetchTrackCount() {
        viewModelScope.launch {
            _totalNumberOfTracks.postValue(repository.fetchTrackCount())
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}