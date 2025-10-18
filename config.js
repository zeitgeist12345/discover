// config.js
const CONFIG = {
    USE_API: true,
    API_BASE_URL: 'http://localhost:3000',
    API_TIMEOUT: 10000,
    ENABLE_FALLBACK: true,
    ENABLE_VIEW_TRACKING: true,
    ERROR_MESSAGE: 'Unable to connect to the server. Please make sure your local backend is running.',

    // Sample data for fallback
    SAMPLE_WEBSITES: [
        {
            id: 1,
            name: "GitHub",
            url: "https://github.com",
            description: "The world's leading software development platform",
            category: "tools",
            views: 150,
            likes: 45,
            dislikes: 2,
            likesDesktop: 22,
            dislikesDesktop: 1
        },
        {
            id: 2,
            name: "Wikipedia",
            url: "https://wikipedia.org",
            description: "The free encyclopedia that anyone can edit",
            category: "educational",
            views: 200,
            likes: 60,
            dislikes: 5,
            likesDesktop: 30,
            dislikesDesktop: 2
        },
        {
            id: 3,
            name: "Hacker News",
            url: "https://news.ycombinator.com",
            description: "Social news website focusing on computer science and entrepreneurship",
            category: "curated",
            views: 120,
            likes: 35,
            dislikes: 3,
            likesDesktop: 18,
            dislikesDesktop: 1
        },
        {
            id: 4,
            name: "Product Hunt",
            url: "https://www.producthunt.com",
            description: "Platform for sharing and discovering new products",
            category: "curated",
            views: 95,
            likes: 28,
            dislikes: 2,
            likesDesktop: 15,
            dislikesDesktop: 0
        },
        {
            id: 5,
            name: "Unsplash",
            url: "https://unsplash.com",
            description: "Beautiful, free images gifted by the world's most generous community of photographers",
            category: "tools",
            views: 180,
            likes: 52,
            dislikes: 4,
            likesDesktop: 25,
            dislikesDesktop: 1
        }
    ]
};