package com.jmblfma.wheely.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmblfma.wheely.data.Difficulty
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.repository.TrackDataRepository
import com.jmblfma.wheely.repository.VehicleDataRepository
import com.jmblfma.wheely.utils.TrackRecordingState
import com.jmblfma.wheely.utils.UserSessionManager
import kotlinx.coroutines.launch

class TrackRecordingViewModel : ViewModel() {
    private val trackDataRepository = TrackDataRepository.sharedInstance
    private val vehicleRepository = VehicleDataRepository.sharedInstance

    companion object {
        val sharedInstance: TrackRecordingViewModel by lazy { TrackRecordingViewModel() }
    }

    // UI MANAGEMENT
    private val _trackRecordingState = MutableLiveData<TrackRecordingState>()
    val trackRecordingState: LiveData<TrackRecordingState> = _trackRecordingState
    fun setUIState(newState: TrackRecordingState, forceUpdate: Boolean = false) {
        // only updates the state if it has changed
        // prevents unnecessary repeated calls to UI elements
        if (_trackRecordingState.value != newState || forceUpdate) {
            _trackRecordingState.value = newState
        }
    }
    fun getUIState(): TrackRecordingState {
        return _trackRecordingState.value!!
    }

    // TRACK DATA
    val trackPointsLiveData: LiveData<List<TrackPoint>> = trackDataRepository.trackPointsLiveData
    fun areThereTrackPointsInTheBuffer(): Boolean {
        return !trackPointsLiveData.value.isNullOrEmpty()
    }
    val elapsedTime: LiveData<Long> = trackDataRepository.elapsedTime
    fun getTrackPoints(): List<TrackPoint>? {
        // returns the current value of trackPoints LiveData as a List<TrackPoint> or null if it's not yet set
        return trackPointsLiveData.value
    }

    // TRACK SAVING LOGIC
    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess
    fun saveCurrentTrack(
        name: String,
        description: String,
        selectedVehicleId: Int?,
        selectedDifficulty: Difficulty
    ) {
        viewModelScope.launch {
            val success = trackDataRepository.saveCurrentTrack(
                name,
                description,
                selectedVehicleId,
                selectedDifficulty
            )
            _saveSuccess.postValue(success)
        }
    }
    private val _loadedVehicles = MutableLiveData<List<Vehicle>>()
    val loadedVehicles: LiveData<List<Vehicle>> = _loadedVehicles
    fun fetchCurrentVehicleList() {
        viewModelScope.launch {
            val currentUserId = UserSessionManager.getCurrentUser()?.userId
            currentUserId?.let {
                _loadedVehicles.postValue(vehicleRepository.fetchVehicles(it))
            }
        }
    }
    fun discardTrack() {
        trackDataRepository.clearBuffer()
    }

    override fun onCleared() {
        super.onCleared()
    }
}