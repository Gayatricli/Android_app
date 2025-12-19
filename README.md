📱 StressEase – Android Frontend

StressEase is an Android-based mental wellness application designed to help users monitor, understand, and manage stress through daily mood tracking, quizzes, AI-powered insights, and guided relaxation activities.
This repository contains the Android frontend implementation of the StressEase system.

🧠 Project Overview

The StressEase Android application serves as the user-facing interface of the system. It allows users to:

Track daily mood and emotional state

Participate in structured quizzes (Mood & DASS-based)

Interact with an AI-powered chat assistant

View stress analytics, predictions, and reports

Practice guided breathing and relaxation exercises

Manage profile, privacy, and notification settings

The app communicates securely with a Flask-based backend and uses Firebase services for authentication and real-time data storage.

🏗️ Architecture (Frontend Perspective)

Platform: Android

Language: Kotlin

UI: XML + Material Design Components

Architecture Pattern: Activity + Fragment-based modular design

Networking: Retrofit (REST API communication)

Authentication: Firebase Authentication

Database: Firebase Firestore

Session Handling: Firebase ID Tokens

✨ Key Modules & Features
🔐 Authentication Module

User Login & Registration

Firebase Authentication (Email/Password)

Secure session handling using Firebase ID tokens

🏠 Home Dashboard

Central navigation hub

Quick access to quizzes, chat, analytics, and settings

🧩 Mood & Quiz Module

Daily mood quiz (12 questions)

Core, rotating, and DASS-based questions

Data sent to backend via REST APIs

Quiz results stored in Firestore

💬 Chat Module

AI-powered chat interface

Sends user messages to Flask backend

Displays AI-generated responses in real time

Chat history persisted in Firestore

📊 Analytics & Summary Module

Displays mood trends and quiz summaries

Uses backend /predict endpoint for stress prediction

Shows predicted stress level with confidence score

📈 Reports Module

Weekly and daily insights

Visualization of stress patterns and emotional trends

🧘 Breathing & Relaxation Module

Guided breathing exercises

Timed animations for inhale/exhale cycles

Designed to reduce immediate stress

🏆 Leaderboard Module

Displays user rankings based on engagement and wellness metrics

Highlights current user position

⚙️ Settings Module

Edit Profile

Notification Settings

Data Storage Preferences

Privacy & Security Controls

Stress Goals Configuration

🔗 Backend Integration

The Android app communicates with the backend using Retrofit.

Example API Flow:

User submits quiz →

Android app sends JSON payload with auth token →

Flask backend processes data & stores in Firestore →

Backend returns insights →

Android app renders results in UI

All API calls include:

Authorization: Bearer <Firebase_ID_Token>

Content-Type: application/json

🔐 Firebase Usage

Firebase Authentication

Handles login/signup

Generates secure ID tokens

Firestore Database

Stores user profiles

Chat messages

Mood logs

Analytics summaries

📂 Project Structure (Module-wise Overview)
Root Directory

Android_app

README.md

build.gradle

settings.gradle

gradle

gradlew / gradlew.bat

📱 Android Application Source

Path:
app/src/main/java/com/example/stressease

Module 1: User Authentication & Navigation

Package: LoginMain

Files:

BaseActivity.kt

LoginActivity.kt

MainActivity.kt

HomeFragment.kt

Purpose: Handles user login, session flow, and primary app navigation.

Module 2: Chatbot & Crisis Support

Package: chats

Files:

ChatActivity.kt

ChatAdapter.kt

ChatFragment.kt

ChatMessage.kt

SOS (crisis support resources)

Purpose: Provides AI-assisted chat support and emergency mental-health resources.

Module 3: Mood Assessment & Quiz

Package: QuizMood

Files:

MoodFragment.kt

QuizFragment.kt

Purpose: Collects user mood inputs and quiz responses for stress evaluation.

Module 4: Analytics, Therapy & Gamification

Package: Analytics

Files:

AnalyticsChatItem.kt

DataAnalyticsActivity.kt

Summary.kt

ReportsActivity.kt

History.kt

HistoryAdapter.kt

Suggestions.kt

Breathing.kt

LeaderBoard.kt

LeaderboardAdapter.kt

EnhancedLeaderboardAdapter.kt

LeaderboardEntry.kt

Purpose: Analyzes user data, provides stress insights, breathing therapy, and engagement via leaderboard.

Module 5: Settings & User Preferences

Package: Settings

Files:

SettingsActivity.kt

SettingsScreen.kt

SettingsViewModel.kt

SettingsRepository.kt

SettingsDataStore.kt

SettingsChildScreens.kt

NotificationSettingsScreen.kt

PrivacySecurityConcern.kt

EditProfileScreen.kt

DataStorageScreen.kt

StressGoalsScreen.kt

AboutSupportScreen.kt

Purpose: Manages user preferences, privacy controls, notifications, and profile data.

Supporting Modules
API Integration

Package: Api

ApiService.kt

RetrofitClient.kt

Handles backend communication with Flask services.

Local Storage

Package: LocalStorageOffline

SharedPreference.kt

Stores user data locally for offline access.

🎨 Resources

Path: app/src/main/res

Includes:

layout

drawable

values (colors.xml, styles.xml, themes.xml)

navigation

🧪 Testing

androidTest → ExampleInstrumentedTest.kt

test → ExampleUnitTest.kt


▶️ How to Run the Android App

Clone the repository

git clone https://github.com/Gayatricli/Android_app.git


Open the project in Android Studio

Connect Firebase:

Add google-services.json

Enable Authentication & Firestore in Firebase Console

Update backend base URL in Retrofit config

Run on emulator or physical device

🎓 Academic Relevance

This project demonstrates practical application of:

Mobile Application Development

REST API Integration

Cloud Databases (Firebase Firestore)

Secure Authentication

AI-assisted mental health systems

🚀 Future Enhancements

Offline mode support

Push notifications using Firebase Cloud Messaging

Advanced data visualizations

Multi-language support

Wearable device integration

👩‍💻 Developed By

Gayatri Damle

BSc Computer Science – BITS Pilani (Online)

📜 License

This project is developed for academic purposes.


