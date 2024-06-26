package com.jmblfma.wheely.repository

import com.jmblfma.wheely.data.RoomDatabaseBuilder
import com.jmblfma.wheely.data.VehicleDao
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle

class VehicleDataRepository {
    private val roomsDB = RoomDatabaseBuilder.sharedInstance
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
    suspend fun insertVehicleWithNewUser(user: User, vehicle: Vehicle, onResult: (Boolean) -> Unit) = vehicleDao.insertVehicleWithNewUser(user, vehicle)
    suspend fun fetchVehicles(userId: Int): List<Vehicle> {
        return try {
            vehicleDao.getAllVehicles(userId)
        } catch (e: Exception) {
            throw Exception("No vehicles fetched for user ID $userId:: ${e.message}")
        }
    }
    suspend fun fetchSingleVehicle(vehicleId: Int): Vehicle = vehicleDao.getSingleVehicle(vehicleId)
    suspend fun deleteVehicle(vehicleId: Int, userId: Int): Int? = vehicleDao.deleteVehicle(vehicleId, userId)
}
