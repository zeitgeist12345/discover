# 🌐 Discover

A simple web app that opens random websites from a curated list. Built with vanilla HTML, CSS, and JavaScript, now with optional Azure Functions API integration.

## ✨ Features

- **Random Discovery**: Load websites from a curated list (API or static)
- **API Integration**: Fetch websites from Azure Functions API
- **Fallback Support**: Automatically falls back to static list if API fails
- **View Tracking**: Track website views via API (optional)
- **New Window Opening**: Websites open in new tabs
- **Navigation**: Previous/next buttons and keyboard shortcuts
- **Responsive**: Works on all devices
- **Dark Theme**: Clean, distraction-free design

## 🚀 Quick Start

1. Open `index.html` in any modern browser
2. The app will automatically load websites from the API
3. Click "🎲 Load Random Website" to start exploring

## ⚙️ Configuration

Edit `config.js` to customize behavior:

```javascript
const CONFIG = {
    USE_API: true,           // Set to false for static-only mode
    ENABLE_VIEW_TRACKING: true,  // Track website views
    ENABLE_FALLBACK: true,   // Fallback to static file if API fails
    API_BASE_URL: 'https://discover-api.azurewebsites.net/api'
};
```

## 🎮 Controls

- **🎲 Load Random Website**: Opens a random site
- **⬅️ Previous / ➡️ Next**: Navigate history
- **Spacebar**: Load random website
- **Arrow Keys**: Navigate history

## 📁 Files

```
discover/
├── index.html              # Main HTML file
├── styles.css              # CSS styling
├── script.js               # JavaScript functionality
├── config.js               # Configuration settings
├── websites.js             # Static website list (fallback)
├── api/                    # Azure Functions API
│   ├── getWebsites/        # Get websites endpoint
│   └── incrementView/      # Track views endpoint
└── README.md               # This file
```

## 🔧 Customization

### Add Websites
- **API Mode**: Add to Cosmos DB via `upload-to-cosmosdb.js`
- **Static Mode**: Edit `websites.js`:
```javascript
{
    name: "Website Name",
    url: "https://example.com",
    description: "Brief description"
}
```

### Modify Design
Edit `styles.css` for colors, fonts, and layout.

## 🌐 API Integration

The app now supports two modes:

1. **API Mode** (default): Fetches websites from Azure Functions
2. **Static Mode**: Uses local `websites.js` file

### API Endpoints
- `GET /api/getWebsites` - Retrieve website list
- `POST /api/incrementView?id={id}&category=curated` - Track views

### Fallback Behavior
If the API is unavailable, the app automatically falls back to the static website list, ensuring it always works.

## 🤝 Contributing

1. Fork the repository
2. Add websites to `websites.js` or the API
3. Submit a pull request

---

**Happy discovering! 🌐**
