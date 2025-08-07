// Global variables
let websites = [];
let currentIndex = -1;
let websiteHistory = [];
let visitedWebsites = [];
let isLoading = false;
let currentWebsiteId = null;
const userActions = new Map();

console.log('Script.js loaded successfully');

// Initialize the app when the page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, initializing app...');
    initializeApp();
});

function initializeApp() {
    console.log('Initializing app...');
    // Load websites based on configuration
    if (CONFIG.USE_API) {
        console.log('Using API mode');
        loadWebsitesFromAPI();
    } else {
        console.log('Using static mode');
        loadStaticWebsites();
    }
}

async function loadWebsitesFromAPI() {
    try {
        isLoading = true;
        
        // Create a timeout promise
        const timeoutPromise = new Promise((_, reject) => {
            setTimeout(() => reject(new Error('Request timeout')), CONFIG.API_TIMEOUT);
        });
        
        // Create the fetch promise
        const fetchPromise = fetch(`${CONFIG.API_BASE_URL}/getWebsitesDesktop`);
        
        // Race between fetch and timeout
        const response = await Promise.race([fetchPromise, timeoutPromise]);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        websites = await response.json();
        
        // Sort websites by name for consistency
        websites.sort((a, b) => a.name.localeCompare(b.name));
        
        console.log(`Loaded ${websites.length} websites from API`);
        console.log('Websites list:', websites.map(w => `${w.name} (${w.url})`));
        
        isLoading = false;
        enableControls();
        
    } catch (error) {
        console.error('Failed to load websites from API:', error);
        isLoading = false;
        
        if (CONFIG.ENABLE_FALLBACK) {
            console.log('Attempting fallback to static websites...');
            loadStaticWebsites();
        } else {
            showErrorMessage(CONFIG.ERROR_MESSAGE);
        }
    }
}

async function loadStaticWebsites() {
    try {
        // Check if websites are already loaded
        if (typeof window.websites !== 'undefined' && window.websites.length > 0) {
            websites = window.websites;
            console.log(`Loaded ${websites.length} websites from static file`);
            enableControls();
            return;
        }
        
        // Try to load from the static websites.js file
        const script = document.createElement('script');
        script.src = 'websites.js';
        script.onload = function() {
            if (typeof window.websites !== 'undefined' && window.websites.length > 0) {
                websites = window.websites;
                console.log(`Loaded ${websites.length} websites from static file`);
                enableControls();
            } else {
                showErrorMessage('No websites available. Please check your connection and refresh.');
            }
        };
        script.onerror = function() {
            showErrorMessage('Unable to load websites. Please check your connection and refresh.');
        };
        document.head.appendChild(script);
    } catch (error) {
        console.error('Failed to load static websites:', error);
        showErrorMessage('No websites available. Please check your connection and refresh.');
    }
}

function enableControls() {
    console.log('enableControls called');
    
    // Hide loading animation
    const loadingAnimation = document.getElementById('loading-animation');
    const controlButtons = document.getElementById('control-buttons');
    
    if (loadingAnimation) {
        loadingAnimation.style.display = 'none';
        console.log('Loading animation hidden');
    }
    
    // Show control buttons with animation
    if (controlButtons) {
        controlButtons.style.display = 'flex';
        console.log('Control buttons container shown');
        
        // Trigger animation after a brief delay
        setTimeout(() => {
            controlButtons.classList.add('show');
            console.log('Control buttons animation triggered');
        }, 10);
    }
    
    // Enable only the control buttons (not modal buttons)
    if (controlButtons) {
        const buttons = controlButtons.querySelectorAll('.btn');
        console.log('Found control buttons:', buttons.length);
        
        buttons.forEach((btn, index) => {
            btn.disabled = false;
            console.log(`Control button ${index} enabled:`, btn.textContent);
        });
    }
}

function showErrorMessage(message) {
    // Create error message element
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.innerHTML = `
        <div class="website-box" style="border-color: #ef4444; background: rgba(239, 68, 68, 0.1);">
            <p style="color: #ef4444; margin: 0;">${message}</p>
        </div>
    `;
    
    // Insert after header
    const header = document.querySelector('.header');
    header.parentNode.insertBefore(errorDiv, header.nextSibling);
    
    // Remove after 5 seconds
    setTimeout(() => {
        if (errorDiv.parentNode) {
            errorDiv.parentNode.removeChild(errorDiv);
        }
    }, 5000);
}

