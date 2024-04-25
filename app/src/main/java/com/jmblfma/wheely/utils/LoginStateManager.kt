package com.jmblfma.wheely.utils

import android.content.Context
import com.jmblfma.wheely.MyApp

object LoginStateManager {
    private val prefs =
        MyApp.applicationContext().getSharedPreferences("UserSessionPrefs", Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean("isFirstLaunch", true)
        set(value) = prefs.edit().putBoolean("isFirstLaunch", value).apply()
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }


}