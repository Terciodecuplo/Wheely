package com.jmblfma.wheely.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.jmblfma.wheely.model.Vehicle

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE ownerId = 1")
    fun getAllVehicles(): List<Vehicle>
    @Insert
    fun insertNewVehicle(vehicle: Vehicle)
}