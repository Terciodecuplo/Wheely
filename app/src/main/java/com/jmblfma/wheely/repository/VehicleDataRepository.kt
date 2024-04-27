package com.jmblfma.wheely.repository

import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.VehicleDao
import com.jmblfma.wheely.model.Vehicle

class VehicleDataRepository {
    private val roomsDB = RoomDatabaseBuilder.database
    private val vehicleDao: VehicleDao = roomsDB.vehicleDao()

    companion object {
        private val instance: VehicleDataRepository by lazy { VehicleDataRepository() }
        val sharedInstance: VehicleDataRepository = instance
    }

    suspend fun postVehicle(vehicle: Vehicle, onResult: (Boolean) -> Unit) {
        try {
            vehicleDao.postVehicle(vehicle)
            onResult(true)
        } catch (e: Exception) {
            onResult(false)
            throw Exception("Database error posting vehicle: ${e.message}")
        }
    }
}
