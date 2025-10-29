package com.demoapp.feature_jobs.data

import com.demoapp.feature_jobs.presentation.models.JobStartChatbot
import com.demoapp.feature_jobs.presentation.models.ChatbotQuestion
import com.demoapp.feature_jobs.presentation.models.ChatbotAnswer
import com.demoapp.feature_jobs.presentation.models.JobStartQuestions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class JobStartChatbotRepository {
    
    private val _chatbots = MutableStateFlow<List<JobStartChatbot>>(emptyList())
    val chatbots: StateFlow<List<JobStartChatbot>> = _chatbots.asStateFlow()
    
    private val _answers = MutableStateFlow<List<ChatbotAnswer>>(emptyList())
    val answers: StateFlow<List<ChatbotAnswer>> = _answers.asStateFlow()
    
    companion object {
        @Volatile
        private var INSTANCE: JobStartChatbotRepository? = null
        
        fun getInstance(): JobStartChatbotRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JobStartChatbotRepository().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Start a new job chatbot session for a worker
     */
    fun startJobChatbot(jobId: String, workerId: String, jobType: String): JobStartChatbot {
        val questions = JobStartQuestions.getQuestionsForJobType(jobType)
        val chatbot = JobStartChatbot(
            id = "chatbot_${jobId}_${workerId}_${System.currentTimeMillis()}",
            jobId = jobId,
            workerId = workerId,
            questions = questions,
            currentQuestionIndex = 0,
            isCompleted = false,
            startedAt = Date()
        )
        
        val currentChatbots = _chatbots.value.toMutableList()
        currentChatbots.add(chatbot)
        _chatbots.value = currentChatbots
        
        println("DEBUG: Started job chatbot for job $jobId, worker $workerId with ${questions.size} questions")
        return chatbot
    }
    
    /**
     * Submit an answer for a specific question
     */
    fun submitAnswer(chatbotId: String, questionId: String, answer: String): Boolean {
        val currentAnswers = _answers.value.toMutableList()
        val existingAnswerIndex = currentAnswers.indexOfFirst { 
            it.questionId == questionId
        }
        
        val newAnswer = ChatbotAnswer(
            questionId = questionId,
            answer = answer,
            answeredAt = Date()
        )
        
        if (existingAnswerIndex != -1) {
            currentAnswers[existingAnswerIndex] = newAnswer
        } else {
            currentAnswers.add(newAnswer)
        }
        
        _answers.value = currentAnswers
        
        // Move to next question
        moveToNextQuestion(chatbotId)
        
        println("DEBUG: Submitted answer for question $questionId: $answer")
        return true
    }
    
    /**
     * Move to the next question in the chatbot
     */
    private fun moveToNextQuestion(chatbotId: String) {
        val currentChatbots = _chatbots.value.toMutableList()
        val chatbotIndex = currentChatbots.indexOfFirst { it.id == chatbotId }
        
        if (chatbotIndex != -1) {
            val chatbot = currentChatbots[chatbotIndex]
            val nextIndex = chatbot.currentQuestionIndex + 1
            
            if (nextIndex >= chatbot.questions.size) {
                // All questions completed
                currentChatbots[chatbotIndex] = chatbot.copy(
                    isCompleted = true,
                    completedAt = Date()
                )
                println("DEBUG: Chatbot $chatbotId completed all questions")
            } else {
                // Move to next question
                currentChatbots[chatbotIndex] = chatbot.copy(
                    currentQuestionIndex = nextIndex
                )
                println("DEBUG: Moved to question ${nextIndex + 1} of ${chatbot.questions.size}")
            }
            
            _chatbots.value = currentChatbots
        }
    }
    
    /**
     * Get current chatbot for a job and worker
     */
    fun getChatbotForJob(jobId: String, workerId: String): JobStartChatbot? {
        return _chatbots.value.find { 
            it.jobId == jobId && it.workerId == workerId && !it.isCompleted 
        }
    }
    
    /**
     * Get completed chatbot for a job and worker
     */
    fun getCompletedChatbotForJob(jobId: String, workerId: String): JobStartChatbot? {
        return _chatbots.value.find { 
            it.jobId == jobId && it.workerId == workerId && it.isCompleted 
        }
    }
    
    /**
     * Get answers for a specific chatbot
     */
    fun getAnswersForChatbot(chatbotId: String): List<ChatbotAnswer> {
        val chatbot = _chatbots.value.find { it.id == chatbotId }
        return if (chatbot != null) {
            _answers.value.filter { answer ->
                chatbot.questions.any { question -> question.id == answer.questionId }
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * Check if worker has completed job start validation
     */
    fun hasWorkerCompletedJobStart(jobId: String, workerId: String): Boolean {
        return getCompletedChatbotForJob(jobId, workerId) != null
    }
    
    /**
     * Reset chatbot for a job (if needed)
     */
    fun resetChatbotForJob(jobId: String, workerId: String) {
        val currentChatbots = _chatbots.value.toMutableList()
        val chatbotIndex = currentChatbots.indexOfFirst { 
            it.jobId == jobId && it.workerId == workerId 
        }
        
        if (chatbotIndex != -1) {
            currentChatbots.removeAt(chatbotIndex)
            _chatbots.value = currentChatbots
            
            // Remove related answers
            val chatbot = currentChatbots[chatbotIndex]
            val currentAnswers = _answers.value.toMutableList()
            val answersToRemove = currentAnswers.filter { answer ->
                chatbot.questions.any { question -> question.id == answer.questionId }
            }
            currentAnswers.removeAll(answersToRemove)
            _answers.value = currentAnswers
            
            println("DEBUG: Reset chatbot for job $jobId, worker $workerId")
        }
    }
}
