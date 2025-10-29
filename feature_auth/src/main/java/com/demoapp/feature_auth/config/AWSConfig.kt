package com.demoapp.feature_auth.config

object AWSConfig {
    // S3 Configuration
    const val S3_BUCKET_NAME = "wkazi-backend"
    const val S3_REGION = "af-south-1"
    
    // AWS Credentials (for development only - use Cognito in production)
    // TODO: Replace with your actual AWS credentials
    const val AWS_ACCESS_KEY = "YOUR_ACCESS_KEY_HERE"  // Replace with actual access key
    const val AWS_SECRET_KEY = "YOUR_SECRET_KEY_HERE"  // Replace with actual secret key
    
    // AWS Cognito Configuration (recommended for production)
    const val COGNITO_POOL_ID = "af-south-1:YOUR_COGNITO_POOL_ID_HERE"
    
    // S3 Upload Settings
    const val MAX_FILE_SIZE_MB = 10L
    const val ALLOWED_IMAGE_TYPES = "image/jpeg,image/png,image/gif,image/webp"
    
    // S3 Folder Structure
    const val PROFILE_PHOTOS_FOLDER = "profile-photos"
    const val TEMP_FOLDER = "temp"
    
    fun getS3BaseUrl(): String {
        return "https://$S3_BUCKET_NAME.s3.$S3_REGION.amazonaws.com"
    }
    
    fun generateProfilePhotoKey(userId: String, fileExtension: String): String {
        return "$PROFILE_PHOTOS_FOLDER/user-$userId/profile-${System.currentTimeMillis()}.$fileExtension"
    }
    
    fun generateTempPhotoKey(fileExtension: String): String {
        return "$PROFILE_PHOTOS_FOLDER/$TEMP_FOLDER/profile-${System.currentTimeMillis()}.$fileExtension"
    }
}
