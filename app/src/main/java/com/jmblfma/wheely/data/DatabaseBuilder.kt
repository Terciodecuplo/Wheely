package com.jmblfma.wheely.data

import androidx.room.Room
import com.jmblfma.wheely.MyApp

object RoomDatabaseBuilder {
    private val instance: AppDatabase by lazy {
        // Initialize and build the Room database
        Room.databaseBuilder(
            MyApp.applicationContext(),
            AppDatabase::class.java,
            "wheely_rooms_db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }
    val database: AppDatabase
        get() = instance

}