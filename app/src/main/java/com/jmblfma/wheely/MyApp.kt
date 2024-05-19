package com.jmblfma.wheely

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.jmblfma.wheely.data.AppDatabase
import com.jmblfma.wheely.data.RoomDatabaseBuilder
import org.osmdroid.config.Configuration

class MyApp : Application() {
    companion object {
        private lateinit var instance: MyApp
        lateinit var roomDB: AppDatabase
        fun applicationContext(): Context {
            return instance.applicationContext
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        // prevents user to set the app to NightMode as it is not supported
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // sets the user agent to comply with OpenStreetMaps usage policy
        Configuration.getInstance().userAgentValue = applicationContext.packageName
        roomDB = RoomDatabaseBuilder.sharedInstance
    }
}
