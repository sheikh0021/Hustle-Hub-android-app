package com.demoapp.core_network.models

data class CancelTaskResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: CancelTaskData?
)

data class CancelTaskData(
    val task_id: Int,
    val status: String
)


