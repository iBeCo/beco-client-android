# Beco Client Android App

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=26)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.8+-blue.svg)](https://kotlinlang.org)

A comprehensive Android client application demonstrating the integration and capabilities of the Becomap Android SDK for interactive indoor mapping.

## 📁 Repository Structure

```
beco-client-android/
├── app/                          # Main Android application
│   ├── src/main/java/com/beco/demo/
│   │   ├── MainActivity.java     # Main activity with map integration
│   │   ├── SearchActivity.java   # Location search functionality
│   │   ├── SplashActivity.java   # App splash screen
│   │   └── components/           # Reusable UI components
│   │       ├── FloorSwitcherComponent.java
│   │       ├── LocationDetailsCard.java
│   │       ├── RouteInfoBarComponent.java
│   │       └── SearchBarComponent.java
│   ├── src/main/res/             # Android resources
│   └── build.gradle.kts          # App build configuration
├── .gitignore                    # Git ignore rules
├── README.md                     # This file
└── gradle/                       # Gradle wrapper files
```

## 🚀 Features

### App Capabilities
- **Interactive Indoor Maps**: Display detailed floor plans and building layouts using Becomap SDK
- **Multi-Floor Navigation**: Switch between different floors seamlessly
- **Location Search & Highlighting**: Advanced search functionality with real-time filtering
- **Category-Based Filtering**: Filter locations by categories with visual icons
- **Route Planning**: Calculate and display routes between locations
- **Custom UI Components**: Modular, reusable components for map interactions
- **Material Design**: Modern Android UI following Material Design principles
- **Real-time Interactions**: Responsive map interactions and location selection
- **Error Handling**: Comprehensive error handling with user-friendly messages

### Technical Features
- **Becomap SDK Integration**: Uses published Becomap SDK (v2.0.3)
- **Modern Android Architecture**: Built with latest Android development practices
- **View Binding**: Type-safe view binding for improved performance
- **Component-Based UI**: Modular UI components for maintainability
- **ProGuard/R8 Support**: Code obfuscation and optimization for release builds
- **Comprehensive Testing**: Unit tests and instrumented tests
- **Material Components**: Google's Material Design components

## 🆕 What's New

### Latest Updates - Enhanced Client App

#### 🎯 Improved User Experience
- **Modern UI Components**: Redesigned interface with Material Design principles
- **Enhanced Search**: Real-time location search with category filtering
- **Route Planning**: Integrated route calculation and display
- **Floor Navigation**: Intuitive floor switching with visual indicators
- **Location Details**: Rich location information cards

#### 🔧 Technical Improvements
- **SDK Integration**: Updated to use Becomap SDK v2.0.3
- **Component Architecture**: Modular UI components for better maintainability
- **Error Handling**: Comprehensive error handling with user feedback
- **Performance**: Optimized rendering and data caching
- **Build Configuration**: Enhanced build types with ProGuard support

### 📋 Getting Started Guide

#### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 26+ (Android 8.0)
- Java 8+ or Kotlin 1.8+
- Becomap SDK credentials (Client ID, Client Secret, Site Identifier)

#### Quick Setup
1. Clone this repository
2. Open in Android Studio
3. Update credentials in `MainActivity.java`
4. Build and run the app

#### Configuration
Update the credentials in `MainActivity.java`:
```java
private static final String CLIENT_ID = "your_client_id";
private static final String CLIENT_SECRET = "your_client_secret";
private static final String SITE_IDENTIFIER = "your_site_identifier";
```

### 🔍 Advanced Search Feature

The demo app includes a comprehensive search implementation showcasing SDK capabilities:

#### Search Interface
- **Real-time search** as user types
- **Category filtering** with visual icons
- **Professional UI** with Material Design
- **Empty state handling** for no results

#### Category Icons
- **Dynamic icon mapping** based on `BCCategory.iconName`
- **Smart fallbacks** for unknown categories
- **Visual consistency** across the interface
- **Color coordination** with selection states

#### Search Flow
```
User taps search → SearchActivity launches → Real-time filtering → Location selection → Map highlighting
```

#### Technical Implementation
- **Serializable models** for seamless data transfer
- **Static data caching** for performance
- **Location ID validation** before selection
- **Graceful error handling** with fallbacks

## 📋 Requirements

- **Android API Level**: 26+ (Android 8.0)
- **Kotlin**: 1.8+
- **Gradle**: 8.0+
- **AndroidX**: Required
- **WebView**: Enabled (for map rendering)

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <your-repository-url>
cd beco-client-android
```

### 2. Open in Android Studio
1. Launch Android Studio
2. Select "Open an existing project"
3. Navigate to the cloned repository folder
4. Wait for Gradle sync to complete

### 3. Configure Credentials
Update the credentials in `app/src/main/java/com/beco/demo/MainActivity.java`:
```java
private static final String CLIENT_ID = "your_client_id";
private static final String CLIENT_SECRET = "your_client_secret";
private static final String SITE_IDENTIFIER = "your_site_identifier";
```

### 4. Build and Run
1. Connect an Android device or start an emulator (API 26+)
2. Select the `app` module
3. Click "Run" or press `Ctrl+R` (Windows/Linux) / `Cmd+R` (macOS)

### 5. Verify Installation
Build the project to ensure everything is working:
```bash
./gradlew build
```

## 🏗️ Architecture & Components

### App Architecture
The app follows a component-based architecture with the following key components:

#### Core Components
- **MainActivity**: Main activity handling map initialization and coordination
- **SearchActivity**: Dedicated search interface for locations
- **SplashActivity**: App startup and initialization screen

#### UI Components
- **SearchBarComponent**: Search input and route planning interface
- **FloorSwitcherComponent**: Floor navigation controls
- **LocationDetailsCard**: Location information display
- **RouteInfoBarComponent**: Route information and controls

#### SDK Integration
The app uses the published Becomap SDK:
```kotlin
dependencies {
    implementation("com.becomap.sdk:becomap:2.0.3")
}
```

### 📱 App Configuration

The app is configured for optimal performance with the Becomap SDK:

#### Portrait Mode Optimization
The app is locked to portrait mode for the best user experience:
```xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="portrait"
    android:configChanges="orientation|screenSize|keyboardHidden">
</activity>
```

#### Permissions
Required permissions for location services and map functionality:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

## 📱 App Usage

### Main Features

#### 1. Interactive Map
- View detailed indoor maps with floor plans
- Navigate between different floors using the floor switcher
- Zoom and pan to explore the venue

#### 2. Location Search
- Tap the search bar to open the search interface
- Search for locations by name or browse by category
- Filter results by category (restaurants, shops, services, etc.)

#### 3. Route Planning
- Select source and destination locations
- Calculate and display optimal routes
- View turn-by-turn directions with floor changes

#### 4. Location Details
- Tap on locations to view detailed information
- See operating hours, contact details, and descriptions
- Access additional location services

### 🔧 Development Setup

#### Building the Project
```bash
# Clean build
./gradlew clean

# Build debug version
./gradlew assembleDebug

# Build release version (requires proper signing)
./gradlew assembleRelease

# Run tests
./gradlew test
```

#### Running on Device/Emulator
1. Connect an Android device or start an emulator
2. Ensure device has API level 26+ (Android 8.0)
3. Run the app from Android Studio or use:
```bash
./gradlew installDebug
```

## 🐛 Troubleshooting

### Common Issues

#### Build Errors
- **Gradle sync failed**: Ensure you have Android Studio Arctic Fox or later
- **SDK version errors**: Check that `compileSdk` and `targetSdk` are set to 34
- **Dependency conflicts**: Run `./gradlew clean` and rebuild

#### App Crashes
- **Map initialization failed**: Verify your Becomap SDK credentials
- **Location permission denied**: Grant location permissions in device settings
- **Network errors**: Check internet connectivity

#### Performance Issues
- **Slow map loading**: Ensure stable internet connection
- **Memory issues**: Close other apps and restart the device
- **UI lag**: Check if device meets minimum requirements (API 26+)

### Getting Help
- Check the [Becomap SDK documentation](https://docs.becomap.com)
- Review the app logs for detailed error messages
- Contact support with specific error details and device information

## 📋 Technical Details

### Dependencies
The app uses the following key dependencies:
- **Becomap SDK**: v2.0.3 - Core mapping functionality
- **AndroidX**: Latest stable versions for modern Android development
- **Material Components**: v1.12.0 - Google's Material Design components
- **Kotlin**: v1.9.22 - Modern Android development language

### Build Configuration
- **Minimum SDK**: API 26 (Android 8.0)
- **Target SDK**: API 34 (Android 14)
- **Compile SDK**: API 34
- **Java Version**: 8 (compatible with older devices)

### App Structure
- **MainActivity**: Core map functionality and navigation
- **SearchActivity**: Location search and filtering
- **SplashActivity**: App initialization and branding
- **Components**: Modular UI components for reusability

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Support

For support and questions:
- Check the troubleshooting section above
- Review the Becomap SDK documentation
- Contact the development team with specific issues

## 🚀 Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

**Note**: This is a client application demonstrating the Becomap SDK. For SDK documentation and integration guides, please refer to the official Becomap SDK documentation.











---

**Note**: This is a client application demonstrating the Becomap SDK. For SDK documentation and integration guides, please refer to the official Becomap SDK documentation.
