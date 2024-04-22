package com.jmblfma.wheely.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jmblfma.wheely.model.Track

@Database(entities = [Track::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}