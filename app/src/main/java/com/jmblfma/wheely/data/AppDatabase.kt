package com.jmblfma.wheely.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jmblfma.wheely.model.Track
import com.jmblfma.wheely.model.TrackPoint

@Database(
    entities = [Track::class,
               TrackPoint::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE trackpoints ADD COLUMN bearing REAL")
    }
}