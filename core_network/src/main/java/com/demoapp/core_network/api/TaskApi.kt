package com.demoapp.core_network.api

import com.demoapp.core_network.models.CreateTaskRequest
import com.demoapp.core_network.models.CreateTaskResponse
import com.demoapp.core_network.models.MyTasksResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.GET

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
}

