package com.demoapp.feature_jobs.data

import android.content.Context
import com.demoapp.core_network.NetworkClient
import com.demoapp.core_network.models.CreateTaskRequest
import com.demoapp.core_network.models.CreateTaskResponse
import com.demoapp.core_network.models.MyTasksResponse
import com.demoapp.core_network.models.TaskListResponse
import com.demoapp.core_network.models.ApplyForTaskRequest
import com.demoapp.core_network.models.ApplyForTaskResponse
import com.demoapp.core_network.models.GetTaskApplicationsResponse
import com.demoapp.core_network.models.AcceptApplicationResponse
import com.demoapp.core_network.models.InvoiceCreateResponse
import com.demoapp.core_network.models.TaskCreateRequest
import com.demoapp.core_network.models.TaskCreateResponse
import com.demoapp.core_network.models.CancelTaskResponse
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

    suspend fun applyToTask(taskId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            android.util.Log.d("TaskRepository", "Calling applyToTask API for $taskId")
            val response = taskApi.applyToTask("Bearer $token", taskId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Apply failed"
                } catch (e: Exception) {
                    "Apply failed: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTaskList(
        status: String? = null,
        category: String? = null,
        page: Int? = null,
        pageSize: Int? = null
    ): Result<TaskListResponse> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            val response = taskApi.getTaskList(
                authorization = "Bearer $token",
                status = status,
                category = category,
                page = page,
                pageSize = pageSize
            )
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response body"))
            } else {
                val msg = try { response.errorBody()?.string() ?: "Fetching task list failed" } catch (e: Exception) { "Fetching task list failed: ${response.code()} - ${response.message()}" }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelTask(taskId: String): Result<CancelTaskResponse> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            val response = taskApi.cancelTask("Bearer $token", taskId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Cancel failed"
                } catch (e: Exception) {
                    "Cancel failed: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
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

    suspend fun applyForTask(taskId: String, proposedPrice: Double, message: String?): Result<ApplyForTaskResponse> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            val req = ApplyForTaskRequest(proposed_price = proposedPrice, message = message)
            val response = taskApi.applyForTask("Bearer $token", taskId, req)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Application failed"
                } catch (e: Exception) {
                    "Application failed: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTaskApplications(taskId: String): Result<GetTaskApplicationsResponse> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            val response = taskApi.getTaskApplications("Bearer $token", taskId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Failed to load applications"
                } catch (e: Exception) {
                    "Failed to load applications: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptApplication(taskId: String, userId: String): Result<AcceptApplicationResponse> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            val response = taskApi.acceptApplication("Bearer $token", taskId, userId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Failed to accept application"
                } catch (e: Exception) {
                    "Failed to accept application: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createInvoice(taskId: String): Result<InvoiceCreateResponse> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            val response = taskApi.createInvoice("Bearer $token", taskId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Failed to create invoice"
                } catch (e: Exception) {
                    "Failed to create invoice: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTask(request: TaskCreateRequest): Result<TaskCreateResponse> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            val response = taskApi.createTask("Bearer $token", request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Failed to create task"
                } catch (e: Exception) {
                    "Failed to create task: ${response.code()} - ${response.message()}"
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
