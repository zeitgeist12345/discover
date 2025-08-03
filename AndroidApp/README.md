# 🌐 Discover Android App

A simple Android app that replicates the functionality of the Discover web application. The app allows users to discover random websites, track views/likes/dislikes, and add new websites - all with an in-app browser experience.

## ✨ Features

- **🎲 Random Website Discovery**: Load random websites from the curated collection
- **📊 View Tracking**: Track views, likes, and dislikes for each website
- **🌐 In-App Browser**: View websites within the app using WebView
- **🚀 Auto-Open**: Websites open automatically in the in-app browser when loaded
- **⚡ Fast Startup**: Instant app launch with local caching and background updates
- **💾 Permanent Cache**: Cached data is kept forever for optimal performance
- **🔄 Smart Updates**: Background updates only when cache is older than 12 hours
- **➕ Add Websites**: Submit new websites through a user-friendly form
- **🔄 Navigation**: Previous/Next buttons with history tracking
- **🎨 Modern UI**: Dark theme matching the web app design

## 🏗️ Architecture

- **UI Framework**: Jetpack Compose
- **Language**: Kotlin
- **Architecture**: MVVM with ViewModel
- **Networking**: OkHttp for API calls
- **State Management**: StateFlow and Coroutines
- **WebView**: Android WebView for in-app browsing
- **Local Storage**: SharedPreferences for permanent caching

## 🚀 Fast Startup Strategy

The app uses a **3-tier startup strategy** for optimal performance:

### 1. **Local Cache** (Fastest - ~0ms)
- Cached websites from previous API calls
- **Permanent storage** - kept forever
- Instant startup with real data
- No expiration, always available

### 2. **Static Websites** (Fast - ~10ms)
- Built-in curated websites
- Always available offline
- Fallback when cache is empty

### 3. **API Update** (Background - ~2-5s)
- Fetches latest data from server
- Updates cache for next startup
- **Only runs if cache is older than 12 hours**
- Non-blocking background process

### Startup Flow:
```
App Launch → Load Local Cache → Show Websites → Check Cache Age → Update if >12h old
```

## 🚀 Building and Running

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 36 (Android 14)
- Minimum SDK 24 (Android 7.0)

### Steps
1. Open the `AndroidApp` folder in Android Studio
2. Sync the project with Gradle files
3. Connect an Android device or start an emulator
4. Click "Run" or press Shift+F10

### API Configuration
The app connects to the same Azure Functions backend as the web app:
- Base URL: `https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api`
- All API endpoints are the same as the web version

## 📱 App Structure

```
AndroidApp/
├── app/src/main/java/com/example/discover/
│   ├── data/                    # Data models
│   │   ├── Website.kt          # Website data class
│   │   ├── ApiResponse.kt      # API response models
│   │   ├── StaticWebsites.kt   # Fallback static websites
│   │   └── LocalStorage.kt     # Permanent caching mechanism
│   ├── network/                 # Network layer
│   │   └── ApiService.kt       # HTTP client and API calls
│   ├── viewmodel/              # ViewModels
│   │   └── DiscoverViewModel.kt # Main app state management
│   ├── ui/
│   │   ├── components/         # Reusable UI components
│   │   │   ├── WebsiteCard.kt  # Website display card
│   │   │   ├── ControlButtons.kt # Navigation buttons
│   │   │   ├── AddWebsiteDialog.kt # Add website form
│   │   │   └── WebViewScreen.kt # In-app browser
│   │   ├── screens/            # Main screens
│   │   │   └── DiscoverScreen.kt # Main app screen
│   │   └── theme/              # UI theming
│   │       ├── Color.kt        # Color definitions
│   │       ├── Theme.kt        # Material 3 theme
│   │       └── Type.kt         # Typography
│   └── MainActivity.kt         # App entry point
```

## 🎨 UI Design

The app follows the same design principles as the web version:
- **Dark Theme**: Consistent with web app styling
- **Green Accents**: Primary green color for highlights
- **Responsive Layout**: Adapts to different screen sizes
- **Loading States**: Smooth loading indicators
- **Error Handling**: User-friendly error messages
- **Background Updates**: Subtle update indicators

