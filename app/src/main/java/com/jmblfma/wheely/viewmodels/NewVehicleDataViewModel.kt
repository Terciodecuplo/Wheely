package com.jmblfma.wheely.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jmblfma.wheely.model.Vehicle
import java.time.LocalDate

class NewVehicleDataViewModel : ViewModel() {
    private val _vehicles = MutableLiveData(generateTestData())
    var vehicles: LiveData<MutableList<Vehicle>> = _vehicles

    private fun generateTestData(): MutableList<Vehicle> {
        return mutableListOf(
            Vehicle(vehicleId = 1, ownerId = 1, name = "Bike One", brand = "Yamaha", model = "YZF-R1", year = "2021", horsepower = 190, dateAdded = LocalDate.now().toString()),
            Vehicle(vehicleId = 2, ownerId = 2, name = "Bike Two", brand = "Honda", model = "CBR600RR", year = "2020", horsepower = 120, dateAdded = LocalDate.now().toString())
        )
    }

    fun addVehicle(vehicle: Vehicle) {
        val currentList = _vehicles.value?.toMutableList()?: mutableListOf()
        currentList.add(vehicle)
        _vehicles.value = currentList
        Log.d("AddVehicleViewModel", "The _list contains:\n ${_vehicles.value?.get(0)}\n${_vehicles.value?.get(1)}\n${_vehicles.value?.get(2)}")
        Log.d("AddVehicleViewModel", "The list contains:\n ${vehicles.value?.get(0)}\n${vehicles.value?.get(1)}\n${vehicles.value?.get(2)}")

    }

}