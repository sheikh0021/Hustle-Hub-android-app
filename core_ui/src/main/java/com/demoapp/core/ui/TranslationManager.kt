package com.demoapp.core.ui

import android.content.Context

object TranslationManager {
    
    // English translations
    private val englishTranslations = mapOf(
        // App
        "app_name" to "HustleHub",
        
        // Dashboard
        "job_request_dashboard" to "Job Request Dashboard",
        "worker_dashboard" to "HustleHub Dashboard",
        "job_request" to "Job request",
        "worker" to "Worker",
        
        // General
        "balance" to "Balance",
        "search_jobs" to "Search jobs...",
        "available_jobs" to "Available Jobs",
        "my_jobs" to "My Jobs",
        "my_posted_jobs" to "My Posted Jobs",
        
        // Language Selection
        "select_language" to "Select Language",
        "choose_your_language" to "Choose Your Language",
        "choose_preferred_language" to "Choose your preferred language for the best experience",
        "btn_continue" to "Continue",
        
        // Job Assistant
        "job_assistant" to "Job Assistant",
        "chat_with_assistant" to "Chat with Assistant",
        "assistant_description" to "Need help with a job? Ask our assistant for guidance, tips, or clarification on job requirements.",
        
        // Task Creation
        "create_task" to "Create Task",
        "task_details" to "Task Details",
        "delivery" to "Delivery",
        "shopping" to "Shopping",
        
        // Payment
        "payment" to "Payment",
        "upload_payment_proof" to "Upload Payment Proof",
        "payment_confirmed" to "Payment Confirmed",
        
        // Worker Flow
        "accept_task" to "Accept Task",
        "execute_task" to "Execute Task",
        "task_completed" to "Task Completed",
        "deliver_items" to "Deliver Items",
        "buy_items" to "Buy Items",
        
        // Status
        "pending" to "Pending",
        "in_progress" to "In Progress",
        "completed" to "Completed",
        "cancelled" to "Cancelled",
        
        // Common Actions
        "submit" to "Submit",
        "cancel" to "Cancel",
        "confirm" to "Confirm",
        "back" to "Back",
        "next" to "Next",
        "save" to "Save",
        "edit" to "Edit",
        "delete" to "Delete",
        
        // Navigation
        "home" to "Home",
        "profile" to "Profile",
        "settings" to "Settings",
        "logout" to "Logout"
    )
    
    // Swahili translations
    private val swahiliTranslations = mapOf(
        // App
        "app_name" to "HustleHub",
        
        // Dashboard
        "job_request_dashboard" to "Dashibodi ya Ombi la Kazi",
        "worker_dashboard" to "Dashibodi ya Mfanyakazi",
        "job_request" to "Ombi la Kazi",
        "worker" to "Mfanyakazi",
        
        // General
        "balance" to "Salio",
        "search_jobs" to "Tafuta kazi...",
        "available_jobs" to "Kazi Zilizopo",
        "my_jobs" to "Kazi Zangu",
        "my_posted_jobs" to "Kazi Nilizoweka",
        
        // Language Selection
        "select_language" to "Chagua Lugha",
        "choose_your_language" to "Chagua Lugha Yako",
        "choose_preferred_language" to "Chagua lugha unayopendelea kwa uzoefu bora",
        "btn_continue" to "Endelea",
        
        // Job Assistant
        "job_assistant" to "Msaidizi wa Kazi",
        "chat_with_assistant" to "Zungumza na Msaidizi",
        "assistant_description" to "Unahitaji msaada wa kazi? Uliza msaidizi wetu mwongozo, vidokezo, au ufafanuzi juu ya mahitaji ya kazi.",
        
        // Task Creation
        "create_task" to "Unda Kazi",
        "task_details" to "Maelezo ya Kazi",
        "delivery" to "Uwasilishaji",
        "shopping" to "Ununuzi",
        
        // Payment
        "payment" to "Malipo",
        "upload_payment_proof" to "Pakia Uthibitisho wa Malipo",
        "payment_confirmed" to "Malipo Yamehakikiwa",
        
        // Worker Flow
        "accept_task" to "Kubali Kazi",
        "execute_task" to "Tekeleza Kazi",
        "task_completed" to "Kazi Imekamilika",
        "deliver_items" to "Wasilisha Vitu",
        "buy_items" to "Nunua Vitu",
        
        // Status
        "pending" to "Inasubiri",
        "in_progress" to "Inaendelea",
        "completed" to "Imekamilika",
        "cancelled" to "Imeghairiwa",
        
        // Common Actions
        "submit" to "Wasilisha",
        "cancel" to "Ghairi",
        "confirm" to "Thibitisha",
        "back" to "Rudi",
        "next" to "Ifuatayo",
        "save" to "Hifadhi",
        "edit" to "Hariri",
        "delete" to "Futa",
        
        // Navigation
        "home" to "Nyumbani",
        "profile" to "Profaili",
        "settings" to "Mipangilio",
        "logout" to "Ondoka"
    )
    
    fun getString(context: Context, key: String): String {
        val currentLanguage = LanguageManager.getCurrentLanguage(context)
        val translations = if (currentLanguage == "sw") swahiliTranslations else englishTranslations
        return translations[key] ?: key
    }
    
    fun getString(context: Context, key: String, default: String): String {
        val currentLanguage = LanguageManager.getCurrentLanguage(context)
        val translations = if (currentLanguage == "sw") swahiliTranslations else englishTranslations
        return translations[key] ?: default
    }
}
