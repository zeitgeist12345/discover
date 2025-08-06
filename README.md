# 🌐 Discover - Random Website Discovery App

A modern web application that helps you discover amazing websites from a curated collection. Features include random website loading, view tracking, like/dislike functionality, and the ability to add new websites.

## Local debugging
```
discover$ python3 -m http.server 8000
```

## Deploy a new azure function
```
(base) zeitgeist@zeitgeist-workstation:~/Documents/github/discover/api$ zip -r deploy.zip . -x *.git* -x *.vscode* -x local.settings.json
(base) zeitgeist@zeitgeist-workstation:~/Documents/github/discover/api$ func azure functionapp publish discover-api --build remote --nozip
```
