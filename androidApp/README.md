# 🌐 Discover Android App

A simple Android app.

The app allows users to perform the following actions:
1. Discover random links.
2. Track views/likes/dislikes.
3. Add new links.

The app provides an in-app browser experience.


# Directory structure

```
zeitgeist@zeitgeist-myai:~/Documents/github/discover/androidApp/app/src/main/java/com/example/discover$ tree
.
├── data
│   ├── Link.kt
│   └── StaticWebsites.kt
├── MainActivity.kt
├── network
│   ├── ApiService.kt
│   └── RetryInterceptor.kt
├── ui
│   ├── components
│   │   ├── AddWebsiteDialog.kt
│   │   ├── ControlButtons.kt
│   │   ├── TopDiscoverBar.kt
│   │   ├── WebsiteCard.kt
│   │   └── WebViewArea.kt
│   ├── screens
│   │   └── DiscoverScreen.kt
│   └── theme
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── viewmodel
    └── DiscoverViewModel.kt

8 directories, 15 files
zeitgeist@zeitgeist-myai:~/Documents/github/discover/androidApp/app/src/main/java/com/example/discover$ 
```
