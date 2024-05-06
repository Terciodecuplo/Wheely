package com.jmblfma.wheely.data

import androidx.room.Room
import com.jmblfma.wheely.MyApp

object RoomDatabaseBuilder {
     val sharedInstance: AppDatabase by lazy {
        // Initialize and build the Room database
        Room.databaseBuilder(
            MyApp.applicationContext(),
            AppDatabase::class.java,
            "wheely_new_20240504"
        ).build()
    }

    // ALT. DB FOR TESTING PURPOSES - UNUSED (it doesn't get instantiated)
    /*val legacyDB: AppDatabaseLegacy by lazy {
        Room.databaseBuilder(
            MyApp.applicationContext(),
            AppDatabaseLegacy::class.java,
            "wheely_rooms_db"
        )
        .addMigrations(MIGRATION_1_2)
        .build()
    }*/

    // LEGACY DB 20240504 NAME:
    // wheely_rooms_db2
}
