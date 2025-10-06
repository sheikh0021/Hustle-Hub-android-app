package com.demoapp.core.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import java.util.*

abstract class LanguageAwareActivity : ComponentActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        val savedLanguage = LanguageManager.getCurrentLanguage(newBase)
        val locale = Locale(savedLanguage)
        val config = Configuration(newBase.resources.configuration)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        val context = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            newBase.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            newBase
        }
        
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun changeLanguage(language: String) {
        LanguageManager.saveLanguage(this, language)
        recreate()
    }

    fun getStringResource(resourceId: Int): String {
        return getString(resourceId)
    }
}
