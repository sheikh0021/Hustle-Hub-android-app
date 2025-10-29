package com.demoapp.core_network.api

import com.demoapp.core_network.models.LoginRequest
import com.demoapp.core_network.models.LoginResponse
import com.demoapp.core_network.models.RegisterRequest
import com.demoapp.core_network.models.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") authorization: String): Response<Unit>
}
