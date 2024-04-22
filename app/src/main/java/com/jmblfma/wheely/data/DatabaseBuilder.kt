package com.jmblfma.wheely.data

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jmblfma.wheely.MyApp

object RoomDatabaseBuilder {
    private val instance: AppDatabase by lazy {
        //Define the migration from version 1 to version 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE UNIQUE INDEX index_users_email ON users(email)")
            }
        }
        // Initialize and build the Room database
        Room.databaseBuilder(
            MyApp.applicationContext(),
            AppDatabase::class.java,
            "wheely_rooms_db"
        )
            .addMigrations(MIGRATION_1_2) // Apply the migration
            .build()

    }
    val database: AppDatabase
        get() = instance

}