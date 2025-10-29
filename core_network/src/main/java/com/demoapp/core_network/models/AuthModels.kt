package com.demoapp.core_network.models

data class RegisterRequest(
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone_number: String,
    val password: String,
    val password_confirm: String,
    val age: Int? = null,
    val gender: String? = null,
    val address: String? = null,
    val comments: String? = null,
    val profile_photo: String? = null
)

data class RegisterResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: RegisterData? = null
)

data class RegisterData(
    val user: AuthUserData,
    val tokens: Tokens
)

data class AuthUserData(
    val id: Int,
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone_number: String,
    val age: Int? = null,
    val gender: String? = null,
    val address: String? = null,
    val comments: String? = null,
    val profile_photo: String? = null
)

data class Tokens(
    val refresh: String,
    val access: String
)
