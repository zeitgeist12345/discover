// Global variables
let currentIndex = -1;
let visitedWebsites = [];
let websiteHistory = [];

// Initialize the page
document.addEventListener('DOMContentLoaded', function () {
    // Page is ready
});

function loadRandomWebsite() {
    // Get a random website that hasn't been visited yet
    const unvisitedWebsites = websites.filter((_, index) => !visitedWebsites.includes(index));

    if (unvisitedWebsites.length === 0) {
        // All websites have been visited, reset
        visitedWebsites = [];
        websiteHistory = [];
        currentIndex = -1;
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
    window.open(website.url, '_blank');
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



// Keyboard shortcuts
document.addEventListener('keydown', function (event) {
    // Only trigger shortcuts if not typing in an input field
    if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') {
        return;
    }

    switch (event.key) {
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
document.querySelector('.header h1').addEventListener('click', function () {
    clickCount++;
    if (clickCount === 5) {
        this.textContent = '🎉 You found the secret! 🎉';
        setTimeout(() => {
            this.textContent = '🌐 Discover';
            clickCount = 0;
        }, 2000);
    }
}); 