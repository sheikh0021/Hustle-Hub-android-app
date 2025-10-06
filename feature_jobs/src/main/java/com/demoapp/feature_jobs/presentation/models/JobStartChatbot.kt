package com.demoapp.feature_jobs.presentation.models

import java.util.Date

data class JobStartChatbot(
    val id: String = "",
    val jobId: String,
    val workerId: String,
    val questions: List<ChatbotQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val isCompleted: Boolean = false,
    val startedAt: Date = Date(),
    val completedAt: Date? = null
)

data class ChatbotQuestion(
    val id: String,
    val question: String,
    val questionType: QuestionType,
    val options: List<String> = emptyList(), // For multiple choice questions
    val isRequired: Boolean = true,
    val validationRule: String? = null // For custom validation
)

data class ChatbotAnswer(
    val questionId: String,
    val answer: String,
    val answeredAt: Date = Date()
)

enum class QuestionType {
    TEXT_INPUT,        // Free text input
    MULTIPLE_CHOICE,   // Select from options
    YES_NO,           // Yes/No question
    NUMBER_INPUT,     // Numeric input
    LOCATION_INPUT    // Location/GPS input
}

// Predefined questions for different job types
object JobStartQuestions {
    
    fun getQuestionsForJobType(jobType: String): List<ChatbotQuestion> {
        return when (jobType.lowercase()) {
            "grocery shopping" -> getGroceryShoppingQuestions()
            "package delivery" -> getPackageDeliveryQuestions()
            "house cleaning" -> getHouseCleaningQuestions()
            "customer survey" -> getCustomerSurveyQuestions()
            "data entry" -> getDataEntryQuestions()
            else -> getGenericQuestions()
        }
    }
    
    private fun getGroceryShoppingQuestions(): List<ChatbotQuestion> {
        return listOf(
            ChatbotQuestion(
                id = "grocery_1",
                question = "Do you have access to reliable transportation to reach the store?",
                questionType = QuestionType.YES_NO
            ),
            ChatbotQuestion(
                id = "grocery_2", 
                question = "What is your estimated time to complete the shopping?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                options = listOf("30 minutes", "1 hour", "1.5 hours", "2+ hours")
            ),
            ChatbotQuestion(
                id = "grocery_3",
                question = "Do you have experience with grocery shopping and can handle perishable items carefully?",
                questionType = QuestionType.YES_NO
            ),
            ChatbotQuestion(
                id = "grocery_4",
                question = "Any additional notes or special requirements you'd like to mention?",
                questionType = QuestionType.TEXT_INPUT,
                isRequired = false
            )
        )
    }
    
    private fun getPackageDeliveryQuestions(): List<ChatbotQuestion> {
        return listOf(
            ChatbotQuestion(
                id = "delivery_1",
                question = "What type of vehicle will you use for delivery?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Motorcycle", "Car", "Bicycle", "Walking", "Public Transport")
            ),
            ChatbotQuestion(
                id = "delivery_2",
                question = "Can you confirm you understand the delivery address and have GPS access?",
                questionType = QuestionType.YES_NO
            ),
            ChatbotQuestion(
                id = "delivery_3",
                question = "What is your estimated delivery time?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Within 30 minutes", "Within 1 hour", "Within 2 hours", "More than 2 hours")
            ),
            ChatbotQuestion(
                id = "delivery_4",
                question = "Do you have a valid driver's license and insurance?",
                questionType = QuestionType.YES_NO
            )
        )
    }
    
    private fun getHouseCleaningQuestions(): List<ChatbotQuestion> {
        return listOf(
            ChatbotQuestion(
                id = "cleaning_1",
                question = "Do you have your own cleaning supplies and equipment?",
                questionType = QuestionType.YES_NO
            ),
            ChatbotQuestion(
                id = "cleaning_2",
                question = "What is your experience level with house cleaning?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Beginner", "Intermediate", "Experienced", "Professional")
            ),
            ChatbotQuestion(
                id = "cleaning_3",
                question = "Are you comfortable with pets in the house?",
                questionType = QuestionType.YES_NO
            ),
            ChatbotQuestion(
                id = "cleaning_4",
                question = "What is your estimated time to complete the cleaning?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                options = listOf("2 hours", "3 hours", "4 hours", "5+ hours")
            )
        )
    }
    
    private fun getCustomerSurveyQuestions(): List<ChatbotQuestion> {
        return listOf(
            ChatbotQuestion(
                id = "survey_1",
                question = "Do you have experience conducting customer surveys?",
                questionType = QuestionType.YES_NO
            ),
            ChatbotQuestion(
                id = "survey_2",
                question = "What is your preferred method of conducting surveys?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                options = listOf("In-person interviews", "Phone calls", "Online forms", "Mixed approach")
            ),
            ChatbotQuestion(
                id = "survey_3",
                question = "How many survey responses can you collect per day?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                options = listOf("5-10", "10-20", "20-30", "30+")
            )
        )
    }
    
    private fun getDataEntryQuestions(): List<ChatbotQuestion> {
        return listOf(
            ChatbotQuestion(
                id = "data_1",
                question = "Do you have access to a computer and reliable internet?",
                questionType = QuestionType.YES_NO
            ),
            ChatbotQuestion(
                id = "data_2",
                question = "What is your typing speed (words per minute)?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                options = listOf("Under 30 WPM", "30-50 WPM", "50-70 WPM", "70+ WPM")
            ),
            ChatbotQuestion(
                id = "data_3",
                question = "Do you have experience with data entry software?",
                questionType = QuestionType.YES_NO
            )
        )
    }
    
    private fun getGenericQuestions(): List<ChatbotQuestion> {
        return listOf(
            ChatbotQuestion(
                id = "generic_1",
                question = "Are you ready to start this job immediately?",
                questionType = QuestionType.YES_NO
            ),
            ChatbotQuestion(
                id = "generic_2",
                question = "Do you have all the necessary tools/equipment for this task?",
                questionType = QuestionType.YES_NO
            ),
            ChatbotQuestion(
                id = "generic_3",
                question = "What is your estimated completion time?",
                questionType = QuestionType.TEXT_INPUT
            ),
            ChatbotQuestion(
                id = "generic_4",
                question = "Any questions or concerns about this job?",
                questionType = QuestionType.TEXT_INPUT,
                isRequired = false
            )
        )
    }
}
