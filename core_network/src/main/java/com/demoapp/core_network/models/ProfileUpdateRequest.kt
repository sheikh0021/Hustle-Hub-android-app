package com.demoapp.core_network.models

data class ProfileUpdateRequest(
    val first_name: String,
    val last_name: String,
    val email: String
)
