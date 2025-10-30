package com.demoapp.feature_auth.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.demoapp.core_network.NetworkClient
import com.demoapp.feature_auth.data.AuthTokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class BackendUploadService(private val context: Context) {
    companion object { private const val TAG = "BackendUploadService" }

    private fun getMimeType(uri: Uri): String {
        return context.contentResolver.getType(uri) ?: "image/jpeg"
    }

    private suspend fun copyUriToTempFile(uri: Uri): File = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to open input stream for URI: $uri")
        val suffix = when (getMimeType(uri)) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            else -> ".jpg"
        }
        val temp = File.createTempFile("upload_", suffix, context.cacheDir)
        FileOutputStream(temp).use { out -> input.use { it.copyTo(out) } }
        temp
    }

    suspend fun uploadIdImage(uri: Uri, bearerTokenOverride: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val storedToken = AuthTokenManager.getToken(context)
            val headerToken = bearerTokenOverride?.takeIf { it.isNotBlank() } ?: storedToken?.let { "Bearer $it" }
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            val file = copyUriToTempFile(uri)
            val mediaType = getMimeType(uri).toMediaTypeOrNull()
            val requestBody: RequestBody = file.asRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

            Log.d(TAG, "Uploading via backend: ${file.name}, type=$mediaType, field=file")
            val response = NetworkClient.profileApi.uploadIdImage(
                authorization = headerToken,
                file = part
            )

            if (response.isSuccessful) {
                val body = response.body()
                val url = body?.data?.user?.id_document ?: body?.url
                if (!url.isNullOrBlank()) {
                    return@withContext Result.success(url)
                }
                return@withContext Result.failure(Exception(body?.message ?: "Upload succeeded but URL missing"))
            }

            Result.failure(Exception("Upload failed: ${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            Log.e(TAG, "Backend upload failed", e)
            Result.failure(e)
        }
    }

    suspend fun setIdDocumentUrl(url: String, bearerTokenOverride: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val storedToken = AuthTokenManager.getToken(context)
            val headerToken = bearerTokenOverride?.takeIf { it.isNotBlank() } ?: storedToken?.let { "Bearer $it" }
                ?: return@withContext Result.failure(Exception("Not authenticated"))

            val response = NetworkClient.profileApi.setIdDocumentUrl(
                authorization = headerToken,
                request = com.demoapp.core_network.models.IdDocumentRequest(id_document = url)
            )

            if (response.isSuccessful) {
                val body = response.body()
                val savedUrl = body?.data?.user?.id_document ?: url
                return@withContext Result.success(savedUrl)
            }

            Result.failure(Exception("Upload failed: ${response.code()} ${response.message()}"))
        } catch (e: Exception) {
            Log.e(TAG, "Backend setIdDocumentUrl failed", e)
            Result.failure(e)
        }
    }
}


