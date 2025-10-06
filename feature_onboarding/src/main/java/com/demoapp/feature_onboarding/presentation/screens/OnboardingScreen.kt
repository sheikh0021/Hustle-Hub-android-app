package com.demoapp.feature_onboarding.presentation.screens

import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.demoapp.core.ui.LanguageManager

enum class OnboardingStep {
    SPLASH,
    WELCOME,
    LANGUAGE,
    TERMS
}

@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    NavHost(
        navController = navController,
        startDestination = OnboardingStep.SPLASH.name
    ) {
        composable(OnboardingStep.SPLASH.name) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(OnboardingStep.WELCOME.name)
                }
            )
        }
        
        composable(OnboardingStep.WELCOME.name) {
            WelcomeScreen(
                onGetStartedClick = {
                    navController.navigate(OnboardingStep.LANGUAGE.name)
                }
            )
        }
        
        composable(OnboardingStep.LANGUAGE.name) {
            LanguageScreen(
                onLanguageSelected = { language ->
                    // Map language name to language code and save it
                    val languageCode = when (language) {
                        "English" -> "en"
                        "Kiswahili" -> "sw"
                        else -> "en"
                    }
                    LanguageManager.saveLanguage(context, languageCode)
                    navController.navigate(OnboardingStep.TERMS.name)
                }
            )
        }
        
        composable(OnboardingStep.TERMS.name) {
            TermsScreen(
                onAcceptTerms = {
                    // Trigger app restart to apply language changes
                    onGetStartedClick()
                },
                onDeclineTerms = {
                    // Handle decline - could show dialog or go back
                    navController.popBackStack()
                }
            )
        }
    }
}
