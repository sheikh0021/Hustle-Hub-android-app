# AWS S3 Setup Guide for Profile Photo Upload

This guide will help you set up AWS S3 for profile photo uploads in the Hustle Hub Android app.

## Prerequisites

1. AWS Account with S3 access
2. S3 bucket named `wkazi-backend` in the `af-south-1` region
3. AWS credentials (Access Key and Secret Key)

## Step 1: Configure AWS Credentials

### Option A: Using Hardcoded Credentials (Development Only)

1. Open `feature_auth/src/main/java/com/demoapp/feature_auth/config/AWSConfig.kt`
2. Replace the placeholder values:
   ```kotlin
   const val AWS_ACCESS_KEY = "YOUR_ACTUAL_ACCESS_KEY_HERE"
   const val AWS_SECRET_KEY = "YOUR_ACTUAL_SECRET_KEY_HERE"
   ```

### Option B: Using AWS Cognito (Recommended for Production)

1. Create a Cognito Identity Pool in AWS Console
2. Configure the Identity Pool to allow unauthenticated access to S3
3. Update the Cognito Pool ID in `AWSConfig.kt`:
   ```kotlin
   const val COGNITO_POOL_ID = "af-south-1:YOUR_ACTUAL_POOL_ID_HERE"
   ```
4. Use `SecureS3UploadService` instead of `S3UploadService` in `RegisterScreen.kt`

## Step 2: S3 Bucket Configuration

### Bucket Policy
Your S3 bucket needs the following policy to allow public read access to uploaded images:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::wkazi-backend/*"
        }
    ]
}
```

### CORS Configuration
Add this CORS configuration to your S3 bucket:

```json
[
    {
        "AllowedHeaders": ["*"],
        "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
        "AllowedOrigins": ["*"],
        "ExposeHeaders": ["ETag"]
    }
]
```

## Step 3: IAM User Permissions

If using hardcoded credentials, create an IAM user with the following policy:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:PutObjectAcl",
                "s3:GetObject",
                "s3:DeleteObject"
            ],
            "Resource": "arn:aws:s3:::wkazi-backend/*"
        }
    ]
}
```

## Step 4: Test the Integration

1. Build and run the app
2. Go to the registration screen
3. Select a profile photo
4. Check the logs for upload success/failure messages
5. Verify the image appears in your S3 bucket

## File Structure in S3

Images will be uploaded with the following structure:
```
wkazi-backend/
├── profile-photos/
│   ├── temp/
│   │   └── profile-[timestamp].jpg
│   └── user-[userId]/
│       └── profile-[timestamp].jpg
```

## Security Considerations

1. **Never commit AWS credentials to version control**
2. Use environment variables or secure storage for production
3. Consider using AWS Cognito for better security
4. Implement proper error handling and user feedback
5. Add file size and type validation

## Troubleshooting

### Common Issues:

1. **Access Denied**: Check IAM permissions and bucket policy
2. **Network Error**: Verify internet connection and AWS region
3. **Upload Fails**: Check file size limits and supported formats
4. **Image Not Displaying**: Verify bucket public read access

### Debug Logs:
Enable debug logging to see detailed upload information:
```kotlin
android.util.Log.d("S3UploadService", "Upload details...")
```

## Next Steps

1. Implement image compression before upload
2. Add progress indicators for large files
3. Implement retry logic for failed uploads
4. Add image editing capabilities (crop, resize)
5. Consider using CloudFront CDN for better performance
