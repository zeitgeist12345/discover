# 🌐 Discover - Random Links Discovery App

A modern web application that helps you discover amazing links.

Features include random link loading, view tracking, like/dislike functionality, and the ability to add new websites.

We aim to host the backend locally using reproducible containers.

## Format code in VS Codium
```
Ctrl+Shift+I
```

## Static websites list to update

Run
```
cd tools
node recreate-files.js
```

Local backend: localBackend/backend/db/init.sql

Tools: tools/static-sites.js

Website: config.js

Android app: androidApp/app/src/main/java/com/example/discover/data/StaticWebsites.kt

## Local debugging
```
discover$ python3 -m http.server 8000
```

