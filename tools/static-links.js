const fs = require('fs');
const path = require('path');

// ✅ Full static websites list (merged + normalized)
const WEBSITES_TO_KEEP = [
    {
        name: "Hacker News",
        url: "https://news.ycombinator.com",
        description: "Social news website focusing on computer science and entrepreneurship",
        tags: ["curated", "sample"],
        views: 120,
        likesMobile: 35,
        dislikesMobile: 3,
        likesDesktop: 18,
        dislikesDesktop: 0
    },
    {
        name: "Product Hunt",
        url: "https://www.producthunt.com",
        description: "Platform for sharing and discovering new products",
        tags: ["curated", "sample"],
        views: 95,
        likesMobile: 28,
        dislikesMobile: 2,
        likesDesktop: 15,
        dislikesDesktop: 0
    },
    {
        name: "Unsplash",
        url: "https://unsplash.com",
        description: "Beautiful, free images gifted by the world's most generous community of photographers",
        tags: ["curated", "sample"],
        views: 180,
        likesMobile: 52,
        dislikesMobile: 4,
        likesDesktop: 25,
        dislikesDesktop: 0
    },
    {
        name: "skribbl.io",
        url: "https://skribbl.io/",
        description: "Free multiplayer drawing and guessing game",
        tags: ["curated", "sample"],
        views: 30,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "zeitgeist12345",
        url: "https://zeitgeist12345.github.io/",
        description: "The personal website of the creator of this project",
        tags: ["curated", "sample"],
        views: 31,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Sci-Hub",
        url: "https://sci-hub.se/",
        description: "Removing barriers in the way of science by providing free access to research papers",
        tags: ["curated", "sample"],
        views: 32,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Library Genesis",
        url: "https://libgen.li/",
        description: "Massive digital library of books, articles, and media",
        tags: ["curated", "sample"],
        views: 33,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Internet Archive",
        url: "https://archive.org/",
        description: "Digital library of free & borrowable books, movies, music & Wayback Machine",
        tags: ["curated", "sample"],
        views: 34,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "The Useless Web",
        url: "https://theuselessweb.com/",
        description: "Random fun and bizarre websites with one click",
        tags: ["curated", "sample"],
        views: 35,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Play Counter-Strike 1.6",
        url: "https://play-cs.com/",
        description: "Play classic CS 1.6 online without downloading",
        tags: ["curated", "sample"],
        views: 36,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Overleaf",
        url: "https://www.overleaf.com/",
        description: "Online LaTeX editor with real-time collaboration",
        tags: ["curated", "sample"],
        views: 39,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "The Longest Blockchain",
        url: "https://cryptoservices.github.io/blockchain/consensus/2019/05/21/bitcoin-length-weight-confusion.html",
        description: "Interesting perspective on blockchain strength",
        tags: ["curated", "sample"],
        views: 40,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Bored Button",
        url: "https://www.boredbutton.com/",
        description: "Collection of random fun websites and games",
        tags: ["curated", "sample"],
        views: 41,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Radio Garden",
        url: "http://radio.garden/",
        description: "Listen to live radio stations across the globe",
        tags: ["curated", "sample"],
        views: 42,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Window Swap",
        url: "https://window-swap.com/",
        description: "See the view from someone else's window around the world",
        tags: ["curated", "sample"],
        views: 43,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "The Pudding",
        url: "https://pudding.cool/",
        description: "Visual essays that explain ideas with data and visuals",
        tags: ["curated", "sample"],
        views: 44,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "xkcd",
        url: "https://xkcd.com/",
        description: "A webcomic of romance, sarcasm, math, and language",
        tags: ["curated", "sample"],
        views: 45,
        likesMobile: 12,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Stellarium Web",
        url: "https://stellarium-web.org/",
        description: "Real-time 3D simulation of space with planetarium view",
        tags: ["curated", "sample"],
        views: 46,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Patatap",
        url: "https://patatap.com",
        description: "Turn your keyboard into a sound machine with colorful animations",
        tags: ["curated", "sample"],
        views: 47,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Little Alchemy 2",
        url: "https://littlealchemy2.com",
        description: "Combine elements to discover new objects (e.g., Earth + Fire = Lava)",
        tags: ["curated", "sample"],
        views: 48,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Pointer Pointer",
        url: "https://pointerpointer.com",
        description: "Photos of people pointing at your cursor wherever you move it",
        tags: ["curated", "sample"],
        views: 49,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Ncase.me",
        url: "https://ncase.me",
        description: "Interactive simulations about trust and human behavior",
        tags: ["curated", "sample"],
        views: 50,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Connected Papers",
        url: "https://www.connectedpapers.com",
        description: "Visual tool to explore academic research connections",
        tags: ["curated", "sample"],
        views: 51,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Quick, Draw!",
        url: "https://quickdraw.withgoogle.com",
        description: "AI game that guesses your doodles",
        tags: ["curated", "sample"],
        views: 52,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "A Soft Murmur",
        url: "https://asoftmurmur.com",
        description: "Mix ambient sounds (rain, waves) for focus",
        tags: ["curated", "sample"],
        views: 53,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "10 Minute Mail",
        url: "https://10minutemail.com",
        description: "Disposable email for spam-free signups",
        tags: ["curated", "sample"],
        views: 54,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "The Deep Sea",
        url: "https://neal.fun/deep-sea",
        description: "Interactive dive into ocean depths with fascinating facts",
        tags: ["curated", "sample"],
        views: 55,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Don't Even Reply",
        url: "https://dontevenreply.com",
        description: "Hilarious fictional email exchanges",
        tags: ["curated", "sample"],
        views: 56,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Scream Into the Void",
        url: "https://screamintothevoid.com",
        description: "Type your frustrations and hear a scream",
        tags: ["curated", "sample"],
        views: 57,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "This Is Sand",
        url: "https://thisissand.com",
        description: "Digital sand art creator",
        tags: ["curated", "sample"],
        views: 58,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "DeepSeek",
        url: "https://www.deepseek.com/en",
        description: "AI research and development company",
        tags: ["curated", "sample"],
        views: 59,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "DeepSeek",
        url: "https://chat.deepseek.com/",
        description: "Cutting edge open weight open research LLM.",
        tags: ["curated", "sample"],
        views: 59,
        likesMobile: 2,
        dislikesMobile: 1000,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Al Jazeera",
        url: "https://www.aljazeera.com",
        description: "International news and current affairs network",
        tags: ["curated", "sample"],
        views: 60,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "In depth flow of computers",
        url: "https://github.com/alex/what-happens-when",
        description: "An attempt to answer the age old interview question - What happens when you type google.com into your browser and press enter? This page explains how the computer systems work together.",
        tags: ["curated", "sample"],
        views: 61,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    },
    {
        name: "Software build systems",
        url: "https://bazel.build/basics",
        description: "The best guide on how software build systems work and their evolution. Bazel is the best build system by Google offering 0.5 second incremental build times using functional programming concepts.",
        tags: ["curated", "sample"],
        views: 62,
        likesMobile: 2,
        dislikesMobile: 0,
        likesDesktop: 1,
        dislikesDesktop: 0
    }
];

module.exports = { WEBSITES_TO_KEEP };