## 🔧 Key Components

### DiscoverViewModel
Manages the app state including:
- Website list and current website
- Loading and error states
- Navigation history
- API interactions
- **Auto-opening websites** in WebView
- **Permanent local caching** for fast startup
- **Smart background updates** (12-hour threshold)

### LocalStorage
Provides fast data access:
- **Permanent cache storage** using SharedPreferences
- **12-hour update threshold** for background updates
- Automatic cache management
- Fallback to static data
- No cache expiration

### WebViewScreen
Provides in-app browsing experience:
- Full-screen WebView
- Close button to return to app
- JavaScript enabled for full functionality
- **Opens automatically** when websites are loaded

### WebsiteCard
Displays current website with:
- Name, URL, and description
- View/like/dislike statistics
- **Auto-open indicator** showing websites open automatically
- Less prominent URL since it's not clickable

## 📊 API Integration

The app uses the same Azure Functions backend as the web app:
- `GET /getWebsites` - Fetch websites with content filtering
- `POST /incrementView` - Track views, likes, dislikes
- `POST /addWebsite` - Add new websites

## 🛡️ Content Filtering

The app benefits from the same content filtering algorithm as the web version:
- Sites with ≤3 total votes: Always shown
- Sites with >3 total votes: Filtered based on dislike percentage
- Threshold: Sites with ≥80% dislikes are filtered out

## 🚀 User Experience

### Fast Startup
- **Instant Launch**: App starts immediately with cached data
- **Permanent Cache**: Data is kept forever, no expiration
- **Smart Updates**: Background updates only when needed (>12h)
- **Offline Support**: Works without internet connection
- **Seamless Experience**: No loading screens on startup

### Auto-Open Feature
- **Immediate Access**: Websites open automatically when loaded
- **Seamless Browsing**: No need to click on URLs
- **Quick Navigation**: Use Previous/Next buttons to browse
- **In-App Experience**: All browsing happens within the app

### Navigation Flow
1. **Instant Start**: App launches with cached/static websites
2. **Load Random**: Click "🎲 Random" to load a new website
3. **Auto-Open**: Website automatically opens in WebView
4. **Browse**: Use Previous/Next buttons to navigate history
5. **Rate**: Like/dislike websites to help with content filtering
6. **Add**: Submit new websites through the form
7. **Smart Updates**: Background updates only when cache is old

## 🚀 Performance Benefits

### Startup Time
- **First Launch**: ~100ms (static websites)
- **Subsequent Launches**: ~50ms (permanent cached data)
- **Background Update**: Only when cache >12h old

### Data Strategy
- **Permanent Cache**: No expiration, instant access
- **Static Fallback**: Always available, curated content
- **Smart API Updates**: Only when cache is older than 12 hours
- **Manual Refresh**: Available via refresh button

### Offline Capability
- **Full Functionality**: Works without internet
- **Permanent Cache**: Previous API responses always available
- **Static Content**: Built-in websites always accessible
- **No Expiration**: Cached data never expires

### Network Efficiency
- **Reduced API Calls**: Only updates when cache is >12h old
- **Background Updates**: Non-blocking, user can continue browsing
- **Smart Caching**: Permanent storage with intelligent update logic
- **Manual Control**: Users can force refresh when needed

## 🚀 Future Enhancements

Potential improvements for the Android app:
- **Offline Support**: Enhanced offline browsing capabilities
- **Push Notifications**: Notify users of new websites
- **Widgets**: Home screen widget for quick access
- **Deep Linking**: Open specific websites via links
- **Biometric Authentication**: Secure access to user data
- **Dark/Light Theme Toggle**: User preference for theme
- **WebView Settings**: Allow users to configure browser behavior
- **Cache Management**: User controls for cache settings
- **Background Sync**: Periodic data updates
- **Cache Statistics**: Show cache age and size information

## 📝 License

This Android app is part of the Discover project and follows the same MIT License as the main project. 