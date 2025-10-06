package com.demoapp.feature_jobs.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

data class FirebaseChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val jobId: String,
    val text: String,
    val senderId: String,
    val senderName: String,
    val senderType: SenderType, // WORKER or CLIENT
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    constructor() : this("", "", "", "", "", SenderType.WORKER, 0, false)
}

data class FirebaseChatRoom(
    val jobId: String,
    val jobTitle: String,
    val clientId: String,
    val clientName: String,
    val workerId: String? = null,
    val workerName: String? = null,
    val lastMessage: FirebaseChatMessage? = null,
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0
) {
    constructor() : this("", "", "", "", null, null, null, 0, 0)
}

class FirebaseChatRepository {
    companion object {
        @Volatile
        private var INSTANCE: FirebaseChatRepository? = null

        fun getInstance(): FirebaseChatRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseChatRepository().also { INSTANCE = it }
            }
        }
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val messagesCollection = firestore.collection("chat_messages")
    private val chatRoomsCollection = firestore.collection("chat_rooms")

    /**
     * Send a message to a specific job chat
     */
    suspend fun sendMessage(message: FirebaseChatMessage): Result<Unit> {
        return try {
            android.util.Log.d("FirebaseChat", "🔥 Attempting to send message to Firebase: ${message.text}")
            val messageData = mapOf(
                "id" to message.id,
                "jobId" to message.jobId,
                "text" to message.text,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "senderType" to message.senderType.name,
                "timestamp" to message.timestamp,
                "isRead" to message.isRead
            )

            messagesCollection
                .document(message.id)
                .set(messageData)
                .await()

            // Update chat room with last message
            updateChatRoomLastMessage(message)
            
            android.util.Log.d("FirebaseChat", "✅ Message sent successfully to Firebase: ${message.text}")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseChat", "❌ Error sending message to Firebase: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Listen to messages for a specific job in real-time
     */
    fun getMessagesForJob(jobId: String): Flow<List<FirebaseChatMessage>> = callbackFlow {
        val listener = messagesCollection
            .whereEqualTo("jobId", jobId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { document ->
                        try {
                            FirebaseChatMessage(
                                id = document.getString("id") ?: "",
                                jobId = document.getString("jobId") ?: "",
                                text = document.getString("text") ?: "",
                                senderId = document.getString("senderId") ?: "",
                                senderName = document.getString("senderName") ?: "",
                                senderType = SenderType.valueOf(document.getString("senderType") ?: "WORKER"),
                                timestamp = document.getLong("timestamp") ?: 0L,
                                isRead = document.getBoolean("isRead") ?: false
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(messages)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Get chat rooms for a specific user
     */
    fun getChatRoomsForUser(userId: String, userType: SenderType): Flow<List<FirebaseChatRoom>> = callbackFlow {
        val fieldName = if (userType == SenderType.WORKER) "workerId" else "clientId"
        
        val listener = chatRoomsCollection
            .whereEqualTo(fieldName, userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatRooms = snapshot.documents.mapNotNull { document ->
                        try {
                            FirebaseChatRoom(
                                jobId = document.getString("jobId") ?: "",
                                jobTitle = document.getString("jobTitle") ?: "",
                                clientId = document.getString("clientId") ?: "",
                                clientName = document.getString("clientName") ?: "",
                                workerId = document.getString("workerId"),
                                workerName = document.getString("workerName"),
                                lastMessageTime = document.getLong("lastMessageTime") ?: 0L,
                                unreadCount = document.getLong("unreadCount")?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(chatRooms)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Create a new chat room for a job
     */
    suspend fun createChatRoom(
        jobId: String,
        jobTitle: String,
        clientId: String,
        clientName: String
    ): Result<Unit> {
        return try {
            val chatRoomData = mapOf(
                "jobId" to jobId,
                "jobTitle" to jobTitle,
                "clientId" to clientId,
                "clientName" to clientName,
                "workerId" to null,
                "workerName" to null,
                "lastMessageTime" to 0L,
                "unreadCount" to 0
            )

            chatRoomsCollection
                .document(jobId)
                .set(chatRoomData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Assign a worker to a chat room
     */
    suspend fun assignWorkerToChatRoom(
        jobId: String,
        workerId: String,
        workerName: String
    ): Result<Unit> {
        return try {
            chatRoomsCollection
                .document(jobId)
                .update(
                    mapOf(
                        "workerId" to workerId,
                        "workerName" to workerName
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update chat room with last message
     */
    private suspend fun updateChatRoomLastMessage(message: FirebaseChatMessage) {
        try {
            val updateData = mutableMapOf<String, Any>(
                "lastMessageTime" to message.timestamp
            )

            // Increment unread count for the other user
            val otherUserField = if (message.senderType == SenderType.WORKER) "clientId" else "workerId"
            updateData["unreadCount"] = com.google.firebase.firestore.FieldValue.increment(1)

            chatRoomsCollection
                .document(message.jobId)
                .update(updateData)
                .await()
        } catch (e: Exception) {
            // Log error but don't fail the message send
            e.printStackTrace()
        }
    }

    /**
     * Mark messages as read for a specific job
     */
    suspend fun markMessagesAsRead(jobId: String, userId: String): Result<Unit> {
        return try {
            // Reset unread count for the user
            chatRoomsCollection
                .document(jobId)
                .update("unreadCount", 0)
                .await()

            // Mark all messages as read
            val messages = messagesCollection
                .whereEqualTo("jobId", jobId)
                .whereNotEqualTo("senderId", userId)
                .get()
                .await()

            val batch = firestore.batch()
            messages.documents.forEach { document ->
                batch.update(document.reference, "isRead", true)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Initialize sample data for testing
     */
    suspend fun initializeSampleData() {
        try {
            // Create sample chat rooms
            val sampleChatRooms = listOf(
                FirebaseChatRoom(
                    jobId = "job_1",
                    jobTitle = "Package Delivery to Karen",
                    clientId = "client_sarah",
                    clientName = "Sarah Johnson",
                    workerId = "worker_john_kamau",
                    workerName = "John Kamau",
                    lastMessageTime = System.currentTimeMillis() - 300000, // 5 minutes ago
                    unreadCount = 0
                ),
                FirebaseChatRoom(
                    jobId = "job_2",
                    jobTitle = "Grocery Shopping at Nakumatt",
                    clientId = "client_mike",
                    clientName = "Mike Ochieng",
                    workerId = "worker_mary_wanjiku",
                    workerName = "Mary Wanjiku",
                    lastMessageTime = System.currentTimeMillis() - 600000, // 10 minutes ago
                    unreadCount = 1
                )
            )

            // Create sample messages
            val sampleMessages = listOf(
                FirebaseChatMessage(
                    jobId = "job_1",
                    text = "Hi! I'm interested in this delivery job. Can you provide more details about the pickup location?",
                    senderId = "worker_john_kamau",
                    senderName = "John Kamau",
                    senderType = SenderType.WORKER,
                    timestamp = System.currentTimeMillis() - 600000
                ),
                FirebaseChatMessage(
                    jobId = "job_1",
                    text = "Hello John! The pickup is at Westgate Mall, Shop 45. The package is ready for collection.",
                    senderId = "client_sarah",
                    senderName = "Sarah Johnson",
                    senderType = SenderType.CLIENT,
                    timestamp = System.currentTimeMillis() - 500000
                ),
                FirebaseChatMessage(
                    jobId = "job_1",
                    text = "Perfect! What time would be convenient for pickup?",
                    senderId = "worker_john_kamau",
                    senderName = "John Kamau",
                    senderType = SenderType.WORKER,
                    timestamp = System.currentTimeMillis() - 300000
                ),
                FirebaseChatMessage(
                    jobId = "job_2",
                    text = "I can help with your shopping task. Do you have a specific list of items?",
                    senderId = "worker_mary_wanjiku",
                    senderName = "Mary Wanjiku",
                    senderType = SenderType.WORKER,
                    timestamp = System.currentTimeMillis() - 900000
                ),
                FirebaseChatMessage(
                    jobId = "job_2",
                    text = "Yes, I need groceries from Nakumatt. I'll send you the list shortly.",
                    senderId = "client_mike",
                    senderName = "Mike Ochieng",
                    senderType = SenderType.CLIENT,
                    timestamp = System.currentTimeMillis() - 600000
                )
            )

            // Upload chat rooms
            sampleChatRooms.forEach { room ->
                chatRoomsCollection.document(room.jobId).set(room)
            }

            // Upload messages
            sampleMessages.forEach { message ->
                messagesCollection.document(message.id).set(message)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
