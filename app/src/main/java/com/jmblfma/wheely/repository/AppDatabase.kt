package com.jmblfma.wheely.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle

@Database(entities = [User::class],[Vehicle::class],version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun userDao(): UserDao
    abstract fun vehicleDao(): VehicleDao
}