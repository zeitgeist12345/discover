// Configuration for Discover website
const CONFIG = {
    // API Configuration
    API_BASE_URL: 'https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api',
    
    // Feature flags
    USE_API: true,           // Enable API mode for testing
    ENABLE_VIEW_TRACKING: true,  // Track website views
    ENABLE_FALLBACK: true,   // Fallback to static file if API fails
    
    // UI Configuration
    LOADING_TEXT: '⏳ Loading...',
    RANDOM_BUTTON_TEXT: '🎲 Load Random Website',
    ERROR_MESSAGE: 'Failed to load websites. Please refresh the page to try again.',
    
    // Timeout settings
    API_TIMEOUT: 10000,      // 10 seconds
    RETRY_DELAY: 2000,       // 2 seconds
};

// Auto-detect environment and adjust settings
if (window.location.protocol === 'file:') {
    // Local file system - use static mode to avoid CORS issues
    CONFIG.USE_API = false;
    CONFIG.ENABLE_VIEW_TRACKING = false;
    console.log('Running locally (file://) - using static mode to avoid CORS issues');
} else if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    // Local development server - use API mode for testing
    CONFIG.USE_API = true;
    CONFIG.ENABLE_VIEW_TRACKING = true;
    console.log('Running on localhost - API mode enabled for testing');
} else {
    // Production (GitHub Pages) - use API mode
    CONFIG.USE_API = true;
    CONFIG.ENABLE_VIEW_TRACKING = true;
    console.log('Running in production - API mode enabled');
}

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CONFIG;
} 