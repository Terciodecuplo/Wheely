package com.jmblfma.wheely.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object LanguageSelector {
    private const val PREFERENCES_FILE = "language_settings"
    private const val LANGUAGE_KEY = "current_language"

    fun saveLanguage(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(LANGUAGE_KEY, languageCode)
            apply()
        }
    }

    fun loadLanguage(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
        // Default language is English
        return sharedPreferences.getString(LANGUAGE_KEY, "en") ?: "en"
    }

    fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getCurrentLanguage(): String = Locale.getDefault().language
}
