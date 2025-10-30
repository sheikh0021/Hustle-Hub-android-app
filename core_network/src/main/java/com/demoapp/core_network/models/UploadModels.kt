package com.demoapp.core_network.models

data class UploadIdResponse(
    val error: Boolean? = null,
    val message: String? = null,
    val success: Boolean? = null,
    val data: Data? = null,
    // Fallback for earlier/simple responses
    val url: String? = null
) {
    data class Data(
        val user: User? = null
    )

    data class User(
        val id: Int? = null,
        val phone_number: String? = null,
        val first_name: String? = null,
        val last_name: String? = null,
        val email: String? = null,
        val is_verified: Boolean? = null,
        val id_document: String? = null
    )
}

data class IdDocumentRequest(
    val id_document: String
)


