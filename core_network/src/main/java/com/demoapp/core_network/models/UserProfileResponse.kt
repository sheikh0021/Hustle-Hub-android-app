package com.demoapp.core_network.models

data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val data: UserData
)

data class UserData(
    val user: User
)

data class User(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone_number: String,
    val profile_photo: String? = null
)
