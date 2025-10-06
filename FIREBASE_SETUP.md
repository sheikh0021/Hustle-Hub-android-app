# Firebase Setup Guide for Chat System

## 🔥 Firebase Configuration

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project"
3. Enter project name: `demoapp-android`
4. Enable Google Analytics (optional)
5. Create project

### 2. Add Android App to Firebase

1. In Firebase Console, click "Add app" → Android
2. Enter package name: `com.demoapp`
3. Enter app nickname: `DemoApp Android`
4. Download `google-services.json`
5. Replace the placeholder file in `app/google-services.json`

### 3. Enable Firestore Database

1. In Firebase Console, go to "Firestore Database"
2. Click "Create database"
3. Choose "Start in test mode" (for development)
4. Select a location (choose closest to your users)

### 4. Set Up Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Chat messages - users can read/write messages for jobs they're involved in
    match /chat_messages/{messageId} {
      allow read, write: if request.auth != null && 
        (resource.data.senderId == request.auth.uid || 
         request.auth.uid in resource.data.participants);
    }
    
    // Chat rooms - users can read/write rooms they're participants in
    match /chat_rooms/{roomId} {
      allow read, write: if request.auth != null && 
        (request.auth.uid == resource.data.clientId || 
         request.auth.uid == resource.data.workerId);
    }
  }
}
```

### 5. Enable Authentication (Optional but Recommended)

1. In Firebase Console, go to "Authentication"
2. Click "Get started"
3. Go to "Sign-in method" tab
4. Enable "Email/Password" or "Anonymous" authentication

## 📱 App Configuration

### 1. Update Dependencies

The following dependencies have been added to your `build.gradle.kts` files:

```kotlin
// Firebase BOM
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")
```

### 2. Google Services Plugin

The Google Services plugin has been added to your project:

```kotlin
// Project-level build.gradle.kts
id("com.google.gms.google-services") version "4.4.0" apply false

// App-level build.gradle.kts
id("com.google.gms.google-services")
```

## 🗄️ Firestore Database Structure

### Collections

#### 1. `chat_messages`
```json
{
  "id": "message_uuid",
  "jobId": "job_1",
  "text": "Hello! I'm interested in this job.",
  "senderId": "worker_john_kamau",
  "senderName": "John Kamau",
  "senderType": "WORKER",
  "timestamp": 1703123456789,
  "isRead": false
}
```

#### 2. `chat_rooms`
```json
{
  "jobId": "job_1",
  "jobTitle": "Package Delivery to Karen",
  "clientId": "client_sarah",
  "clientName": "Sarah Johnson",
  "workerId": "worker_john_kamau",
  "workerName": "John Kamau",
  "lastMessageTime": 1703123456789,
  "unreadCount": 0
}
```

## 🚀 Features Implemented

### Real-time Messaging
- Messages are stored in Firestore
- Real-time updates using Firestore listeners
- Automatic UI updates when new messages arrive

### Chat Room Management
- Automatic chat room creation for each job
- Worker assignment to chat rooms
- Unread message counting

### Message Persistence
- All messages are permanently stored in Firestore
- Message history is maintained across app sessions
- Offline support (messages sync when connection is restored)

## 🧪 Testing the Firebase Chat

### 1. Run the App
```bash
./gradlew assembleDebug
```

### 2. Test Scenarios

#### Send Messages
1. Navigate to Worker Dashboard
2. Tap "💬 Chat" on any job
3. Type a message and send
4. Message should appear instantly

#### Real-time Updates
1. Open chat in two different devices/emulators
2. Send message from one device
3. Message should appear on the other device immediately

#### Message Persistence
1. Send some messages
2. Close and reopen the app
3. Messages should still be there

### 3. Firebase Console Verification

1. Go to Firebase Console → Firestore Database
2. Check `chat_messages` collection for your messages
3. Check `chat_rooms` collection for chat room data

## 🔧 Troubleshooting

### Common Issues

1. **Build Errors**: Make sure `google-services.json` is in the correct location
2. **Permission Denied**: Check Firestore security rules
3. **No Real-time Updates**: Verify Firestore listeners are properly set up
4. **Messages Not Saving**: Check Firebase project configuration

### Debug Tips

1. Enable Firebase Analytics to monitor usage
2. Check Firebase Console logs for errors
3. Use Firebase Emulator Suite for local testing
4. Monitor Firestore usage in Firebase Console

## 📊 Monitoring

### Firebase Console
- Monitor message count in Firestore
- Check authentication usage
- View analytics data

### App Performance
- Real-time message delivery
- Offline message queuing
- Network usage optimization

## 🔐 Security Considerations

1. **Authentication**: Implement proper user authentication
2. **Security Rules**: Use proper Firestore security rules
3. **Data Validation**: Validate message content on client and server
4. **Rate Limiting**: Implement message rate limiting
5. **Content Moderation**: Add content filtering for inappropriate messages

## 🚀 Production Deployment

### Before Going Live

1. **Update Security Rules**: Move from test mode to production rules
2. **Enable Authentication**: Implement proper user authentication
3. **Set Up Monitoring**: Configure Firebase monitoring and alerts
4. **Backup Strategy**: Set up Firestore backup
5. **Performance Optimization**: Optimize queries and indexes

### Production Security Rules Example

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /chat_messages/{messageId} {
      allow read, write: if request.auth != null && 
        request.auth.uid in resource.data.participants &&
        request.time < resource.data.timestamp + duration.value(1, 'h');
    }
  }
}
```

This setup provides a robust, scalable chat system with real-time messaging, persistent storage, and proper security measures.
