package com.jmblfma.wheely.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE ownerId = :userId")
    suspend fun getAllVehicles(userId: Int): List<Vehicle>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun postVehicle(vehicle: Vehicle)
}