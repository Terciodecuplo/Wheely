package com.jmblfma.wheely.utils

import android.content.Context
import android.content.SharedPreferences

class UserLoginState(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean
        get() = preferences.getBoolean("isFirstLaunch", true)
        set(value) = preferences.edit().putBoolean("isFirstLaunch", value).apply()

    var isLoggedIn: Boolean
        get() = preferences.getBoolean("isLoggedIn", false)
        set(value) = preferences.edit().putBoolean("isLoggedIn", value).apply()
}