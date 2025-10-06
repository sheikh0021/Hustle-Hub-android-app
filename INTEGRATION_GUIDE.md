# Enhanced Job Posting and Management Features - Integration Guide

This guide provides comprehensive instructions for integrating the new enhanced job posting and management features into your Android app.

## 🚀 Features Implemented

### 1. Poor Network Connectivity Support
- **Offline Draft Functionality**: Jobs can be saved as drafts when offline
- **Auto-sync**: Drafts automatically sync when network connection is restored
- **Network Status Monitoring**: Real-time network connectivity monitoring

### 2. Job Payments & Weight Limits
- **Minimum Pay Enforcement**: Enforces minimum pay limit (configurable)
- **Weight Limit Warnings**: Prompts users about weight limits for practical job management
- **Payment Validation**: Ensures fair compensation for workers

### 3. Mandatory Fields
- **Required Fields**: Location, Brand, Quantity, Price, and Substitutes are now mandatory
- **Validation**: Real-time validation with clear error messages
- **User Guidance**: Clear indicators for required fields

### 4. Enhanced Location Input
- **Landmark Integration**: Nearby landmark options (schools, churches, bus stations, etc.)
- **Better Directions**: Improved location clarity for workers
- **Landmark Types**: Categorized landmark selection

### 5. Time Frames & Penalties
- **Delivery Timeframes**: Required timeframes for all jobs
- **Penalty System**: Clear penalty structure for late completion
- **Transparency**: Penalty information displayed upfront

### 6. Job Cancellation System
- **Reason Selection**: Structured cancellation reasons
- **Penalty Calculation**: Automatic penalty calculation based on timing
- **Immediate Cancellation**: Higher penalties for immediate cancellations after acceptance

### 7. Payment Guidance Chatbot
- **Step-by-step Guidance**: AI assistant guides users through payment process
- **Interactive Flow**: Conversational interface for payment assistance
- **Progress Tracking**: Visual progress indicators

## 📁 New Files Created

### Data Layer
- `OfflineJobRepository.kt` - Enhanced repository with offline support
- `NetworkConnectivityManager.kt` - Network status monitoring

### Presentation Layer
- `EnhancedJobPostingScreen.kt` - New comprehensive job posting form
- `JobCancellationScreen.kt` - Job cancellation with reason selection
- `PaymentGuidanceScreen.kt` - AI-powered payment guidance
- `DraftManagementScreen.kt` - Offline draft management
- `EnhancedJobPostingViewModel.kt` - ViewModel for job posting state management

### Models
- Updated `JobData.kt` with new fields and enums

## 🔧 Integration Steps

### Step 1: Update Dependencies
Add the following to your `build.gradle.kts` files:

```kotlin
// In feature_jobs/build.gradle.kts
dependencies {
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
}
```

### Step 2: Update Navigation
Add new routes to your navigation graph:

```kotlin
// In your navigation setup
composable("enhanced_job_posting") {
    EnhancedJobPostingScreen(
        navController = navController,
        repository = OfflineJobRepository(context, coroutineScope)
    )
}

composable("job_cancellation/{jobId}") { backStackEntry ->
    val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
    JobCancellationScreen(
        navController = navController,
        jobId = jobId,
        repository = OfflineJobRepository(context, coroutineScope)
    )
}

composable("payment_guidance") {
    PaymentGuidanceScreen(navController = navController)
}

composable("draft_management") {
    DraftManagementScreen(
        navController = navController,
        drafts = viewModel.drafts.value
    )
}
```

### Step 3: Initialize Services
In your Application class or main activity:

```kotlin
class DemoApp : Application() {
    lateinit var networkManager: NetworkConnectivityManager
    lateinit var jobRepository: OfflineJobRepository
    
    override fun onCreate() {
        super.onCreate()
        
        networkManager = NetworkConnectivityManager(this)
        jobRepository = OfflineJobRepository(this, CoroutineScope(Dispatchers.Main))
    }
}
```

### Step 4: Update Existing Screens
Replace the existing `PostJobScreen` with `EnhancedJobPostingScreen` or integrate the new features:

