package com.demoapp.core_network.api

import com.demoapp.core_network.models.UserProfileResponse
import com.demoapp.core_network.models.ProfileUpdateRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.PUT

interface ProfileApi {
    
    @Multipart
    @PUT("api/auth/update-profile")
    suspend fun updateProfileWithPhoto(
        @Header("Authorization") authorization: String,
        @Part("first_name") firstName: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("email") email: RequestBody,
        @Part profilePhoto: MultipartBody.Part?
    ): Response<UserProfileResponse>
    
    @PUT("api/auth/update-profile")
    suspend fun updateProfile(
        @Header("Authorization") authorization: String,
        @Body request: ProfileUpdateRequest
    ): Response<UserProfileResponse>
}
