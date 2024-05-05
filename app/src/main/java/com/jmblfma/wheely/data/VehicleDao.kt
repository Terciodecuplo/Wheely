package com.jmblfma.wheely.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jmblfma.wheely.model.Vehicle

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE ownerId = :userId")
    suspend fun getAllVehicles(userId: Int): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE vehicleId = :vehicleId")
    suspend fun getSingleVehicle(vehicleId: Int): Vehicle
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun postVehicle(vehicle: Vehicle)

    @Query("DELETE FROM vehicles WHERE vehicleId = :vehicleId")
    suspend fun deleteVehicle(vehicleId: Int): Int?
}