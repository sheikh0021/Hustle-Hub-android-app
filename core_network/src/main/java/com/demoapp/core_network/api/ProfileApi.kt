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
import retrofit2.http.POST
import retrofit2.http.GET

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

    @Multipart
    @POST("api/auth/upload-id")
    suspend fun uploadIdImage(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part
    ): Response<com.demoapp.core_network.models.UploadIdResponse>

    @POST("api/auth/upload-id")
    suspend fun setIdDocumentUrl(
        @Header("Authorization") authorization: String,
        @Body request: com.demoapp.core_network.models.IdDocumentRequest
    ): Response<com.demoapp.core_network.models.UploadIdResponse>

    @GET("api/auth/verification-status")
    suspend fun getVerificationStatus(
        @Header("Authorization") authorization: String
    ): Response<com.demoapp.core_network.models.VerificationStatusResponse>
}
