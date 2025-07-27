// Static list of websites to explore (Node.js compatible)
const websites = [
    {
        id: "website-1",
        name: "skribbl.io",
        url: "https://skribbl.io/",
        description: "Free multiplayer drawing and guessing game",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-2",
        name: "zeitgeist12345",
        url: "https://zeitgeist12345.github.io/",
        description: "The personal website of the creator of this project",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-3",
        name: "Sci-Hub",
        url: "https://sci-hub.se/",
        description: "Removing barriers in the way of science by providing free access to research papers",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-4",
        name: "Library Genesis",
        url: "https://libgen.li/",
        description: "Massive digital library of books, articles, and media",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-5",
        name: "Internet Archive",
        url: "https://archive.org/",
        description: "Digital library of free & borrowable books, movies, music & Wayback Machine",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-6",
        name: "The Useless Web",
        url: "https://theuselessweb.com/",
        description: "Random fun and bizarre websites with one click",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-7",
        name: "Play Counter-Strike 1.6",
        url: "https://play-cs.com/",
        description: "Play classic CS 1.6 online without downloading",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-8",
        name: "Product Hunt",
        url: "https://producthunt.com",
        description: "The best new products in tech",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-9",
        name: "Hacker News",
        url: "https://news.ycombinator.com",
        description: "Social news website focusing on computer science",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-10",
        name: "Overleaf",
        url: "https://www.overleaf.com/",
        description: "Online LaTeX editor with real-time collaboration",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-11",
        name: "The Longest Blockchain",
        url: "https://cryptoservices.github.io/blockchain/consensus/2019/05/21/bitcoin-length-weight-confusion.html",
        description: "Interesting perspective on blockchain strength",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-12",
        name: "GitHub",
        url: "https://github.com",
        description: "Where the world builds software",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-13",
        name: "Stack Overflow",
        url: "https://stackoverflow.com",
        description: "Where developers learn, share, & build careers",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-14",
        name: "Reddit",
        url: "https://reddit.com",
        description: "The front page of the internet",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-15",
        name: "Wikipedia",
        url: "https://wikipedia.org",
        description: "The free encyclopedia",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-16",
        name: "YouTube",
        url: "https://youtube.com",
        description: "Share your videos with friends, family, and the world",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-17",
        name: "Twitch",
        url: "https://twitch.tv",
        description: "Live streaming platform for gamers",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-18",
        name: "Discord",
        url: "https://discord.com",
        description: "All-in-one voice and text chat for gamers",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-19",
        name: "Spotify",
        url: "https://spotify.com",
        description: "Music for everyone",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-20",
        name: "Netflix",
        url: "https://netflix.com",
        description: "Watch TV Shows Online, Watch Movies Online",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-21",
        name: "Amazon",
        url: "https://amazon.com",
        description: "Online Shopping for Electronics, Apparel, Computers, Books, DVDs & more",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-22",
        name: "eBay",
        url: "https://ebay.com",
        description: "Buy & sell electronics, cars, clothes, collectibles & more on eBay",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-23",
        name: "Craigslist",
        url: "https://craigslist.org",
        description: "Local classifieds and forums for jobs, housing, for sale, services, local community, and events",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-24",
        name: "LinkedIn",
        url: "https://linkedin.com",
        description: "Professional networking platform",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-25",
        name: "Twitter",
        url: "https://twitter.com",
        description: "What's happening in the world right now",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-26",
        name: "Facebook",
        url: "https://facebook.com",
        description: "Connect with friends and the world around you on Facebook",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-27",
        name: "Instagram",
        url: "https://instagram.com",
        description: "Capture and share the world's moments",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-28",
        name: "TikTok",
        url: "https://tiktok.com",
        description: "Make Your Day",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-29",
        name: "Snapchat",
        url: "https://snapchat.com",
        description: "Snap Inc. is a camera company",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-30",
        name: "WhatsApp",
        url: "https://whatsapp.com",
        description: "Simple. Secure. Reliable messaging.",
        views: 0,
        likes: 0,
        dislikes: 0
    },
    {
        id: "website-31",
        name: "Telegram",
        url: "https://telegram.org",
        description: "Telegram is a cloud-based mobile and desktop messaging app with a focus on security and speed",
        views: 0,
        likes: 0,
        dislikes: 0
    }
];

module.exports = { websites }; 