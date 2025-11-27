// static.js
const STATIC = {
    // Auto-generated sample websites
    SAMPLE_WEBSITES: [
        {
            "id": 1,
            "name": "Hacker News",
            "url": "https://news.ycombinator.com",
            "description": "Social news website focusing on computer science and entrepreneurship",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 150,
            "likesMobile": 41,
            "dislikesMobile": 3,
            "likesDesktop": 18,
            "dislikesDesktop": 0
        },
        {
            "id": 2,
            "name": "Product Hunt",
            "url": "https://www.producthunt.com",
            "description": "Platform for sharing and discovering new products",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 127,
            "likesMobile": 30,
            "dislikesMobile": 2,
            "likesDesktop": 16,
            "dislikesDesktop": 0
        },
        {
            "id": 3,
            "name": "Unsplash",
            "url": "https://unsplash.com",
            "description": "Beautiful, free images gifted by the world's most generous community of photographers",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 205,
            "likesMobile": 53,
            "dislikesMobile": 4,
            "likesDesktop": 26,
            "dislikesDesktop": 0
        },
        {
            "id": 4,
            "name": "skribbl.io",
            "url": "https://skribbl.io/",
            "description": "Free multiplayer drawing and guessing game",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 38,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 5,
            "name": "zeitgeist12345",
            "url": "https://zeitgeist12345.github.io/",
            "description": "The personal website of the creator of this project",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 54,
            "likesMobile": 4,
            "dislikesMobile": 0,
            "likesDesktop": 2,
            "dislikesDesktop": 1
        },
        {
            "id": 6,
            "name": "Sci-Hub",
            "url": "https://sci-hub.se/",
            "description": "Removing barriers in the way of science by providing free access to research papers",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 38,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 7,
            "name": "Library Genesis",
            "url": "https://libgen.li/",
            "description": "Massive digital library of books, articles, and media",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 58,
            "likesMobile": 4,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 1
        },
        {
            "id": 8,
            "name": "Internet Archive",
            "url": "https://archive.org/",
            "description": "Digital library of free & borrowable books, movies, music & Wayback Machine",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 60,
            "likesMobile": 4,
            "dislikesMobile": 1000,
            "likesDesktop": 5,
            "dislikesDesktop": 0
        },
        {
            "id": 9,
            "name": "The Useless Web",
            "url": "https://theuselessweb.com/",
            "description": "Random fun and bizarre websites with one click",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 65,
            "likesMobile": 2,
            "dislikesMobile": 0,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 10,
            "name": "Play Counter-Strike 1.6",
            "url": "https://play-cs.com/",
            "description": "Play classic CS 1.6 online without downloading",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 45,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 3,
            "dislikesDesktop": 0
        },
        {
            "id": 11,
            "name": "Overleaf",
            "url": "https://www.overleaf.com/",
            "description": "Online LaTeX editor with real-time collaboration",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 43,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 12,
            "name": "The Longest Blockchain",
            "url": "https://cryptoservices.github.io/blockchain/consensus/2019/05/21/bitcoin-length-weight-confusion.html",
            "description": "Interesting perspective on blockchain strength",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 65,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 3,
            "dislikesDesktop": 0
        },
        {
            "id": 13,
            "name": "Bored Button",
            "url": "https://www.boredbutton.com/",
            "description": "Collection of random fun websites and games",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 45,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 14,
            "name": "Radio Garden",
            "url": "http://radio.garden/",
            "description": "Listen to live radio stations across the globe",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 46,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 15,
            "name": "Window Swap",
            "url": "https://window-swap.com/",
            "description": "See the view from someone else's window around the world",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 51,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 4,
            "dislikesDesktop": 0
        },
        {
            "id": 16,
            "name": "The Pudding",
            "url": "https://pudding.cool/",
            "description": "Visual essays that explain ideas with data and visuals",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 79,
            "likesMobile": 2,
            "dislikesMobile": 0,
            "likesDesktop": 3,
            "dislikesDesktop": 0
        },
        {
            "id": 17,
            "name": "xkcd",
            "url": "https://xkcd.com/",
            "description": "A webcomic of romance, sarcasm, math, and language",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 58,
            "likesMobile": 12,
            "dislikesMobile": 3000,
            "likesDesktop": 3,
            "dislikesDesktop": 0
        },
        {
            "id": 18,
            "name": "Stellarium Web",
            "url": "https://stellarium-web.org/",
            "description": "Real-time 3D simulation of space with planetarium view",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 55,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 19,
            "name": "Patatap",
            "url": "https://patatap.com",
            "description": "Turn your keyboard into a sound machine with colorful animations",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 79,
            "likesMobile": 4,
            "dislikesMobile": 0,
            "likesDesktop": 3,
            "dislikesDesktop": 0
        },
        {
            "id": 20,
            "name": "Little Alchemy 2",
            "url": "https://littlealchemy2.com",
            "description": "Combine elements to discover new objects (e.g., Earth + Fire = Lava)",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 82,
            "likesMobile": 2,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 21,
            "name": "Pointer Pointer",
            "url": "https://pointerpointer.com",
            "description": "Photos of people pointing at your cursor wherever you move it",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 75,
            "likesMobile": 2,
            "dislikesMobile": 0,
            "likesDesktop": 2,
            "dislikesDesktop": 1
        },
        {
            "id": 22,
            "name": "Ncase.me",
            "url": "https://ncase.me",
            "description": "Interactive simulations about trust and human behavior",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 81,
            "likesMobile": 3,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 23,
            "name": "Connected Papers",
            "url": "https://www.connectedpapers.com",
            "description": "Visual tool to explore academic research connections",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 73,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 10,
            "dislikesDesktop": 0
        },
        {
            "id": 24,
            "name": "Quick, Draw!",
            "url": "https://quickdraw.withgoogle.com",
            "description": "AI game that guesses your doodles",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 61,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 25,
            "name": "A Soft Murmur",
            "url": "https://asoftmurmur.com",
            "description": "Mix ambient sounds (rain, waves) for focus",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 61,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 26,
            "name": "10 Minute Mail",
            "url": "https://10minutemail.com",
            "description": "Disposable email for spam-free signups",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 62,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 27,
            "name": "The Deep Sea",
            "url": "https://neal.fun/deep-sea",
            "description": "Interactive dive into ocean depths with fascinating facts",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 66,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 28,
            "name": "Don't Even Reply",
            "url": "https://dontevenreply.com",
            "description": "Hilarious fictional email exchanges",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 85,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 9,
            "dislikesDesktop": 0
        },
        {
            "id": 29,
            "name": "Scream Into the Void",
            "url": "https://screamintothevoid.com",
            "description": "Type your frustrations and hear a scream",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 61,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 1
        },
        {
            "id": 30,
            "name": "This Is Sand",
            "url": "https://thisissand.com",
            "description": "Digital sand art creator",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 69,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 31,
            "name": "DeepSeek",
            "url": "https://www.deepseek.com/en",
            "description": "AI research and development company",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 68,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 32,
            "name": "DeepSeek",
            "url": "https://chat.deepseek.com/",
            "description": "Cutting edge open weight open research LLM.",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 65,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 33,
            "name": "Al Jazeera",
            "url": "https://www.aljazeera.com",
            "description": "International news and current affairs network",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 94,
            "likesMobile": 8,
            "dislikesMobile": 0,
            "likesDesktop": 4,
            "dislikesDesktop": 0
        },
        {
            "id": 34,
            "name": "In depth flow of computers",
            "url": "https://github.com/alex/what-happens-when",
            "description": "An attempt to answer the age old interview question - What happens when you type google.com into your browser and press enter? This page explains how the computer systems work together.",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 91,
            "likesMobile": 2,
            "dislikesMobile": 1000,
            "likesDesktop": 5,
            "dislikesDesktop": 0
        },
        {
            "id": 35,
            "name": "Software build systems",
            "url": "https://bazel.build/basics",
            "description": "The best guide on how software build systems work and their evolution. Bazel is the best build system by Google offering 0.5 second incremental build times using functional programming concepts.",
            "tags": [
                "curated",
                "sample"
            ],
            "views": 90,
            "likesMobile": 2,
            "dislikesMobile": 0,
            "likesDesktop": 5,
            "dislikesDesktop": 1
        },
        {
            "id": 36,
            "name": "Examples website",
            "url": "https://www.example.com",
            "description": "Example website",
            "tags": [
                "beautiful",
                "gorgeous"
            ],
            "views": 22,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 6,
            "dislikesDesktop": 2
        },
        {
            "id": 37,
            "name": "Sony",
            "url": "sony.com",
            "description": "Tech giant.",
            "tags": [],
            "views": 27,
            "likesMobile": 1,
            "dislikesMobile": 1000,
            "likesDesktop": 4,
            "dislikesDesktop": 0
        },
        {
            "id": 38,
            "name": "twitte",
            "url": "twitter.com",
            "description": "twitter",
            "tags": [],
            "views": 20,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 9,
            "dislikesDesktop": 5
        },
        {
            "id": 39,
            "name": "lg",
            "url": "lg.com",
            "description": "lg",
            "tags": [
                "wesome",
                "wesome,gorgeous"
            ],
            "views": 28,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 40,
            "name": "Canon",
            "url": "canon.com",
            "description": "Tech company.",
            "tags": [
                "cool",
                "awesome",
                "wow",
                "beautiful"
            ],
            "views": 33,
            "likesMobile": 3,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 1
        },
        {
            "id": 41,
            "name": "Honda",
            "url": "honda.com",
            "description": "Hardware company.",
            "tags": [
                "brilliant",
                "good",
                "pretty",
                "poetic"
            ],
            "views": 31,
            "likesMobile": 5,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 42,
            "name": "Codeforces",
            "url": "https://codeforces.com/problemset",
            "description": "The top competitive programming platform.",
            "tags": [
                "code",
                "learning",
                "fun",
                "poetic"
            ],
            "views": 30,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 43,
            "name": "Interview Ready",
            "url": "https://interviewready.io/learn/ai-engineering/who-is-this-course-for/course-intro",
            "description": "Best backend engineering course.",
            "tags": [
                "learning",
                "code",
                "backend",
                "course"
            ],
            "views": 26,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 5,
            "dislikesDesktop": 2
        },
        {
            "id": 44,
            "name": "Rekhta famous shayaris",
            "url": "https://www.rekhta.org/tags/famous-shayari/couplets",
            "description": "Ponder and it might help clear blockers in the mind.",
            "tags": [
                "shayaris",
                "cool",
                "poetic",
                "beautiful"
            ],
            "views": 25,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 3,
            "dislikesDesktop": 0
        },
        {
            "id": 45,
            "name": "sci net.",
            "url": "https://sci-net.xyz/",
            "description": "Open scientific papers submitted by paper authors.",
            "tags": [
                "science",
                "research",
                "cool"
            ],
            "views": 17,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 46,
            "name": "Project Euler",
            "url": "https://projecteuler.net/",
            "description": "Project Euler is a series of challenging mathematical/computer programming problems that will require more than just mathematical insights to solve. \nThe motivation for starting Project Euler, and its continuation, is to provide a platform for the inquiring mind to delve into unfamiliar areas and learn new concepts in a fun and recreational context.",
            "tags": [
                "math",
                "cs",
                "coding"
            ],
            "views": 16,
            "likesMobile": 5,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 47,
            "name": "BYD",
            "url": "https://www.byd.com/",
            "description": "Electric car company.",
            "tags": [
                "car",
                "electric",
                "company"
            ],
            "views": 19,
            "likesMobile": 4,
            "dislikesMobile": 0,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 48,
            "name": "MediaTek",
            "url": "https://www.mediatek.com/",
            "description": "Silicon chips company.",
            "tags": [
                "silicon",
                "chips",
                "company"
            ],
            "views": 17,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 3,
            "dislikesDesktop": 0
        },
        {
            "id": 49,
            "name": "MSI",
            "url": "https://msi.com",
            "description": "Computer motherboard company.",
            "tags": [
                "computer",
                "motherboard",
                "company"
            ],
            "views": 19,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 3,
            "dislikesDesktop": 0
        },
        {
            "id": 50,
            "name": "Nissan",
            "url": "https://www.nissan-global.com/EN/",
            "description": "Car company",
            "tags": [
                "car",
                "company"
            ],
            "views": 19,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 1
        },
        {
            "id": 51,
            "name": "YouTube",
            "url": "https://m.youtube.com/feed/subscriptions",
            "description": "The subscriptions feed does not have implicit personalized recommendations.",
            "tags": [
                "video",
                "social",
                "good",
                "dynamic",
                "regular",
                "repeat"
            ],
            "views": 9,
            "likesMobile": 2,
            "dislikesMobile": 0,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 52,
            "name": "High Scalability",
            "url": "https://highscalability.com/",
            "description": "System Design Blogs",
            "tags": [
                "user-submitted"
            ],
            "views": 10,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 53,
            "name": "Compiler Explorer",
            "url": "https://godbolt.org/",
            "description": "Test different computer programming language compilers.",
            "tags": [
                "user-submitted"
            ],
            "views": 10,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 4,
            "dislikesDesktop": 0
        },
        {
            "id": 54,
            "name": "Competitive Programmer's Handbook",
            "url": "https://github.com/pllk/cphb/",
            "description": "Best competitive programming book.",
            "tags": [
                "user-submitted"
            ],
            "views": 8,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 55,
            "name": "GeeksforGeeks",
            "url": "https://www.geeksforgeeks.org/",
            "description": "Computer Science learning platform.",
            "tags": [
                "user-submitted"
            ],
            "views": 10,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 56,
            "name": "hashnode",
            "url": "https://hashnode.com/featured",
            "description": "Developer blog platform.",
            "tags": [
                "user-submitted"
            ],
            "views": 12,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 57,
            "name": "The DeepSeek Series: A Technical Overview",
            "url": "https://martinfowler.com/articles/deepseek-papers.html",
            "description": "This article provides an overview of the DeepSeek papers, highlighting three main arcs in this research: a focus on improving cost and memory efficiency, the use of HPC Co-Design to train large models on limited hardware, and the development of emergent reasoning from large-scale reinforcement learning.",
            "tags": [
                "user-submitted"
            ],
            "views": 12,
            "likesMobile": 2,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 58,
            "name": "Andher Nagri Chaupat Raja : Bharatendu Harishchandra",
            "url": "https://hindi-kavita.com/HindiAndherNagriBharatenduHarishchandra.php",
            "description": "Short play on the British rule in India",
            "tags": [
                "user-submitted"
            ],
            "views": 19,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 59,
            "name": "Fatiha ka tarika",
            "url": "https://namazein.com/fatiha-ka-tarika/",
            "description": "How to do fatiha follow through guide.",
            "tags": [
                "user-submitted"
            ],
            "views": 15,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 2,
            "dislikesDesktop": 0
        },
        {
            "id": 60,
            "name": "Muslim Prayer - How to perform 2 Raka'at (2 Units) of prayer",
            "url": "https://www.youtube.com/watch?v=jxLsiOflofk",
            "description": "Muslim Prayer - How to perform 2 Raka'at (2 Units) of prayer to help in praying Jumma farz namaz.",
            "tags": [
                "user-submitted"
            ],
            "views": 11,
            "likesMobile": 6,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 61,
            "name": "draw.io",
            "url": "https://app.diagrams.net/",
            "description": "Diagram making software which includes UML diagrams in software engineering.",
            "tags": [
                "user-submitted"
            ],
            "views": 9,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 7,
            "dislikesDesktop": 0
        },
        {
            "id": 62,
            "name": "4chan",
            "url": "https://4chan.org/",
            "description": "Free speech social media platform.",
            "tags": [
                "user-submitted"
            ],
            "views": 15,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 63,
            "name": "Blind",
            "url": "https://www.teamblind.com/",
            "description": "Social media.",
            "tags": [
                "social",
                "media"
            ],
            "views": 9,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 64,
            "name": "Blind recent sort",
            "url": "https://www.teamblind.com/?sort=id",
            "description": "Social media.",
            "tags": [
                "social",
                "media"
            ],
            "views": 9,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 65,
            "name": "Group trivia game",
            "url": "https://crowdparty.app/",
            "description": "Group online trivia game.",
            "tags": [
                "user-submitted"
            ],
            "views": 2,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 66,
            "name": "Group puzzle game",
            "url": "https://codenames.game/",
            "description": "Group online puzzle game.",
            "tags": [
                "user-submitted"
            ],
            "views": 2,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 67,
            "name": "Moj",
            "url": "https://mojapp.in",
            "description": "TikTok alternative.",
            "tags": [
                "tiktok",
                "moj",
                "social",
                "media",
                "socialmedia",
                "app"
            ],
            "views": 1,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 1,
            "dislikesDesktop": 0
        },
        {
            "id": 68,
            "name": "Oppo",
            "url": "https://www.oppo.com",
            "description": "Mobile phone company.",
            "tags": [
                "mobile",
                "mobile phone",
                "company",
                "mobilephonecompany",
                "poetic"
            ],
            "views": 3,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 69,
            "name": "OnePlus",
            "url": "https://www.oneplus.com",
            "description": "Phone company",
            "tags": [
                "phone",
                "phonecompany",
                "mobilephonecompany",
                "poetic"
            ],
            "views": 2,
            "likesMobile": 1,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 70,
            "name": "test",
            "url": "https://test.com",
            "description": "Test",
            "tags": [
                "test",
                "testingalot",
                "good"
            ],
            "views": 2,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 71,
            "name": "Test2",
            "url": "https://test2.com",
            "description": "Testing 2",
            "tags": [
                "test",
                "testingalot",
                "cool"
            ],
            "views": 1,
            "likesMobile": 0,
            "dislikesMobile": 1000,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 72,
            "name": "Anna's archive",
            "url": "https://annas-archive.se/",
            "description": "Open library.",
            "tags": [
                "open",
                "library",
                "books"
            ],
            "views": 0,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 73,
            "name": "Tails",
            "url": "https://tails.net",
            "description": "Secure OS.",
            "tags": [
                "secure",
                "os",
                "privacy"
            ],
            "views": 0,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 74,
            "name": "Interview Ready - System Design",
            "url": "https://interviewready.io/learn/system-design-course/how-do-i-use-this-course/what-do-we-offer?tab=chapters",
            "description": "Best System Design course.",
            "tags": [
                "systemdesign",
                "course",
                "learning"
            ],
            "views": 1,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 75,
            "name": "LLM Embeddings Explained",
            "url": "https://huggingface.co/spaces/hesamation/primer-llm-embedding",
            "description": "LLM Embeddings Explained:\nA Visual and Intuitive Guide",
            "tags": [
                "llm",
                "ai",
                "explained"
            ],
            "views": 1,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        },
        {
            "id": 76,
            "name": "Linux Kernel Explorer",
            "url": "https://reverser.dev/linux-kernel-explorer",
            "description": "A portal to study the Linux Kernel.",
            "tags": [
                "linux",
                "kernel"
            ],
            "views": 0,
            "likesMobile": 0,
            "dislikesMobile": 0,
            "likesDesktop": 0,
            "dislikesDesktop": 0
        }
    ]
};

if (typeof module !== 'undefined') {
    module.exports = { STATIC };
}
