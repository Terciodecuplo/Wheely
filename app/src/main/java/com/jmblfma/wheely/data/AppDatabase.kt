package com.jmblfma.wheely.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle

@Database(
    entities = [Track::class,
                TrackPoint::class,
                User::class,
                Vehicle::class
    ],
    version = 1
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun trackDao(): TrackDao
    abstract fun vehicleDao(): VehicleDao
}

/*
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE trackpoints ADD COLUMN bearing REAL")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS users (\n" +
                "    userId INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    nickname TEXT NOT NULL,\n" +
                "    firstName TEXT NOT NULL,\n" +
                "    lastName TEXT NOT NULL,\n" +
                "    email TEXT NOT NULL UNIQUE,\n" +
                "    dateOfBirth TEXT NOT NULL,\n" +
                "    profileImage TEXT,\n" +
                "    profileBanner TEXT\n" +
                ");"
        )
        db.execSQL("CREATE TABLE IF NOT EXISTS vehicles (\n" +
                "    vehicleId INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    ownerId INTEGER NOT NULL,\n" +
                "    name TEXT NOT NULL,\n" +
                "    brand TEXT NOT NULL,\n" +
                "    model TEXT NOT NULL,\n" +
                "    year TEXT NOT NULL,\n" +
                "    horsepower INTEGER NOT NULL,\n" +
                "    dateAdded TEXT NOT NULL DEFAULT (date('now')),\n" +
                "    image TEXT NOT NULL,\n" +
                "    FOREIGN KEY (ownerId) REFERENCES users(userId) ON DELETE CASCADE\n" +
                ");"
        )
    }
}
*/

