package com.demoapp.feature_jobs.data

import com.demoapp.feature_jobs.domain.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class TaskRepository {
    private val _tasks = MutableStateFlow<List<TaskData>>(emptyList())
    val tasks: Flow<List<TaskData>> = _tasks.asStateFlow()

    private val _workers = MutableStateFlow<List<WorkerData>>(emptyList())
    val workers: Flow<List<WorkerData>> = _workers.asStateFlow()

    private val _jobRequesters = MutableStateFlow<List<JobRequesterData>>(emptyList())
    val jobRequesters: Flow<List<JobRequesterData>> = _jobRequesters.asStateFlow()

    private val _taskProgress = MutableStateFlow<List<TaskProgress>>(emptyList())
    val taskProgress: Flow<List<TaskProgress>> = _taskProgress.asStateFlow()

    private val _messages = MutableStateFlow<List<TaskMessage>>(emptyList())
    val messages: Flow<List<TaskMessage>> = _messages.asStateFlow()

    private val _payments = MutableStateFlow<List<PaymentTransaction>>(emptyList())
    val payments: Flow<List<PaymentTransaction>> = _payments.asStateFlow()

    // Task Operations
    suspend fun createTask(task: TaskData): TaskData {
        val newTask = task.copy(
            id = UUID.randomUUID().toString(),
            createdAt = Date(),
            updatedAt = Date()
        )
        _tasks.value = _tasks.value + newTask
        return newTask
    }

    suspend fun updateTask(task: TaskData): TaskData {
        val updatedTask = task.copy(updatedAt = Date())
        _tasks.value = _tasks.value.map { if (it.id == task.id) updatedTask else it }
        return updatedTask
    }

    suspend fun getTaskById(taskId: String): TaskData? {
        return _tasks.value.find { it.id == taskId }
    }

    suspend fun getTasksByRequester(requesterId: String): List<TaskData> {
        return _tasks.value.filter { it.jobRequesterId == requesterId }
    }

    suspend fun getAvailableTasks(): List<TaskData> {
        return _tasks.value.filter { it.status == TaskStatus.AVAILABLE }
    }

    suspend fun getTasksByWorker(workerId: String): List<TaskData> {
        return _tasks.value.filter { it.assignedWorkerId == workerId }
    }

    suspend fun assignTaskToWorker(taskId: String, workerId: String): TaskData? {
        val task = getTaskById(taskId)
        return if (task != null && task.status == TaskStatus.AVAILABLE) {
            val updatedTask = task.copy(
                assignedWorkerId = workerId,
                status = TaskStatus.ASSIGNED,
                updatedAt = Date()
            )
            updateTask(updatedTask)
        } else null
    }

    // Worker Operations
    suspend fun createWorker(worker: WorkerData): WorkerData {
        val newWorker = worker.copy(id = UUID.randomUUID().toString())
        _workers.value = _workers.value + newWorker
        return newWorker
    }

    suspend fun updateWorker(worker: WorkerData): WorkerData {
        _workers.value = _workers.value.map { if (it.id == worker.id) worker else it }
        return worker
    }

    suspend fun getWorkerById(workerId: String): WorkerData? {
        return _workers.value.find { it.id == workerId }
    }

    suspend fun getAvailableWorkers(): List<WorkerData> {
        return _workers.value.filter { 
            it.isActive && it.availability == WorkerAvailability.AVAILABLE 
        }
    }

    // Job Requester Operations
    suspend fun createJobRequester(requester: JobRequesterData): JobRequesterData {
        val newRequester = requester.copy(id = UUID.randomUUID().toString())
        _jobRequesters.value = _jobRequesters.value + newRequester
        return newRequester
    }

    suspend fun getJobRequesterById(requesterId: String): JobRequesterData? {
        return _jobRequesters.value.find { it.id == requesterId }
    }

    // Task Progress Operations
    suspend fun updateTaskProgress(progress: TaskProgress): TaskProgress {
        _taskProgress.value = _taskProgress.value.filter { it.taskId != progress.taskId } + progress
        
        // Update task status based on progress
        val task = getTaskById(progress.taskId)
        if (task != null) {
            val newStatus = when (progress.currentStep) {
                WorkerStep.TASK_ACCEPTED -> TaskStatus.ASSIGNED
                WorkerStep.AT_STORE -> TaskStatus.IN_PROGRESS
                WorkerStep.GOODS_CONFIRMED -> TaskStatus.AWAITING_APPROVAL
                WorkerStep.DELIVERY_COMPLETED -> TaskStatus.COMPLETED
                else -> task.status
            }
            
            if (newStatus != task.status) {
                updateTask(task.copy(status = newStatus))
            }
        }
        
        return progress
    }

    suspend fun getTaskProgress(taskId: String): TaskProgress? {
        return _taskProgress.value.find { it.taskId == taskId }
    }

    // Message Operations
    suspend fun sendMessage(message: TaskMessage): TaskMessage {
        val newMessage = message.copy(
            id = UUID.randomUUID().toString(),
            timestamp = Date()
        )
        _messages.value = _messages.value + newMessage
        return newMessage
    }

    suspend fun getMessagesForTask(taskId: String): List<TaskMessage> {
        return _messages.value.filter { it.taskId == taskId }.sortedBy { it.timestamp }
    }

    suspend fun markMessageAsRead(messageId: String) {
        _messages.value = _messages.value.map { 
            if (it.id == messageId) it.copy(isRead = true) else it 
        }
    }

    // Payment Operations
    suspend fun createPayment(payment: PaymentTransaction): PaymentTransaction {
        val newPayment = payment.copy(
            id = UUID.randomUUID().toString(),
            createdAt = Date(),
            updatedAt = Date()
        )
        _payments.value = _payments.value + newPayment
        return newPayment
    }

    suspend fun updatePayment(payment: PaymentTransaction): PaymentTransaction {
        val updatedPayment = payment.copy(updatedAt = Date())
        _payments.value = _payments.value.map { if (it.id == payment.id) updatedPayment else it }
        
        // Update task payment status
        val task = getTaskById(payment.taskId)
        if (task != null && task.paymentStatus != updatedPayment.status) {
            updateTask(task.copy(paymentStatus = updatedPayment.status))
        }
        
        return updatedPayment
    }

    suspend fun getPaymentByTaskId(taskId: String): PaymentTransaction? {
        return _payments.value.find { it.taskId == taskId }
    }

    // AI Task Refinement
    suspend fun refineTaskDescription(originalDescription: String, category: TaskCategory): String {
        return when (category) {
            TaskCategory.SHOPPING -> refineShoppingTask(originalDescription)
            TaskCategory.DELIVERY -> refineDeliveryTask(originalDescription)
            TaskCategory.SURVEY -> refineSurveyTask(originalDescription)
            TaskCategory.PHOTOGRAPHY -> refinePhotographyTask(originalDescription)
            else -> originalDescription
        }
    }

    private fun refineShoppingTask(description: String): String {
        // AI logic to ensure shopping tasks are specific
        val refined = StringBuilder(description)
        
        if (!description.contains("brand", ignoreCase = true)) {
            refined.append("\n\nPlease specify the exact brand you prefer.")
        }
        
        if (!description.contains("price", ignoreCase = true) && !description.contains("budget", ignoreCase = true)) {
            refined.append("\nPlease specify your budget or expected price range.")
        }
        
        if (!description.contains("store", ignoreCase = true) && !description.contains("shop", ignoreCase = true)) {
            refined.append("\nPlease specify your preferred store or shopping location.")
        }
        
        return refined.toString()
    }

    private fun refineDeliveryTask(description: String): String {
        val refined = StringBuilder(description)
        
        if (!description.contains("pickup", ignoreCase = true)) {
            refined.append("\n\nPlease specify the pickup location.")
        }
        
        if (!description.contains("delivery", ignoreCase = true)) {
            refined.append("\nPlease specify the delivery location.")
        }
        
        return refined.toString()
    }

    private fun refineSurveyTask(description: String): String {
        val refined = StringBuilder(description)
        
        if (!description.contains("questions", ignoreCase = true)) {
            refined.append("\n\nPlease specify the questions to be asked.")
        }
        
        if (!description.contains("target", ignoreCase = true)) {
            refined.append("\nPlease specify the target audience.")
        }
        
        return refined.toString()
    }

    private fun refinePhotographyTask(description: String): String {
        val refined = StringBuilder(description)
        
        if (!description.contains("location", ignoreCase = true)) {
            refined.append("\n\nPlease specify the photography location.")
        }
        
        if (!description.contains("style", ignoreCase = true)) {
            refined.append("\nPlease specify the photography style or requirements.")
        }
        
        return refined.toString()
    }

    // Initialize sample data
    fun initializeSampleData() {
        // Sample workers
        val sampleWorkers = listOf(
            WorkerData(
                name = "John Kamau",
                email = "john.kamau@email.com",
                phone = "+254712345678",
                location = LocationDetails(
                    address = "Westlands, Nairobi",
                    latitude = -1.2544,
                    longitude = 36.8065,
                    city = "Nairobi"
                ),
                rating = 4.8f,
                totalTasks = 45,
                completedTasks = 43,
                skills = listOf("Shopping", "Delivery", "Photography"),
                availability = WorkerAvailability.AVAILABLE,
                earnings = 12500.0,
                verificationStatus = VerificationStatus.VERIFIED
            ),
            WorkerData(
                name = "Mary Wanjiku",
                email = "mary.wanjiku@email.com",
                phone = "+254723456789",
                location = LocationDetails(
                    address = "Karen, Nairobi",
                    latitude = -1.3192,
                    longitude = 36.6812,
                    city = "Nairobi"
                ),
                rating = 4.6f,
                totalTasks = 32,
                completedTasks = 30,
                skills = listOf("Shopping", "Survey"),
                availability = WorkerAvailability.AVAILABLE,
                earnings = 8900.0,
                verificationStatus = VerificationStatus.VERIFIED
            )
        )

        // Sample job requesters
        val sampleRequesters = listOf(
            JobRequesterData(
                name = "David Otieno",
                email = "david.otieno@email.com",
                phone = "+254734567890",
                location = LocationDetails(
                    address = "Kilimani, Nairobi",
                    latitude = -1.2921,
                    longitude = 36.7789,
                    city = "Nairobi"
                ),
                totalTasksPosted = 8,
                completedTasks = 6,
                rating = 4.7f
            )
        )

        _workers.value = sampleWorkers
        _jobRequesters.value = sampleRequesters
    }

    companion object {
        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository().also { INSTANCE = it }
            }
        }
    }
}
