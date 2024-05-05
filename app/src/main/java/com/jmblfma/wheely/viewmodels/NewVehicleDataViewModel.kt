package com.jmblfma.wheely.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.VehicleDao
import com.jmblfma.wheely.model.Vehicle
import com.jmblfma.wheely.repository.VehicleDataRepository
import kotlinx.coroutines.launch

class NewVehicleDataViewModel : ViewModel() {
    private val vehicleDao: VehicleDao = RoomDatabaseBuilder.sharedInstance.vehicleDao()
    private val repository = VehicleDataRepository.sharedInstance
    private val _vehicleData = MutableLiveData<Vehicle>()
    val vehicleData: LiveData<Vehicle>
        get() =_vehicleData

    private val _vehiclePostStatus = MutableLiveData<String?>()
    var vehiclePostStatus: LiveData<String?> = _vehiclePostStatus

    fun addVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.postVehicle(vehicle) { isSuccess ->
                if (isSuccess) {
                    _vehiclePostStatus.postValue("Vehicle added successfully")
                } else {
                    _vehiclePostStatus.postValue("Vehicle cannot be added.")
                }
            }
        }
    }

    fun fetchSingleVehicle(vehicleId: Int){
        viewModelScope.launch {
            _vehicleData.postValue(repository.fetchSingleVehicle(vehicleId))
        }
    }
    fun deleteVehicle(vehicleId : Int){
        viewModelScope.launch {
            repository.deleteVehicle(vehicleId)
        }
    }

}