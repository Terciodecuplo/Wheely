package com.jmblfma.wheely.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.User
import com.jmblfma.wheely.model.Vehicle

@Database(entities = [Track::class, User::class, Vehicle::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun trackDao(): TrackDao
    abstract fun vehicleDao(): VehicleDao
}

val MIGRATION_1_2: Migration = object:Migration(1,2){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE users ADD COLUMN profileImage TEXT NOT NULL DEFAULT ''")
    }
}