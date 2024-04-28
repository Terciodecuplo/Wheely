package com.jmblfma.wheely.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle

@Database(entities = [Track::class, User::class, Vehicle::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun trackDao(): TrackDao
    abstract fun vehicleDao(): VehicleDao


}