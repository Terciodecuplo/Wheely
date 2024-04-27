package com.jmblfma.wheely.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jmblfma.wheely.model.Vehicle

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE ownerId = 1")
    suspend fun getAllVehicles(): List<Vehicle>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun postVehicle(vehicle: Vehicle)
}