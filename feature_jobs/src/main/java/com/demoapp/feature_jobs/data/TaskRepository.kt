package com.demoapp.feature_jobs.data

import android.content.Context
import com.demoapp.core_network.NetworkClient
import com.demoapp.core_network.models.CreateTaskRequest
import com.demoapp.core_network.models.CreateTaskResponse
import com.demoapp.core_network.models.MyTasksResponse
import com.demoapp.core_network.models.ShoppingItem
import com.demoapp.feature_auth.data.AuthTokenManager
import com.demoapp.feature_jobs.domain.models.TaskData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class TaskRepository private constructor(context: Context) {

    private val appContext: Context = context.applicationContext
    private val taskApi = NetworkClient.taskApi

    companion object {
        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TaskRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    suspend fun getMyTasks(): Result<MyTasksResponse> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }

            android.util.Log.d("TaskRepository", "Calling getMyTasks API with token: ${token.take(20)}...")
            val response = taskApi.getMyTasks("Bearer $token")

            android.util.Log.d("TaskRepository", "getMyTasks response - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")

            if (response.isSuccessful) {
                val myTasksResponse = response.body()
                if (myTasksResponse != null) {
                    Result.success(myTasksResponse)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Fetching tasks failed"
                } catch (e: Exception) {
                    "Fetching tasks failed: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTask(
        title: String,
        taskDescription: String,
        category: String,
        storeServiceLocation: String,
        deliveryLocation: String,
        budgetKes: Double,
        dueDate: String,
        storeServiceLatitude: Double,
        storeServiceLongitude: Double,
        deliveryLatitude: Double,
        deliveryLongitude: Double,
        shoppingItems: List<ShoppingItem>
    ): Result<CreateTaskResponse> = withContext(Dispatchers.IO) {
        try {
            // Get auth token from SharedPreferences
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }

            val request = CreateTaskRequest(
                title = title,
                task_description = taskDescription,
                category = category,
                store_service_location = storeServiceLocation,
                delivery_location = deliveryLocation,
                budget_kes = budgetKes,
                due_date = dueDate,
                store_service_latitude = storeServiceLatitude,
                store_service_longitude = storeServiceLongitude,
                delivery_latitude = deliveryLatitude,
                delivery_longitude = deliveryLongitude,
                shopping_items = shoppingItems
            )

            android.util.Log.d("TaskRepository", "Calling createTask API with token: ${token.take(20)}...")
            val response = taskApi.createTask("Bearer $token", request)

            android.util.Log.d("TaskRepository", "CreateTask response - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")

            if (response.isSuccessful) {
                val createTaskResponse = response.body()
                if (createTaskResponse != null) {
                    android.util.Log.d("TaskRepository", "Task created successfully - ID: ${createTaskResponse.data?.id}")
                    Result.success(createTaskResponse)
                } else {
                    android.util.Log.e("TaskRepository", "Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Task creation failed"
                } catch (e: Exception) {
                    "Task creation failed: ${response.code()} - ${response.message()}"
                }
                android.util.Log.e("TaskRepository", "Task creation failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("TaskRepository", "Exception creating task", e)
            Result.failure(e)
        }
    }

    // Stub methods for flow screens - TODO: Implement with real API calls
    suspend fun getTaskById(taskId: String): TaskData? = withContext(Dispatchers.IO) {
        // TODO: Implement API call to fetch task by ID
        android.util.Log.w("TaskRepository", "getTaskById not implemented yet for taskId: $taskId")
        null
    }

    suspend fun getAvailableTasks(): List<TaskData> = withContext(Dispatchers.IO) {
        // TODO: Implement API call to fetch available tasks
        android.util.Log.w("TaskRepository", "getAvailableTasks not implemented yet")
        emptyList()
    }

    suspend fun updateTask(task: TaskData): TaskData? = withContext(Dispatchers.IO) {
        // TODO: Implement API call to update task
        android.util.Log.w("TaskRepository", "updateTask not implemented yet for taskId: ${task.id}")
        task
    }

    fun initializeSampleData() {
        // Stub method for TestFlowScreen
        android.util.Log.w("TaskRepository", "initializeSampleData not implemented")
    }
}