async function updateWebsiteStats(websiteId, action) {
    if (!CONFIG.ENABLE_VIEW_TRACKING) {
        return;
    }
    
    // Check if user has already performed this action for this website
    const actionKey = `${websiteId}-${action}`;
    if (userActions.has(actionKey)) {
        console.log(`User already performed ${action} for ${websiteId}`);
        return;
    }
    
    // Find the website to get its URL
    const website = websites.find(w => w.id === websiteId);
    if (!website) {
        console.error(`Website not found with id: ${websiteId}`);
        return;
    }
    
    // Optimistic update - update UI immediately
    const currentStats = {
        views: parseInt(document.getElementById('views-count').textContent) || 0,
        likes: parseInt(document.getElementById('likes-count').textContent) || 0,
        dislikes: parseInt(document.getElementById('dislikes-count').textContent) || 0
    };
    
    // Increment the appropriate counter
    switch (action) {
        case 'view':
            currentStats.views++;
            break;
        case 'like':
            currentStats.likes++;
            break;
        case 'dislike':
            currentStats.dislikes++;
            break;
    }
    
    // Update UI immediately (optimistic)
    updateStatsDisplay(currentStats);
    
    // Mark this action as performed to prevent duplicate clicks
    userActions.set(actionKey, true);
    
    // Update button states for like/dislike
    if (action === 'like' || action === 'dislike') {
        updateButtonStates(action);
    }
    
    // Sync with server in the background
    try {
        actionDesktop = action === 'like' ? 'likeDesktop' : action === 'dislike' ? 'dislikeDesktop' : 'view';

        const response = await fetch(`${CONFIG.API_BASE_URL}/incrementViewDesktop?id=${websiteId}&url=${encodeURIComponent(website.url)}&category=curated&action=${actionDesktop}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            const result = await response.json();
            console.log(`Synced ${action} for ${websiteId}:`, result);
            
            // Update UI with actual server response (in case there were any server-side adjustments)
            updateStatsDisplay(result);
            
        } else {
            console.error(`Failed to sync ${action} for ${websiteId}:`, response.status);
            // Optionally revert the optimistic update on error
            // For now, we'll keep the optimistic update for better UX
        }
    } catch (error) {
        console.error(`Failed to sync ${action} for ${websiteId}:`, error);
        // Optionally revert the optimistic update on error
        // For now, we'll keep the optimistic update for better UX
    }
}

function updateStatsDisplay(stats, forceUpdate = false) {
    const viewsCount = document.getElementById('views-count');
    const likesCount = document.getElementById('likes-count');
    const dislikesCount = document.getElementById('dislikes-count');
    
    // Only update if forceUpdate is true or if the new value is higher (preserve optimistic updates)
    if (viewsCount && (forceUpdate || (stats.views || 0) > parseInt(viewsCount.textContent))) {
        viewsCount.textContent = stats.views || 0;
    }
    if (likesCount && (forceUpdate || (stats.likes || 0) > parseInt(likesCount.textContent))) {
        likesCount.textContent = stats.likes || 0;
    }
    if (dislikesCount && (forceUpdate || (stats.dislikes || 0) > parseInt(dislikesCount.textContent))) {
        dislikesCount.textContent = stats.dislikes || 0;
    }
}

function updateButtonStates(action) {
    const likeBtn = document.getElementById('like-btn');
    const dislikeBtn = document.getElementById('dislike-btn');
    
    // Check if buttons exist before manipulating them
    if (!likeBtn || !dislikeBtn) {
        console.error('Like or dislike buttons not found');
        return;
    }
    
    if (action === 'like') {
        likeBtn.classList.add('liked');
        dislikeBtn.classList.remove('disliked');
    } else if (action === 'dislike') {
        dislikeBtn.classList.add('disliked');
        likeBtn.classList.remove('liked');
    }
}

function likeWebsite() {
    if (currentWebsiteId) {
        updateWebsiteStats(currentWebsiteId, 'like');
    } else {
        console.error('No current website ID available for like action');
    }
}

function dislikeWebsite() {
    if (currentWebsiteId) {
        updateWebsiteStats(currentWebsiteId, 'dislike');
    } else {
        console.error('No current website ID available for dislike action');
    }
}

function loadRandomWebsite() {
    console.log('loadRandomWebsite called');
    console.log('isLoading:', isLoading);
    console.log('websites.length:', websites.length);
    console.log('visitedWebsites:', visitedWebsites);
    
    if (isLoading || websites.length === 0) {
        console.log('Cannot load random website - loading or no websites');
        return;
    }

    // Get a random website that hasn't been visited yet
    const unvisitedWebsites = websites.filter((_, index) => !visitedWebsites.includes(index));
    console.log('unvisitedWebsites.length:', unvisitedWebsites.length);

    if (unvisitedWebsites.length === 0) {
        // All websites have been visited, reset
        console.log('All websites visited, resetting...');
        visitedWebsites = [];
        websiteHistory = [];
        currentIndex = -1;
        setTimeout(loadRandomWebsite, 2000);
        return;
    }

    const randomIndex = Math.floor(Math.random() * unvisitedWebsites.length);
    const website = unvisitedWebsites[randomIndex];
    const originalIndex = websites.indexOf(website);
    
    console.log('Selected random website:', website.name, 'at index:', originalIndex);

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
    console.log('loadWebsite called with index:', index, 'addToHistory:', addToHistory);
    
    // Validate index and website
    if (index < 0 || index >= websites.length) {
        console.error('Invalid website index:', index);
        return;
    }
    
    const website = websites[index];
    if (!website) {
        console.error('Website not found at index:', index);
        return;
    }
    
    console.log('Website to load:', website);

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

    // Track the current website ID for stats
    currentWebsiteId = website.id;

    // Update UI first
    updateCurrentSiteInfo(website);

    // Optimistically increment views immediately
    if (website.id && CONFIG.ENABLE_VIEW_TRACKING) {
        const viewsElement = document.getElementById('views-count');
        if (viewsElement) {
            const currentViews = parseInt(viewsElement.textContent) || 0;
            viewsElement.textContent = currentViews + 1;
            
            // Sync with server in the background
            updateWebsiteStats(website.id, 'view');
        }
    }

    // Open the website in a new window/tab
    console.log('Opening website:', website.url);
    window.open(website.url, '_blank');
}

function updateCurrentSiteInfo(website) {
    const link = document.getElementById('current-site-link');
    const statsDiv = document.getElementById('website-stats');
    const likeBtn = document.getElementById('like-btn');
    const dislikeBtn = document.getElementById('dislike-btn');
    
    // Check if link element exists before accessing its properties
    if (link) {
        link.href = website.url;
        link.textContent = `${website.name} - ${website.url}`;
    } else {
        console.error('current-site-link element not found');
        return;
    }

    // Add description as a separate element
    const description = document.getElementById('website-description');
    if (description) {
        description.textContent = website.description;
    }

    // Show stats and reset button states
    if (statsDiv) {
        statsDiv.style.display = 'block';
    }
    
    // Reset button states
    if (likeBtn) likeBtn.classList.remove('liked');
    if (dislikeBtn) dislikeBtn.classList.remove('disliked');
    
    // Update stats with current data (force update for initial load)
    updateStatsDisplay({
        views: website.views || 0,
        likes: website.likes || 0,
        dislikes: website.dislikes || 0
    }, true);
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

// Add Website Modal Functions
function showAddWebsiteForm() {
    const modal = document.getElementById('add-website-modal');
    modal.style.display = 'flex';
    
    // Clear any existing error messages
    hideModalError();
    
    // Add custom validation listeners
    addCustomValidation();
    
    // Focus on first input
    setTimeout(() => {
        document.getElementById('website-name').focus();
    }, 100);
}

function addCustomValidation() {
    const form = document.getElementById('add-website-form');
    const inputs = form.querySelectorAll('input[required], textarea[required]');
    
    inputs.forEach(input => {
        // Remove existing listeners to prevent duplicates
        input.removeEventListener('invalid', handleInvalidInput);
        input.removeEventListener('input', clearInputError);
        
        // Add new listeners
        input.addEventListener('invalid', handleInvalidInput);
        input.addEventListener('input', clearInputError);
    });
}

function handleInvalidInput(event) {
    event.preventDefault();
    
    const input = event.target;
    const fieldName = input.getAttribute('name');
    let errorMessage = '';
    
    // Custom error messages for different fields
    switch(fieldName) {
        case 'name':
            errorMessage = 'Please enter a link name';
            break;
        case 'url':
            errorMessage = 'Please enter a valid URL (e.g., https://example.com)';
            break;
        case 'description':
            errorMessage = 'Please enter a description for the link';
            break;
        default:
            errorMessage = 'This field is required';
    }
    
    // Show error in modal
    showModalError(errorMessage);
    
    // Add visual error styling to the input
    input.classList.add('input-error');
    
    // Focus on the problematic field
    input.focus();
}

function clearInputError(event) {
    const input = event.target;
    input.classList.remove('input-error');
    hideModalError();
}

function clearAllInputErrors() {
    const inputs = document.querySelectorAll('.input-error');
    inputs.forEach(input => {
        input.classList.remove('input-error');
    });
}

function hideAddWebsiteForm() {
    const modal = document.getElementById('add-website-modal');
    modal.style.display = 'none';
    
    // Reset form
    document.getElementById('add-website-form').reset();
    
    // Clear any error messages
    hideModalError();
}

function showModalError(message) {
    const errorDiv = document.getElementById('modal-error-message');
    const errorText = document.getElementById('modal-error-text');
    
    if (errorDiv && errorText) {
        errorText.textContent = message;
        errorDiv.style.display = 'block';
        
        // Scroll to error message
        errorDiv.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
}

function hideModalError() {
    const errorDiv = document.getElementById('modal-error-message');
    if (errorDiv) {
        errorDiv.style.display = 'none';
    }
}

async function submitWebsite(event) {
    event.preventDefault();
    
    // Clear any existing errors
    hideModalError();
    clearAllInputErrors();
    
    // Check if form is valid
    const form = event.target;
    if (!form.checkValidity()) {
        // Trigger validation for the first invalid field
        const firstInvalid = form.querySelector(':invalid');
        if (firstInvalid) {
            firstInvalid.focus();
            handleInvalidInput({ target: firstInvalid, preventDefault: () => {} });
        }
        return;
    }
    
    const submitBtn = document.getElementById('submit-btn');
    const originalText = submitBtn.textContent;
    
    // Show loading state
    submitBtn.disabled = true;
    submitBtn.classList.add('btn-loading');
    submitBtn.textContent = 'Adding...';
    
    try {
        const formData = new FormData(event.target);
        const websiteData = {
            name: formData.get('name'),
            url: formData.get('url'),
            description: formData.get('description'),
            category: formData.get('category'),
            views: 0,
            likes: 0,
            dislikes: 0,
            likesDesktop: 0,
            dislikesDesktop: 0
        };
        
        const response = await fetch(`${CONFIG.API_BASE_URL}/addwebsite`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(websiteData)
        });
        
        const result = await response.json();
        
        if (response.ok) {
            // Success
            showSuccessMessage('Website added successfully! 🎉');
            hideAddWebsiteForm();
            
            // Reload websites to include the new one
            if (CONFIG.USE_API) {
                await loadWebsitesFromAPI();
            }
        } else {
            // Handle specific error cases
            if (response.status === 409) {
                showModalError('This link already exists in the database. Please try a different URL.');
            } else {
                showModalError(result.error || 'Failed to add website');
            }
        }
        
    } catch (error) {
        console.error('Error submitting website:', error);
        showModalError('Failed to add website. Please try again.');
    } finally {
        // Reset button state
        submitBtn.disabled = false;
        submitBtn.classList.remove('btn-loading');
        submitBtn.textContent = originalText;
    }
}

function showSuccessMessage(message) {
    // Create success message element
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.innerHTML = `
        <div class="website-box" style="border-color: #10b981; background: rgba(16, 185, 129, 0.1);">
            <p style="color: #10b981; margin: 0;">${message}</p>
        </div>
    `;
    
    // Insert after header
    const header = document.querySelector('.header');
    header.parentNode.insertBefore(successDiv, header.nextSibling);
    
    // Remove after 3 seconds
    setTimeout(() => {
        if (successDiv.parentNode) {
            successDiv.parentNode.removeChild(successDiv);
        }
    }, 3000);
}

// Close modal when clicking outside
document.addEventListener('click', function(event) {
    const modal = document.getElementById('add-website-modal');
    if (event.target === modal) {
        hideAddWebsiteForm();
    }
});

// Close modal with Escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        const modal = document.getElementById('add-website-modal');
        if (modal.style.display === 'flex') {
            hideAddWebsiteForm();
        }
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