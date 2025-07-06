# SyncWell: Health & Wellness Tracking App

## App Overview

SyncWell is a comprehensive Android application designed to help users manage their health and wellness journey through an intuitive, visually appealing interface. The app combines task management using the Eisenhower Matrix method, medicine reminders, and wellness tracking (sleep, water intake, and activity) in a unified experience. The UI is inspired by Apple Watch's clean aesthetic, featuring smooth animations and easy navigation patterns to create a delightful user experience.

## Architecture & Tech Stack

### Frontend
- **Jetpack Compose**: Our entire UI is built using Android's modern declarative UI toolkit, enabling us to create beautiful, responsive interfaces with less code and more intuitive state management.
- **Navigation Component**: We use Jetpack's Navigation framework with Compose integration to handle screen transitions with proper backstack management.
- **ViewModel + StateFlow**: UI state is managed through the MVVM pattern with ViewModels exposing immutable StateFlow objects that Compose observes for reactivity.

### Backend
- **Room Database**: Local SQLite database through Room for storing all user data including tasks, medicines, and wellness metrics, providing offline capabilities.
- **Firebase Authentication**: User authentication via Google Sign-In to enable account features and data synchronization.
- **Firebase Firestore**: (Optional) Cloud storage for data backup and multi-device synchronization.
- **WorkManager**: Handles background tasks and scheduling medicine reminders even when the app is not active.
- **AlarmManager + Notifications**: Provides precise timing for reminders with user-friendly notifications.
- **Google Fit Integration**: Optional connection to Google's fitness APIs for enhanced activity tracking.

## Database Schema Design

Our database design follows a clear entity relationship model:

### User Entity
- Stores basic user information including display name and authentication details
- Central relation point for all other entities

### Task Entity
- Fields: id, title, description, due date, importance (HIGH/LOW), urgency (HIGH/LOW), isCompleted, recurrence pattern
- Relationships: Belongs to a user

### Medicine Entity
- Fields: id, name, dosage, instructions, schedule, reminder times, quantity, refill threshold
- Relationships: Belongs to a user, has many scheduled reminders

### Wellness Entry Entity
- Fields: id, date, sleep hours, sleep goal, water intake, water goal, activity metrics
- Relationships: Belongs to a user, daily entries

## Core Features Implementation

### Dashboard

The dashboard serves as the central hub of the application, providing quick insights and navigation to all main features.

**Implementation Details:**
- Custom cards displaying upcoming tasks, medicines, and wellness statistics
- Circular progress metrics for sleep and water tracking with visual indicators
- Quick action buttons for common tasks
- User profile access
- Implemented in `DashboardScreen.kt` with composables for each section

### Task Scheduler Mode

The task scheduler implements the Eisenhower Matrix methodology, helping users prioritize tasks based on importance and urgency.

**Implementation Details:**
- Task creation form with title, description, deadline picker, and importance selectors
- Matrix view showing the four quadrants: Important/Urgent, Important/Not Urgent, Not Important/Urgent, Not Important/Not Urgent
- Drag-and-drop interaction for re-categorizing tasks between quadrants
- Task completion tracking with statistics
- Recurring task pattern implementation using a flexible recurrence rule system
- Database operations handled via TaskViewModel and TaskRepository

### Medicine Reminder Mode

The medicine reminder system helps users manage their medication schedule with customizable reminders.

**Implementation Details:**
- Medicine creation form with fields for name, dosage, schedule, and instructions
- Schedule builder offering daily, specific days, or interval-based options
- Reminder system using AlarmManager for precise timing
- Notification delivery through NotificationManager with proper channels
- Medication tracking to record compliance (taken/missed)
- Refill alert system triggered when medicine quantity falls below the threshold
- Implementation spread across MedicineViewModel, MedicineRepository, and MedicineAlarmReceiver

### Wellness Features

Wellness tracking provides users with tools to monitor and improve their sleep, hydration, and activity levels.

**Implementation Details:**
- **Sleep Tracking**: 
  - Bedtime reminder system
  - Sleep duration input and goal setting
  - Historical sleep data visualization
  
- **Water Intake**: 
  - Quick-add buttons for water consumption
  - Custom goal setting based on user preferences
  - Reminder system with customizable intervals
  
- **Activity Monitoring**: 
  - Step counting through device sensors or Google Fit
  - Sedentary time tracking with gentle reminder notifications
  - Activity goals and achievement tracking
  
- All wellness metrics stored in the WellnessEntry entity with daily records
- Implemented through WellnessViewModel and supporting repositories

### Language Support & Accessibility

The app is designed to be accessible to all users regardless of language preference or accessibility needs.

**Implementation Details:**
- **Multi-language Support**:
  - String resources in multiple languages (currently English and Spanish)
  - Language selection in user profile
  - Runtime language switching without app restart
  - Implementation in LanguageUtils.kt and applied in MainActivity and SyncWellApp

- **Accessibility Features**:
  - TalkBack support with semantic properties for all interactive elements
  - Content descriptions for images and icons
  - Proper contrast ratios for text and UI elements
  - Support for larger text sizes
  - Navigation without relying solely on gestures or color

## UI/UX Design Principles

Our design approach focuses on clarity, consistency, and user delight:

1. **Dark Mode First**: A dark theme with careful color accents reduces eye strain and battery usage.
2. **Card-Based Layout**: Information is organized in distinct cards with clear visual hierarchy.
3. **Circular Progress Indicators**: Provides intuitive visualization of completion status.
4. **Consistent Color Coding**: Different features maintain their unique color identity throughout the app.
5. **Animation & Transitions**: Subtle animations provide feedback and orientation during navigation.
6. **Floating Action Buttons**: Quick access to common actions from anywhere in the app.

## App Startup & Navigation Flow

1. **Splash Screen**: Custom branded splash screen showing the app logo.
2. **Authentication**: Google Sign-In option for user identification.
3. **Main Navigation**: Bottom navigation or tab-based switching between Dashboard, Tasks, Medicines, and Wellness.
4. **Detail Screens**: Secondary screens for item creation, editing, and detailed views.

## Background Processing & Notifications

1. **WorkManager Jobs**: Scheduled tasks for daily metric resets, data syncing, and reminder preparation.
2. **Notification Channels**: Separate channels for medicine reminders, task due dates, and wellness prompts.
3. **AlarmManager**: Precise timing for critical reminders even when the app is closed.

## Future Enhancement Opportunities

1. **Cloud Sync**: Expanded Firebase integration for cross-device synchronization.
2. **Social Features**: Friend challenges for wellness goals.
3. **Health Device Integration**: Support for Bluetooth health devices.
4. **AI Recommendations**: Smart suggestions based on user patterns.
5. **Widget Support**: Home screen widgets for key metrics and upcoming items.

## Development Best Practices

Throughout development, we adhere to:

1. **Dependency Injection**: Using Hilt for clean component management.
2. **Repository Pattern**: Data operations abstracted from UI components.
3. **Single Responsibility**: Classes and functions have focused roles.
4. **Unit Testing**: Core business logic and database operations covered by tests.
5. **Documentation**: Clear documentation for complex algorithms and custom implementations.

This implementation guide provides a comprehensive overview of the SyncWell application, its architecture, and feature set. Developers can use this as a reference for understanding the overall system design and implementation details. 