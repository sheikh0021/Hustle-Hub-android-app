package com.demoapp.core_network.api

import com.demoapp.core_network.models.CreateTaskRequest
import com.demoapp.core_network.models.CreateTaskResponse
import com.demoapp.core_network.models.MyTasksResponse
import com.demoapp.core_network.models.TaskListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.DELETE

interface TaskApi {
    
    @POST("api/tasks/create")
    suspend fun createTask(
        @Header("Authorization") authorization: String,
        @Body request: CreateTaskRequest
    ): Response<CreateTaskResponse>

    @GET("api/tasks/my-tasks")
    suspend fun getMyTasks(
        @Header("Authorization") authorization: String
    ): Response<MyTasksResponse>

    @POST("api/tasks/{id}/apply")
    suspend fun applyToTask(
        @Header("Authorization") authorization: String,
        @Path("id") taskId: String
    ): Response<Unit>

    @POST("api/tasks/{task_id}/apply")
    suspend fun applyForTask(
        @Header("Authorization") authorization: String,
        @Path("task_id") taskId: String,
        @Body request: com.demoapp.core_network.models.ApplyForTaskRequest
    ): Response<com.demoapp.core_network.models.ApplyForTaskResponse>

    @GET("api/tasks/{task_id}/applications")
    suspend fun getTaskApplications(
        @Header("Authorization") authorization: String,
        @Path("task_id") taskId: String
    ): Response<com.demoapp.core_network.models.GetTaskApplicationsResponse>

    @POST("api/tasks/{task_id}/accept/{user_id}")
    suspend fun acceptApplication(
        @Header("Authorization") authorization: String,
        @Path("task_id") taskId: String,
        @Path("user_id") userId: String
    ): Response<com.demoapp.core_network.models.AcceptApplicationResponse>

    @POST("api/tasks/{task_id}/invoice/create")
    suspend fun createInvoice(
        @Header("Authorization") authorization: String,
        @Path("task_id") taskId: String
    ): Response<com.demoapp.core_network.models.InvoiceCreateResponse>

    @POST("api/tasks/create")
    suspend fun createTask(
        @Header("Authorization") authorization: String,
        @Body request: com.demoapp.core_network.models.TaskCreateRequest
    ): Response<com.demoapp.core_network.models.TaskCreateResponse>

    @GET("api/tasks/list")
    suspend fun getTaskList(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Query("status") status: String? = null,
        @retrofit2.http.Query("category") category: String? = null,
        @retrofit2.http.Query("page") page: Int? = null,
        @retrofit2.http.Query("page_size") pageSize: Int? = null
    ): Response<TaskListResponse>

    @DELETE("api/tasks/{id}/cancel")
    suspend fun cancelTask(
        @Header("Authorization") authorization: String,
        @Path("id") taskId: String
    ): Response<com.demoapp.core_network.models.CancelTaskResponse>

    @GET("api/tasks/{task_id}")
    suspend fun getTaskDetails(
        @Header("Authorization") authorization: String,
        @Path("task_id") taskId: String
    ): Response<com.demoapp.core_network.models.TaskDetailsResponse>
}

