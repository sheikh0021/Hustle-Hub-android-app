package com.demoapp.core_network.models

data class LoginRequest(
    val phone_number: String,
    val password: String
)

data class LoginResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: LoginData? = null
)

data class LoginData(
    val user: AuthUserData,
    val tokens: Tokens
)
