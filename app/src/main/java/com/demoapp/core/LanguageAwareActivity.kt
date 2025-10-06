package com.demoapp.core

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class LanguageAwareActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.setLocale(newBase, LanguageManager.getCurrentLanguage(newBase)))
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
