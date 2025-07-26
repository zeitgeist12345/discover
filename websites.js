// Static list of websites to explore
window.websites = [
    {
        name: "skribbl.io",
        url: "https://skribbl.io/",
        description: "Free multiplayer drawing and guessing game"
    },
    {
        name: "zeitgeist12345",
        url: "https://zeitgeist12345.github.io/",
        description: "The personal website of the creator of this project"
    },
    {
        name: "Sci-Hub",
        url: "https://sci-hub.se/",
        description: "Removing barriers in the way of science by providing free access to research papers"
    },
    {
        name: "Library Genesis",
        url: "https://libgen.li/",
        description: "Massive digital library of books, articles, and media"
    },
    {
        name: "Internet Archive",
        url: "https://archive.org/",
        description: "Digital library of free & borrowable books, movies, music & Wayback Machine"
    },
    {
        name: "The Useless Web",
        url: "https://theuselessweb.com/",
        description: "Random fun and bizarre websites with one click"
    },
    {
        name: "Play Counter-Strike 1.6",
        url: "https://play-cs.com/",
        description: "Play classic CS 1.6 online without downloading"
    },
    {
        name: "Product Hunt",
        url: "https://producthunt.com",
        description: "The best new products in tech"
    },
    {
        name: "Hacker News",
        url: "https://news.ycombinator.com",
        description: "Social news website focusing on computer science"
    },
    {
        name: "Overleaf",
        url: "https://www.overleaf.com/",
        description: "Online LaTeX editor with real-time collaboration"
    },
    {
        name: "The Longest Blockchain",
        url: "https://cryptoservices.github.io/blockchain/consensus/2019/05/21/bitcoin-length-weight-confusion.html",
        description: "Interesting perspective on blockchain strength"
    },
    {
        name: "Bored Button",
        url: "https://www.boredbutton.com/",
        description: "Collection of random fun websites and games"
    },
    {
        name: "Radio Garden",
        url: "http://radio.garden/",
        description: "Listen to live radio stations across the globe"
    },
    {
        name: "Window Swap",
        url: "https://window-swap.com/",
        description: "See the view from someone else's window around the world"
    },
    {
        name: "The Pudding",
        url: "https://pudding.cool/",
        description: "Visual essays that explain ideas with data and visuals"
    },
    {
        name: "xkcd",
        url: "https://xkcd.com/",
        description: "A webcomic of romance, sarcasm, math, and language"
    },
    {
        name: "Stellarium Web",
        url: "https://stellarium-web.org/",
        description: "Real-time 3D simulation of space with planetarium view"
    },
    {
        name: "Patatap",
        url: "https://patatap.com",
        description: "Turn your keyboard into a sound machine with colorful animations"
    },
    {
        name: "Little Alchemy 2",
        url: "https://littlealchemy2.com",
        description: "Combine elements to discover new objects (e.g., Earth + Fire = Lava)"
    },
    {
        name: "Pointer Pointer",
        url: "https://pointerpointer.com",
        description: "Photos of people pointing at your cursor wherever you move it"
    },
    {
        name: "Ncase.me",
        url: "https://ncase.me",
        description: "Interactive simulations about trust and human behavior"
    },
    {
        name: "Connected Papers",
        url: "https://www.connectedpapers.com",
        description: "Visual tool to explore academic research connections"
    },
    {
        name: "Quick, Draw!",
        url: "https://quickdraw.withgoogle.com",
        description: "AI game that guesses your doodles"
    },
    {
        name: "A Soft Murmur",
        url: "https://asoftmurmur.com",
        description: "Mix ambient sounds (rain, waves) for focus"
    },
    {
        name: "10 Minute Mail",
        url: "https://10minutemail.com",
        description: "Disposable email for spam-free signups"
    },
    {
        name: "The Deep Sea",
        url: "https://neal.fun/deep-sea",
        description: "Interactive dive into ocean depths with fascinating facts"
    },
    {
        name: "Don't Even Reply",
        url: "https://dontevenreply.com",
        description: "Hilarious fictional email exchanges"
    },
    {
        name: "Scream Into the Void",
        url: "https://screamintothevoid.com",
        description: "Type your frustrations and hear a scream"
    },
    {
        name: "This Is Sand",
        url: "https://thisissand.com",
        description: "Digital sand art creator"
    },
    {
        name: "DeepSeek",
        url: "https://www.deepseek.com/en",
        description: "AI research and development company"
    },
    {
        name: "Al Jazeera",
        url: "https://www.aljazeera.com",
        description: "International news and current affairs network"
    }
];

// Export for use in other modules (for Node.js compatibility)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { websites: window.websites };
}
