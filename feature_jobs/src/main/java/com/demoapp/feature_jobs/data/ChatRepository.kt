package com.demoapp.feature_jobs.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val jobId: String,
    val text: String,
    val senderId: String,
    val senderName: String,
    val senderType: SenderType, // WORKER or CLIENT
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

enum class SenderType {
    WORKER, CLIENT
}

data class ChatRoom(
    val jobId: String,
    val jobTitle: String,
    val clientId: String,
    val clientName: String,
    val workerId: String? = null,
    val workerName: String? = null,
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0
)

class ChatRepository {
    companion object {
        @Volatile
        private var INSTANCE: ChatRepository? = null

        fun getInstance(): ChatRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ChatRepository().also { INSTANCE = it }
            }
        }
    }

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()

    // Sample data for demonstration
    fun initializeSampleData() {
        val sampleMessages = mapOf(
            "job_1" to listOf(
                ChatMessage(
                    jobId = "job_1",
                    text = "Hi! I'm interested in this delivery job. Can you provide more details about the pickup location?",
                    senderId = "worker_john_kamau",
                    senderName = "John Kamau",
                    senderType = SenderType.WORKER
                ),
                ChatMessage(
                    jobId = "job_1",
                    text = "Hello John! The pickup is at Westgate Mall, Shop 45. The package is ready for collection.",
                    senderId = "client_sarah",
                    senderName = "Sarah Johnson",
                    senderType = SenderType.CLIENT
                ),
                ChatMessage(
                    jobId = "job_1",
                    text = "Perfect! What time would be convenient for pickup?",
                    senderId = "worker_john_kamau",
                    senderName = "John Kamau",
                    senderType = SenderType.WORKER
                )
            ),
            "job_2" to listOf(
                ChatMessage(
                    jobId = "job_2",
                    text = "I can help with your shopping task. Do you have a specific list of items?",
                    senderId = "worker_mary_wanjiku",
                    senderName = "Mary Wanjiku",
                    senderType = SenderType.WORKER
                ),
                ChatMessage(
                    jobId = "job_2",
                    text = "Yes, I need groceries from Nakumatt. I'll send you the list shortly.",
                    senderId = "client_mike",
                    senderName = "Mike Ochieng",
                    senderType = SenderType.CLIENT
                )
            )
        )

        val sampleChatRooms = listOf(
            ChatRoom(
                jobId = "job_1",
                jobTitle = "Package Delivery to Karen",
                clientId = "client_sarah",
                clientName = "Sarah Johnson",
                workerId = "worker_john_kamau",
                workerName = "John Kamau",
                lastMessage = sampleMessages["job_1"]?.last(),
                unreadCount = 0
            ),
            ChatRoom(
                jobId = "job_2",
                jobTitle = "Grocery Shopping at Nakumatt",
                clientId = "client_mike",
                clientName = "Mike Ochieng",
                workerId = "worker_mary_wanjiku",
                workerName = "Mary Wanjiku",
                lastMessage = sampleMessages["job_2"]?.last(),
                unreadCount = 1
            )
        )

        _messages.value = sampleMessages
        _chatRooms.value = sampleChatRooms
    }

    fun sendMessage(message: ChatMessage) {
        val currentMessages = _messages.value.toMutableMap()
        val jobMessages = currentMessages[message.jobId]?.toMutableList() ?: mutableListOf()
        jobMessages.add(message)
        currentMessages[message.jobId] = jobMessages
        _messages.value = currentMessages

        // Update chat room with last message
        updateChatRoomLastMessage(message)
    }

    fun getMessagesForJob(jobId: String): List<ChatMessage> {
        return _messages.value[jobId] ?: emptyList()
    }

    fun getChatRoomsForUser(userId: String, userType: SenderType): List<ChatRoom> {
        return _chatRooms.value.filter { room ->
            when (userType) {
                SenderType.WORKER -> room.workerId == userId
                SenderType.CLIENT -> room.clientId == userId
            }
        }
    }

    fun createChatRoom(jobId: String, jobTitle: String, clientId: String, clientName: String) {
        val currentRooms = _chatRooms.value.toMutableList()
        val existingRoom = currentRooms.find { it.jobId == jobId }
        
        if (existingRoom == null) {
            val newRoom = ChatRoom(
                jobId = jobId,
                jobTitle = jobTitle,
                clientId = clientId,
                clientName = clientName
            )
            currentRooms.add(newRoom)
            _chatRooms.value = currentRooms
        }
    }

    fun assignWorkerToChatRoom(jobId: String, workerId: String, workerName: String) {
        val currentRooms = _chatRooms.value.toMutableList()
        val roomIndex = currentRooms.indexOfFirst { it.jobId == jobId }
        
        if (roomIndex != -1) {
            currentRooms[roomIndex] = currentRooms[roomIndex].copy(
                workerId = workerId,
                workerName = workerName
            )
            _chatRooms.value = currentRooms
        }
    }

    private fun updateChatRoomLastMessage(message: ChatMessage) {
        val currentRooms = _chatRooms.value.toMutableList()
        val roomIndex = currentRooms.indexOfFirst { it.jobId == message.jobId }
        
        if (roomIndex != -1) {
            currentRooms[roomIndex] = currentRooms[roomIndex].copy(
                lastMessage = message,
                unreadCount = if (message.senderType == SenderType.WORKER) 
                    currentRooms[roomIndex].unreadCount + 1 
                else 
                    currentRooms[roomIndex].unreadCount
            )
            _chatRooms.value = currentRooms
        }
    }

    fun markMessagesAsRead(jobId: String, userId: String) {
        val currentRooms = _chatRooms.value.toMutableList()
        val roomIndex = currentRooms.indexOfFirst { it.jobId == jobId }
        
        if (roomIndex != -1) {
            currentRooms[roomIndex] = currentRooms[roomIndex].copy(unreadCount = 0)
            _chatRooms.value = currentRooms
        }
    }
}
