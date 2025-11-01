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
import com.demoapp.core_network.models.TaskDetailsResponse
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
            android.util.Log.d("TaskRepository", "Applying for task: taskId=$taskId, proposedPrice=$proposedPrice, message=$message")
            
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                android.util.Log.e("TaskRepository", "Authentication token not found")
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            
            val req = ApplyForTaskRequest(proposed_price = proposedPrice, message = message)
            android.util.Log.d("TaskRepository", "Calling applyForTask API with request: $req")
            
            val response = taskApi.applyForTask("Bearer $token", taskId, req)
            
            android.util.Log.d("TaskRepository", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("TaskRepository", "Response body: $body")
                body?.let { 
                    android.util.Log.d("TaskRepository", "Application submitted successfully")
                    Result.success(it) 
                } ?: run {
                    android.util.Log.e("TaskRepository", "Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("TaskRepository", "API error: ${response.code()} - ${response.message()}, body: $errorBody")
                    
                    // Try to parse JSON error response to extract the message
                    if (errorBody != null && errorBody.contains("\"message\"")) {
                        try {
                            val jsonObject = org.json.JSONObject(errorBody)
                            jsonObject.optString("message", errorBody)
                        } catch (e: Exception) {
                            errorBody
                        }
                    } else {
                        errorBody ?: "Application failed"
                    }
                } catch (e: Exception) {
                    val msg = "Application failed: ${response.code()} - ${response.message()}"
                    android.util.Log.e("TaskRepository", msg, e)
                    msg
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("TaskRepository", "Exception in applyForTask", e)
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
            android.util.Log.d("TaskRepository", "=== CREATE INVOICE API CALL ===")
            android.util.Log.d("TaskRepository", "taskId=$taskId")
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                android.util.Log.e("TaskRepository", "Authentication token is null!")
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            android.util.Log.d("TaskRepository", "Token found, calling API: POST /api/tasks/$taskId/invoice/create")
            val response = taskApi.createInvoice("Bearer $token", taskId)
            android.util.Log.d("TaskRepository", "API response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            if (response.isSuccessful) {
                response.body()?.let { 
                    android.util.Log.d("TaskRepository", "Invoice created successfully: ${it.data?.invoice_number}")
                    Result.success(it) 
                } ?: run {
                    android.util.Log.e("TaskRepository", "Empty response body from createInvoice API")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Failed to create invoice"
                } catch (e: Exception) {
                    "Failed to create invoice: ${response.code()} - ${response.message()}"
                }
                android.util.Log.e("TaskRepository", "API call failed: $errorMessage (code: ${response.code()})")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            android.util.Log.e("TaskRepository", "Exception in createInvoice: ${e.message}", e)
            android.util.Log.e("TaskRepository", "Exception type: ${e::class.simpleName}")
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

    suspend fun getTaskDetails(taskId: String): Result<TaskDetailsResponse> = withContext(Dispatchers.IO) {
        try {
            val token = AuthTokenManager.getToken(appContext)
            if (token == null) {
                return@withContext Result.failure(Exception("Authentication token not found. Please login again."))
            }
            android.util.Log.d("TaskRepository", "Calling getTaskDetails API for taskId: $taskId")
            val response = taskApi.getTaskDetails("Bearer $token", taskId)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Failed to fetch task details"
                } catch (e: Exception) {
                    "Failed to fetch task details: ${response.code()} - ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
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
