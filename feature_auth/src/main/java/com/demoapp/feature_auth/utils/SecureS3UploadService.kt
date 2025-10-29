package com.demoapp.feature_auth.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.demoapp.feature_auth.config.AWSConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.*

class SecureS3UploadService(private val context: Context) {
    
    companion object {
        private const val BUCKET_NAME = "wkazi-backend"
        private const val REGION = "af-south-1"
        private const val TAG = "SecureS3UploadService"
        
        // AWS Cognito Identity Pool ID - Replace with your actual pool ID
        private const val COGNITO_POOL_ID = "af-south-1:YOUR_COGNITO_POOL_ID_HERE"
    }
    
    private val s3Client: AmazonS3Client by lazy {
        val credentials = BasicAWSCredentials(AWSConfig.AWS_ACCESS_KEY, AWSConfig.AWS_SECRET_KEY)
        AmazonS3Client(credentials, Region.getRegion(Regions.fromName(AWSConfig.S3_REGION)))
    }
    
    suspend fun uploadProfileImage(
        imageUri: Uri,
        userId: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting secure S3 upload for URI: $imageUri")
            
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return@withContext Result.failure(Exception("Could not open input stream from URI"))
            }
            
            // Generate unique filename
            val fileExtension = getFileExtension(imageUri)
            val fileName = if (userId != null) {
                "profile-photos/user-$userId/profile-${System.currentTimeMillis()}.$fileExtension"
            } else {
                "profile-photos/temp/profile-${UUID.randomUUID()}.$fileExtension"
            }
            
            Log.d(TAG, "Uploading to S3 with filename: $fileName")
            
            // Create metadata
            val metadata = ObjectMetadata().apply {
                contentType = getContentType(fileExtension)
                contentLength = inputStream.available().toLong()
                // Add cache control for better performance
                cacheControl = "public, max-age=31536000" // 1 year
            }
            
            // Create put request
            val putRequest = PutObjectRequest(BUCKET_NAME, fileName, inputStream, metadata)
            
            // Upload to S3
            s3Client.putObject(putRequest)
            
            // Generate the public URL
            val imageUrl = "https://$BUCKET_NAME.s3.$REGION.amazonaws.com/$fileName"
            
            Log.d(TAG, "S3 upload successful. URL: $imageUrl")
            Result.success(imageUrl)
            
        } catch (e: Exception) {
            Log.e(TAG, "S3 upload failed", e)
            Result.failure(e)
        }
    }
    
    private fun getFileExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg" // default
        }
    }
    
    private fun getContentType(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
    }
    
    fun deleteProfileImage(imageUrl: String): Result<Unit> {
        return try {
            // Extract the key from the URL
            val key = imageUrl.substringAfter("$BUCKET_NAME.s3.$REGION.amazonaws.com/")
            s3Client.deleteObject(BUCKET_NAME, key)
            Log.d(TAG, "Successfully deleted image: $imageUrl")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete image: $imageUrl", e)
            Result.failure(e)
        }
    }
}
