package com.demoapp.core

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object LanguageManager {
    private const val PREF_LANGUAGE = "pref_language"
    private const val ENGLISH = "en"
    private const val SWAHILI = "sw"

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context
        }
    }

    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString(PREF_LANGUAGE, ENGLISH) ?: ENGLISH
    }

    fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LANGUAGE, language).apply()
    }

    fun isEnglish(context: Context): Boolean = getCurrentLanguage(context) == ENGLISH
    fun isSwahili(context: Context): Boolean = getCurrentLanguage(context) == SWAHILI

    fun getLanguageDisplayName(context: Context): String {
        return when (getCurrentLanguage(context)) {
            ENGLISH -> "English"
            SWAHILI -> "Kiswahili"
            else -> "English"
        }
    }
}
