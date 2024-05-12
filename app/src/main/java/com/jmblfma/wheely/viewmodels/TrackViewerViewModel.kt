package com.jmblfma.wheely.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.repository.TrackDataRepository
import kotlinx.coroutines.launch
class TrackViewerViewModel : ViewModel() {
    private val repository = TrackDataRepository.sharedInstance

    companion object {
        val sharedInstance: TrackViewerViewModel by lazy { TrackViewerViewModel() }
    }

    private val _trackLoader = MutableLiveData<Track?>()
    val trackLoader: LiveData<Track?> = _trackLoader
    fun fetchLastTrack() {
        viewModelScope.launch {
            val loadedTrack = repository.fetchLastTrack()
            _trackLoader.postValue(loadedTrack)
            // gets last trackId if loaded
            loadedTrack?.let { lastTrackLoadedId = it.trackId }
        }
    }

    private val _trackListLoader = MutableLiveData<List<Track>>()
    val trackListLoader: LiveData<List<Track>> = _trackListLoader
    fun fetchTrackList() {
        viewModelScope.launch {
            val trackList = repository.fetchAllTracks()
            _trackListLoader.postValue(trackList)
        }
    }

    fun fetchCycle(forward: Boolean): Boolean {
        val trackIDs = trackIds.value?.toList() ?: emptyList()
        if (trackIDs.isNotEmpty()) {
            // if there was a fetch on onCreate; sets the initial state of the pointer to the currently loaded tack
            var currentTrackId = lastTrackLoadedId ?: 0
            var trackIdListPointer = trackIDs.indexOf(currentTrackId)

            // wrap-around list implementation
            if (forward) {
                trackIdListPointer = (trackIdListPointer + 1) % trackIDs.size
            } else {
                trackIdListPointer = (trackIdListPointer - 1 + trackIDs.size) % trackIDs.size
            }
            currentTrackId = trackIDs[trackIdListPointer]

            viewModelScope.launch {
                val loadedTrack = fetchTrackByID(currentTrackId)
            }
            // if trackIDs.size == 1 returns false to trigger no tracks to cycle msg
            return trackIDs.size != 1
        } else {
            return false
        }
    }
    fun fetchTrackByID(trackId: Int) {
        viewModelScope.launch {
            val loadedTrack = repository.fetchTrackByID(trackId)
            _trackLoader.postValue(loadedTrack)
            // gets last trackId if loaded
            loadedTrack?.let { lastTrackLoadedId = it.trackId }
        }
    }

    private val _totalNumberOfTracks = MutableLiveData<Int>()
    val totalNumberOfTracks: LiveData<Int> = _totalNumberOfTracks
    fun fetchTrackCount() {
        viewModelScope.launch {
            _totalNumberOfTracks.postValue(repository.fetchTrackCount())
        }
    }
    private val _trackIds = MutableLiveData<List<Int>>()
    val trackIds: LiveData<List<Int>> = _trackIds
    fun fetchTrackIDs() {
        viewModelScope.launch {
            _trackIds.postValue(repository.fetchTrackIDList())
        }
    }

    private var lastTrackLoadedId: Int? = null
    private val _deleteSuccess = MutableLiveData<Boolean?>()
    val deleteSuccess: LiveData<Boolean?> = _deleteSuccess
    fun deleteLoadedTrack() {
        viewModelScope.launch {
            lastTrackLoadedId?.let {
                val isDeleted = repository.deleteTrackById(it)
                _deleteSuccess.postValue(isDeleted)
                if (isDeleted) {
                    // Updates parameters
                    fetchTrackIDs()
                    fetchTrackCount()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}