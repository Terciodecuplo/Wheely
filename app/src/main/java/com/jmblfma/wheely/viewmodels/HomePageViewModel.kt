package com.jmblfma.wheely.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.repository.TrackDataRepository
import com.jmblfma.wheely.repository.UserDataRepository
import kotlinx.coroutines.launch

class HomePageViewModel : ViewModel() {
    private val trackRepository = TrackDataRepository.sharedInstance
    private val userRepository = UserDataRepository.sharedInstance

    companion object {
        val sharedInstance: HomePageViewModel by lazy { HomePageViewModel() }
    }

    private val _trackListLoader = MutableLiveData<List<Track>>()
    private val _usersLoader = MutableLiveData<List<User>>()

    val combinedData = MediatorLiveData<Pair<List<Track>?, List<User>?>>().apply {
        addSource(_trackListLoader) { tracks ->
            value = Pair(tracks, _usersLoader.value)
        }
        addSource(_usersLoader) { users ->
            value = Pair(_trackListLoader.value, users)
        }
    }
    fun fetchTrackList() {
        viewModelScope.launch {
            val trackList = trackRepository.fetchAllTracks()
            _trackListLoader.postValue(trackList)
        }
    }

    fun fetchUserList() {
        viewModelScope.launch {
            val userList = userRepository.fetchUsers()
            _usersLoader.postValue(userList)
        }
    }
}
