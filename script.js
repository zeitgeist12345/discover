// Static list of websites to explore
const websites = [
    {
        name: "Wikipedia",
        url: "https://wikipedia.org",
        description: "The free encyclopedia that anyone can edit"
    },
    {
        name: "GitHub",
        url: "https://github.com",
        description: "The world's leading software development platform"
    },
    {
        name: "Stack Overflow",
        url: "https://stackoverflow.com",
        description: "Where developers learn, share, & build careers"
    },
    {
        name: "MDN Web Docs",
        url: "https://developer.mozilla.org",
        description: "Resources for developers, by developers"
    },
    {
        name: "W3Schools",
        url: "https://w3schools.com",
        description: "The world's largest web developer site"
    },
    {
        name: "FreeCodeCamp",
        url: "https://freecodecamp.org",
        description: "Learn to code for free"
    },
    {
        name: "Dev.to",
        url: "https://dev.to",
        description: "A constructive and inclusive social network for software developers"
    },
    {
        name: "CSS-Tricks",
        url: "https://css-tricks.com",
        description: "Tips, tricks, and techniques for CSS"
    },
    {
        name: "Smashing Magazine",
        url: "https://smashingmagazine.com",
        description: "For professional web designers and developers"
    },
    {
        name: "A List Apart",
        url: "https://alistapart.com",
        description: "For people who make websites"
    },
    {
        name: "Web Design Weekly",
        url: "https://webdesignweekly.com",
        description: "A free weekly newsletter for web designers"
    },
    {
        name: "The Verge",
        url: "https://theverge.com",
        description: "Technology, science, art, and culture"
    },
    {
        name: "TechCrunch",
        url: "https://techcrunch.com",
        description: "Latest technology news and startup information"
    },
    {
        name: "Ars Technica",
        url: "https://arstechnica.com",
        description: "Technology news and analysis"
    },
    {
        name: "Hacker News",
        url: "https://news.ycombinator.com",
        description: "Social news website focusing on computer science"
    },
    {
        name: "Product Hunt",
        url: "https://producthunt.com",
        description: "The best new products in tech"
    },
    {
        name: "Dribbble",
        url: "https://dribbble.com",
        description: "Discover and connect with designers worldwide"
    },
    {
        name: "Behance",
        url: "https://behance.net",
        description: "Showcase and discover creative work"
    },
    {
        name: "Awwwards",
        url: "https://awwwards.com",
        description: "The awards for design, creativity and innovation"
    },
    {
        name: "Codepen",
        url: "https://codepen.io",
        description: "An online code editor and open-source learning environment"
    },
    {
        name: "JSFiddle",
        url: "https://jsfiddle.net",
        description: "Online code editor for web developers"
    }
];

let currentIndex = -1;
let visitedWebsites = [];
let websiteHistory = [];

        // Initialize the page
        document.addEventListener('DOMContentLoaded', function() {
            showSuccessMessage("Ready to explore! Click 'Load Random Website' to begin.");
        });

function loadRandomWebsite() {
    // Get a random website that hasn't been visited yet
    const unvisitedWebsites = websites.filter((_, index) => !visitedWebsites.includes(index));
    
    if (unvisitedWebsites.length === 0) {
        // All websites have been visited, reset
        visitedWebsites = [];
        websiteHistory = [];
        currentIndex = -1;
        showSuccessMessage("All websites visited! Starting fresh...");
        setTimeout(loadRandomWebsite, 2000);
        return;
    }

    const randomIndex = Math.floor(Math.random() * unvisitedWebsites.length);
    const website = unvisitedWebsites[randomIndex];
    const originalIndex = websites.indexOf(website);
    
    loadWebsite(originalIndex);
}

function loadNextWebsite() {
    if (websiteHistory.length === 0) {
        loadRandomWebsite();
        return;
    }
    
    if (currentIndex < websiteHistory.length - 1) {
        currentIndex++;
        const websiteIndex = websiteHistory[currentIndex];
        loadWebsite(websiteIndex, false);
    } else {
        loadRandomWebsite();
    }
}

function loadPreviousWebsite() {
    if (websiteHistory.length === 0 || currentIndex <= 0) {
        showSuccessMessage("No previous websites to go back to.");
        return;
    }
    
    currentIndex--;
    const websiteIndex = websiteHistory[currentIndex];
    loadWebsite(websiteIndex, false);
}

function loadWebsite(index, addToHistory = true) {
    const website = websites[index];
    
    if (addToHistory) {
        // Add to history if it's a new website
        if (currentIndex < websiteHistory.length - 1) {
            // Remove any forward history if we're going back and then to a new site
            websiteHistory = websiteHistory.slice(0, currentIndex + 1);
        }
        websiteHistory.push(index);
        currentIndex = websiteHistory.length - 1;
    }

    // Mark as visited
    if (!visitedWebsites.includes(index)) {
        visitedWebsites.push(index);
    }

    // Update UI first
    updateCurrentSiteInfo(website);
    
    // Open the website in a new window/tab
    const newWindow = window.open(website.url, '_blank');
    
    if (newWindow) {
        showSuccessMessage(`Opening ${website.name} in a new window...`);
    } else {
        showSuccessMessage(`Click the link above to visit ${website.name}`);
    }
}

function updateCurrentSiteInfo(website) {
    const link = document.getElementById('current-site-link');
    link.href = website.url;
    link.textContent = `${website.name} - ${website.url}`;
    
    // Add description as a separate element
    const description = document.getElementById('website-description');
    if (description) {
        description.textContent = website.description;
    }
}

function updateStats() {
    // Stats functionality removed for minimalist design
}

function showSuccessMessage(message) {
    const loading = document.getElementById('loading');
    
    loading.querySelector('span').textContent = message;
    loading.style.display = 'flex';
    
    // Change the styling to show success
    loading.style.color = '#4ade80';
    
    // Reset after 3 seconds
    setTimeout(() => {
        loading.style.color = '#888';
        loading.querySelector('span').textContent = 'Ready to explore! Click "Load Random Website" to continue.';
    }, 3000);
}

// Keyboard shortcuts
document.addEventListener('keydown', function(event) {
    // Only trigger shortcuts if not typing in an input field
    if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') {
        return;
    }
    
    switch(event.key) {
        case 'ArrowRight':
            event.preventDefault();
            loadNextWebsite();
            break;
        case 'ArrowLeft':
            event.preventDefault();
            loadPreviousWebsite();
            break;
        case ' ':
            event.preventDefault();
            loadRandomWebsite();
            break;
    }
});

// Add some fun Easter eggs
let clickCount = 0;
document.querySelector('.header h1').addEventListener('click', function() {
    clickCount++;
    if (clickCount === 5) {
        this.textContent = '🎉 You found the secret! 🎉';
        setTimeout(() => {
            this.textContent = '🌐 Discover';
            clickCount = 0;
        }, 2000);
    }
}); 