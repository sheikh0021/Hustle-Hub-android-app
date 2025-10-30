package com.demoapp.core_network

import com.demoapp.core_network.api.AuthApi
import com.demoapp.core_network.api.ProfileApi
import com.demoapp.core_network.api.TaskApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    
    private const val BASE_URL = "http://16.28.133.78:8000/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        // No Host override when using a real IP/base URL
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val profileApi: ProfileApi = retrofit.create(ProfileApi::class.java)
    val taskApi: TaskApi = retrofit.create(TaskApi::class.java)
}
