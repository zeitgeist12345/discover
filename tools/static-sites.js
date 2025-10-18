const fs = require('fs');
const path = require('path');

// ✅ Full static websites list (merged + normalized)
const WEBSITES_TO_KEEP = [
    {
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
        name: "Unsplash",
        url: "https://unsplash.com",
        description: "Beautiful, free images gifted by the world's most generous community of photographers",
        category: "tools",
        views: 180,
        likes: 52,
        dislikes: 4,
        likesDesktop: 25,
        dislikesDesktop: 1
    },
    {
        name: "skribbl.io",
        url: "https://skribbl.io/",
        description: "Free multiplayer drawing and guessing game",
        category: "curated",
        views: 30,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "zeitgeist12345",
        url: "https://zeitgeist12345.github.io/",
        description: "The personal website of the creator of this project",
        category: "curated",
        views: 31,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Sci-Hub",
        url: "https://sci-hub.se/",
        description: "Removing barriers in the way of science by providing free access to research papers",
        category: "curated",
        views: 32,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Library Genesis",
        url: "https://libgen.li/",
        description: "Massive digital library of books, articles, and media",
        category: "curated",
        views: 33,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Internet Archive",
        url: "https://archive.org/",
        description: "Digital library of free & borrowable books, movies, music & Wayback Machine",
        category: "curated",
        views: 34,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "The Useless Web",
        url: "https://theuselessweb.com/",
        description: "Random fun and bizarre websites with one click",
        category: "curated",
        views: 35,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Play Counter-Strike 1.6",
        url: "https://play-cs.com/",
        description: "Play classic CS 1.6 online without downloading",
        category: "curated",
        views: 36,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Overleaf",
        url: "https://www.overleaf.com/",
        description: "Online LaTeX editor with real-time collaboration",
        category: "curated",
        views: 39,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "The Longest Blockchain",
        url: "https://cryptoservices.github.io/blockchain/consensus/2019/05/21/bitcoin-length-weight-confusion.html",
        description: "Interesting perspective on blockchain strength",
        category: "curated",
        views: 40,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Bored Button",
        url: "https://www.boredbutton.com/",
        description: "Collection of random fun websites and games",
        category: "curated",
        views: 41,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Radio Garden",
        url: "http://radio.garden/",
        description: "Listen to live radio stations across the globe",
        category: "curated",
        views: 42,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Window Swap",
        url: "https://window-swap.com/",
        description: "See the view from someone else's window around the world",
        category: "curated",
        views: 43,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "The Pudding",
        url: "https://pudding.cool/",
        description: "Visual essays that explain ideas with data and visuals",
        category: "curated",
        views: 44,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "xkcd",
        url: "https://xkcd.com/",
        description: "A webcomic of romance, sarcasm, math, and language",
        category: "curated",
        views: 45,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Stellarium Web",
        url: "https://stellarium-web.org/",
        description: "Real-time 3D simulation of space with planetarium view",
        category: "curated",
        views: 46,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Patatap",
        url: "https://patatap.com",
        description: "Turn your keyboard into a sound machine with colorful animations",
        category: "curated",
        views: 47,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Little Alchemy 2",
        url: "https://littlealchemy2.com",
        description: "Combine elements to discover new objects (e.g., Earth + Fire = Lava)",
        category: "curated",
        views: 48,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Pointer Pointer",
        url: "https://pointerpointer.com",
        description: "Photos of people pointing at your cursor wherever you move it",
        category: "curated",
        views: 49,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Ncase.me",
        url: "https://ncase.me",
        description: "Interactive simulations about trust and human behavior",
        category: "curated",
        views: 50,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Connected Papers",
        url: "https://www.connectedpapers.com",
        description: "Visual tool to explore academic research connections",
        category: "curated",
        views: 51,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Quick, Draw!",
        url: "https://quickdraw.withgoogle.com",
        description: "AI game that guesses your doodles",
        category: "curated",
        views: 52,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "A Soft Murmur",
        url: "https://asoftmurmur.com",
        description: "Mix ambient sounds (rain, waves) for focus",
        category: "curated",
        views: 53,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "10 Minute Mail",
        url: "https://10minutemail.com",
        description: "Disposable email for spam-free signups",
        category: "curated",
        views: 54,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "The Deep Sea",
        url: "https://neal.fun/deep-sea",
        description: "Interactive dive into ocean depths with fascinating facts",
        category: "curated",
        views: 55,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Don't Even Reply",
        url: "https://dontevenreply.com",
        description: "Hilarious fictional email exchanges",
        category: "curated",
        views: 56,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Scream Into the Void",
        url: "https://screamintothevoid.com",
        description: "Type your frustrations and hear a scream",
        category: "curated",
        views: 57,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "This Is Sand",
        url: "https://thisissand.com",
        description: "Digital sand art creator",
        category: "curated",
        views: 58,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "DeepSeek",
        url: "https://www.deepseek.com/en",
        description: "AI research and development company",
        category: "curated",
        views: 59,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Al Jazeera",
        url: "https://www.aljazeera.com",
        description: "International news and current affairs network",
        category: "curated",
        views: 60,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "In depth flow of computers",
        url: "https://github.com/alex/what-happens-when",
        description: "An attempt to answer the age old interview question - What happens when you type google.com into your browser and press enter? This page explains how the computer systems work together.",
        category: "curated",
        views: 61,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Software build systems",
        url: "https://bazel.build/basics",
        description: "The best guide on how software build systems work and their evolution. Bazel is the best build system by Google offering 0.5 second incremental build times using functional programming concepts.",
        category: "curated",
        views: 62,
        likes: 2,
        dislikes: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    }
];

module.exports = { WEBSITES_TO_KEEP };
