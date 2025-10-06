# DemoApp Android - Job Marketplace

A comprehensive Android application built with Kotlin and Jetpack Compose that serves as a job marketplace connecting clients with workers for various tasks.

## 🚀 Features

### Client Features
- **Job Posting**: Create and post various types of jobs (Grocery Shopping, Package Delivery, House Cleaning, etc.)
- **Job Management**: View all posted jobs, track their status, and manage applications
- **Worker Selection**: Review worker applications and select the best candidate
- **Real-time Notifications**: Receive notifications for job applications, worker selections, and job updates
- **Chat Integration**: Communicate with selected workers
- **Invoice Management**: Create and manage invoices for completed jobs

### Worker Features
- **Job Discovery**: Browse available jobs and apply to interesting opportunities
- **Application Management**: Track application status and receive notifications
- **Job Start Validation**: Complete chatbot-based validation before starting jobs
- **Task Execution**: Execute jobs with timeline tracking and status updates
- **Chat Integration**: Communicate with job posters
- **Notification System**: Receive real-time updates about job applications and assignments

### Core Features
- **Real-time Chat**: Firebase-powered chat system between clients and workers
- **Timeline Tracking**: Uber-like job progress tracking with status updates
- **Notification System**: In-app notifications for all important events
- **Job Categories**: Support for multiple job types with specialized workflows
- **Payment Integration**: QR code-based payment system
- **Offline Support**: Works offline with data synchronization

## 🏗️ Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Database**: Firebase Firestore
- **Authentication**: Firebase Auth
- **Real-time Updates**: Firebase Realtime Database
- **Navigation**: Jetpack Navigation Compose
- **State Management**: StateFlow + Compose State

## 📱 Screenshots

The app includes the following main screens:
- Onboarding & Authentication
- Client Dashboard with job posting and management
- Worker Dashboard with job discovery and applications
- Job Details with application and selection features
- Real-time Chat with timeline integration
- Notification Center for both clients and workers
- Job Start Chatbot for worker validation
- Invoice Creation and Management

## 🛠️ Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK API level 24 or higher
- Firebase project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/DemoApp-android.git
   cd DemoApp-android
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned repository folder
   - Click "OK"

3. **Firebase Setup**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download `google-services.json` and place it in the `app/` directory
   - Enable the following Firebase services:
     - Authentication (Anonymous)
     - Firestore Database
     - Realtime Database

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

## 📋 Project Structure

```
app/
├── src/main/java/com/demoapp/
│   ├── MainActivity.kt                 # Main activity with navigation
│   └── ui/theme/                       # Material Design theme
├── build.gradle.kts                    # App-level build configuration
└── google-services.json               # Firebase configuration

feature_jobs/
├── src/main/java/com/demoapp/feature_jobs/
│   ├── data/                          # Data layer (repositories, models)
│   │   ├── JobRepository.kt
│   │   ├── NotificationRepository.kt
│   │   ├── JobApplicationRepository.kt
│   │   └── FirebaseChatRepository.kt
│   ├── presentation/
│   │   ├── models/                    # Data models
│   │   ├── screens/                   # UI screens
│   │   └── components/                # Reusable UI components
│   └── build.gradle.kts               # Feature module build config

feature_auth/
├── src/main/java/com/demoapp/feature_auth/
│   └── presentation/screens/          # Authentication screens

feature_onboarding/
├── src/main/java/com/demoapp/feature_onboarding/
│   └── presentation/screens/          # Onboarding screens

feature_wallet/
├── src/main/java/com/demoapp/feature_wallet/
│   └── presentation/screens/          # Payment and wallet screens

core_ui/
├── src/main/java/com/demoapp/core_ui/
│   └── theme/                         # Shared UI components and themes

core_database/
├── src/main/java/com/demoapp/core_database/
│   └── models/                        # Database models

core_network/
├── src/main/java/com/demoapp/core_network/
│   └── api/                           # Network API interfaces
```

## 🔧 Configuration

### Firebase Configuration
1. Replace the `google-services.json` file with your own Firebase project configuration
2. Update Firebase project settings in the Firebase Console
3. Ensure all required Firebase services are enabled

### Build Configuration
- Update `compileSdk` and `targetSdk` versions in `build.gradle.kts` files as needed
- Modify package names if required
- Update signing configurations for release builds

## 🚀 Key Workflows

### Job Posting Workflow
1. Client creates a job with details
2. Job becomes available for workers to apply
3. Workers apply with their information
4. Client reviews applications and selects a worker
5. Selected worker receives notification and completes job start validation
6. Job execution begins with timeline tracking
7. Job completion triggers invoice creation

### Worker Application Workflow
1. Worker browses available jobs
2. Worker applies to interesting jobs
3. Worker receives notifications about application status
4. If selected, worker completes job start chatbot validation
5. Worker executes job with real-time status updates
6. Worker communicates with client via chat
7. Job completion triggers payment process

## 🧪 Testing

The app includes sample data for testing:
- Sample jobs with different types and statuses
- Sample worker applications
- Sample notifications for both clients and workers
- Sample chat messages and timeline updates

## 📝 Development Notes

- The app uses hardcoded user IDs for demo purposes (`client_1`, `worker_1`, etc.)
- Sample data is automatically initialized on app startup
- All Firebase operations include error handling and offline support
- The notification system works with both client and worker perspectives
- Chat integration includes real-time message updates
- Timeline system provides Uber-like job progress tracking

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

If you encounter any issues or have questions:
1. Check the [Issues](https://github.com/YOUR_USERNAME/DemoApp-android/issues) page
2. Create a new issue with detailed description
3. Include device information and error logs if applicable

## 🎯 Future Enhancements

- [ ] User authentication with email/password
- [ ] Push notifications
- [ ] Advanced payment integration
- [ ] Rating and review system
- [ ] Location-based job matching
- [ ] Advanced search and filtering
- [ ] Multi-language support
- [ ] Dark mode support

---

**Built with ❤️ using Kotlin and Jetpack Compose**