```kotlin
// In your main navigation or screen routing
@Composable
fun MainScreen() {
    // Replace PostJobScreen with EnhancedJobPostingScreen
    EnhancedJobPostingScreen(
        navController = navController,
        repository = jobRepository
    )
}
```

### Step 5: Add Permissions
Update your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## 🎯 Key Features Usage

### Offline Draft Management
```kotlin
// Save job as draft when offline
val result = repository.saveAsDraft(jobData)

// Auto-sync when network is restored
repository.updateNetworkStatus(isConnected = true)
```

### Job Cancellation
```kotlin
// Cancel job with reason
val result = repository.cancelJob(
    jobId = "job_123",
    reason = "Transport issues",
    reasonType = CancellationReasonType.TRANSPORT_ISSUES
)
```

### Payment Guidance
```kotlin
// Navigate to payment guidance
navController.navigate("payment_guidance")
```

### Enhanced Job Creation
```kotlin
// Create job with all new fields
val jobData = JobData(
    title = "Grocery Shopping",
    description = "Buy groceries from market",
    location = "Central Market", // Required
    brand = "Local Market", // Required
    quantity = 5, // Required
    price = 150.0, // Required
    substitutes = "Any fresh vegetables", // Required
    deliveryTimeFrame = "2 hours", // Required
    nearbyLandmark = "Central School",
    landmarkType = LandmarkType.SCHOOL,
    maxWeightLimit = 10.0,
    pay = 25.0
)
```

## 🔧 Configuration

### Customize Limits
Update constants in `OfflineJobRepository.kt`:

```kotlin
companion object {
    const val MINIMUM_PAY_LIMIT = 10.0 // Adjust minimum pay
    const val MAX_WEIGHT_LIMIT = 50.0 // Adjust weight limit
    const val CANCELLATION_PENALTY_PERCENTAGE = 10.0 // Adjust penalty
    const val LATE_PENALTY_PERCENTAGE = 5.0 // Adjust late penalty
}
```

### Customize Landmarks
Add new landmark types in `JobData.kt`:

```kotlin
enum class LandmarkType {
    SCHOOL,
    CHURCH,
    MOSQUE,
    BUS_STATION,
    HOSPITAL,
    MALL,
    MARKET,
    YOUR_CUSTOM_LANDMARK, // Add your custom landmarks
    OTHER
}
```

## 🧪 Testing

### Test Offline Functionality
1. Disable network connection
2. Create a job - should save as draft
3. Enable network - should auto-sync

### Test Validation
1. Try creating job without required fields
2. Verify error messages appear
3. Test minimum pay validation

### Test Cancellation
1. Create and accept a job
2. Cancel with different reasons
3. Verify penalty calculations

## 🚨 Important Notes

1. **Network Monitoring**: The `NetworkConnectivityManager` should be started in your Application class and stopped when the app is destroyed.

2. **Draft Storage**: Drafts are currently stored in memory. For production, consider using Room database for persistence.

3. **Payment Integration**: The payment guidance is a UI flow. Integrate with your actual payment gateway.

4. **Error Handling**: All operations return result objects for proper error handling.

5. **Performance**: The repository uses StateFlow for reactive updates. Ensure proper lifecycle management.

## 🔄 Migration from Existing Code

If you have existing job posting code:

1. **Gradual Migration**: You can use both old and new screens side by side
2. **Data Migration**: Existing jobs will work with the new system
3. **Backward Compatibility**: The new `JobData` model is backward compatible

## 📱 UI/UX Considerations

1. **Loading States**: All screens show loading indicators during operations
2. **Error Handling**: Clear error messages with actionable guidance
3. **Offline Indicators**: Visual indicators for offline state
4. **Progress Tracking**: Step-by-step progress in payment guidance
5. **Accessibility**: All components support accessibility features

## 🎨 Customization

The implementation follows Material Design 3 principles and can be customized:

1. **Colors**: Update color schemes in the composables
2. **Typography**: Modify text styles and sizes
3. **Spacing**: Adjust padding and margins
4. **Shapes**: Customize card and button shapes

## 📞 Support

For questions or issues with the integration:

1. Check the implementation files for detailed comments
2. Review the error handling in each component
3. Test with different network conditions
4. Verify all required fields are properly validated

This implementation follows MVVM + Clean Architecture principles and is designed for scalability and maintainability.
