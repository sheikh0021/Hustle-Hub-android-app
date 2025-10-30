package com.demoapp.core_network.models

data class VerificationStatusResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: VerificationData?
)

data class VerificationData(
    val has_id_document: Boolean,
    val id_document_url: String?
)


