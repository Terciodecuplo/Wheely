package com.jmblfma.wheely.utils

import android.content.Context
import android.text.Highlights
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jmblfma.wheely.MyApp
import com.jmblfma.wheely.model.User

object LoginStateManager {
    private val prefs =
        MyApp.applicationContext().getSharedPreferences("UserSessionPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean("isFirstLaunch", true)
        set(value) = prefs.edit().putBoolean("isFirstLaunch", value).apply()

    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("isLoggedIn", false)

    fun saveCurrentUser(user: User?) {
        val gson = GsonBuilder().serializeNulls().create()
        val userJson = gson.toJson(user) // Converts null to "null" if user is null
        // Log.d("SAVING USER","User image = $userJson")
        prefs.edit().putString("currentUser", userJson).apply()
    }

    fun getCurrentUser(): User? {
        val userJson = prefs.getString("currentUser", null)
        return userJson?.let { gson.fromJson(it, User::class.java) }
    }

    fun setUserHighlightPreference(highlightsSelected : Int){
        prefs.edit().putInt("highlightPreferred", highlightsSelected).apply()
    }

    fun getUserHighlightPreference() : Int = prefs.getInt("highlightPreferred", 0)
}
