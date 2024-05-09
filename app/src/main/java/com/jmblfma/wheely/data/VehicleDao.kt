package com.jmblfma.wheely.data

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE ownerId = :userId")
    suspend fun getAllVehicles(userId: Int): List<Vehicle>

    @Query("SELECT * FROM vehicles WHERE vehicleId = :vehicleId")
    suspend fun getSingleVehicle(vehicleId: Int): Vehicle
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun postUser(user: User): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun postVehicle(vehicle: Vehicle): Long

    @Query("DELETE FROM vehicles WHERE " +
            "vehicleId = :vehicleId " +
            "AND (SELECT COUNT(*) FROM vehicles WHERE ownerId = :userId) > 1")
    suspend fun deleteVehicle(vehicleId: Int, userId: Int): Int?
    @Transaction
    suspend fun insertVehicleWithNewUser(user: User, vehicle: Vehicle) {
        Log.d("VEHICLE","Posting vehicle with new user")
        val userId = postUser(user)
        postVehicle(vehicle.copy(ownerId = userId.toInt()))
    }
}