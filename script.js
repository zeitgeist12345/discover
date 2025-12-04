// script.js
// Global variables
let websites = [];
let currentIndex = -1;
let websiteHistory = [];
let visitedWebsites = [];
let isLoading = false;
let currentWebsiteUrl = null;
let selectedTags = [];
const userActions = new Map();

const UI_ANIMATION_DELAY = 10;
const FOCUS_DELAY = 100;
const RESET_DELAY = 2000;
const RESET_DELAY_LONG = 10000;

const API_BASE_URL = 'https://backend.discoverall.space';
const API_TIMEOUT = 3000;
const ENABLE_FALLBACK = true;
const ENABLE_VIEW_TRACKING = true;
const ERROR_MESSAGE = 'Unable to connect to the server. Please make sure your local backend is running.';

// Initialize the app when the page loads
document.addEventListener('DOMContentLoaded', function () {
    console.log('DOM loaded, initializing app...');
    initializeApp();
});

function initializeApp() {
    console.log('Initializing app...');
    console.log('Using API mode');
    loadWebsitesFromAPI();
}

async function loadWebsitesFromAPI(tagsAllowlist = [], tagsBlocklist = []) {
    try {
        isLoading = true;

        const query = `&tagsAllowlist=${encodeURIComponent(tagsAllowlist.join(','))}&tagsBlocklist=${encodeURIComponent(tagsBlocklist.join(','))}`;
        const response = await fetch(`${API_BASE_URL}/getWebsites?platform=desktop${query}`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        websites = await response.json();
        console.log(`Loaded ${websites.length} websites from API.`);

        enableControls();
        document.getElementById('api-status-indicator').classList.add('online');

        // ✅ Return the count so it can be shown in the success message
        return websites.length;
    } catch (error) {
        console.error('Failed to load websites from API:', error);
        showErrorMessage("Failed to load links from API.<br>Cannot reach backend server: " + error + "<br>Fallback to static list.");

        if (ENABLE_FALLBACK) loadStaticWebsites();

        document.getElementById('api-status-indicator').classList.add('offline');
        return 0;
    } finally {
        isLoading = false;
    }
}

async function loadStaticWebsites() {
    if (STATIC.SAMPLE_WEBSITES && STATIC.SAMPLE_WEBSITES.length > 0) {
        websites = STATIC.SAMPLE_WEBSITES;
        console.log(`Loaded ${websites.length} websites from static.js fallback`);
        enableControls();
    } else {
        showErrorMessage('No static websites available. Please check your static.js file.');
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
        }, UI_ANIMATION_DELAY);
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

    // If an error box already exists, just update the text.
    const existing = document.querySelector('.error-message');
    if (existing) {
        existing.querySelector('p').textContent = message;
        return;
    }

    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.innerHTML = `
        <div class="website-box" style="border-color:#ef4444; background:rgba(239,68,68,0.1);">
            <p style="color:#ef4444; margin:0;">${message}</p>
        </div>
    `;

    document.body.appendChild(errorDiv);
}



async function updateWebsiteStats(websiteUrl, action) {
    if (!ENABLE_VIEW_TRACKING) {
        return;
    }

    // Check if user has already performed this action for this website
    const actionKey = `${websiteUrl}-${action}`;
    if (userActions.has(actionKey)) {
        console.log(`User already performed ${action} for ${websiteUrl}`);
        return;
    }

    // Find the website to get its URL
    const website = websites.find(w => w.url === websiteUrl);
    if (!website) {
        console.error(`Website not found with url: ${websiteUrl}`);
        return;
    }

    // Optimistic update - update UI immediately
    const currentStats = {
        views: parseInt(document.getElementById('views-count').textContent) || 0,
        likesDesktop: parseInt(document.getElementById('likesDesktop-count').textContent) || 0,
        dislikesDesktop: parseInt(document.getElementById('dislikesDesktop-count').textContent) || 0
    };

    // Increment the appropriate counter
    switch (action) {
        case 'view':
            currentStats.views++;
            break;
        case 'likesDesktop':
            currentStats.likesDesktop++;
            break;
        case 'dislikesDesktop':
            currentStats.dislikesDesktop++;
            break;
    }

    // Update UI immediately (optimistic)
    updateStatsDisplay(currentStats);

    // Mark this action as performed to prevent duplicate clicks
    userActions.set(actionKey, true);

    // Update button states for likesDesktop/dislikesDesktop
    if (action === 'likesDesktop' || action === 'dislikesDesktop') {
        updateButtonStates(action);
    }

    // Sync with server in the background
    try {
        const response = await fetch(`${API_BASE_URL}/incrementView?url=${encodeURIComponent(website.url)}&action=${action}`, {
            method: 'POST'
        });

        if (response.ok) {
            const result = await response.json();
            console.log(`Synced ${action} for ${websiteUrl}:`, result);

            // Update UI with actual server response (in case there were any server-side adjustments)
            updateStatsDisplay(result);

        } else {
            console.error(`Failed to sync ${action} for ${websiteUrl}:`, response.status);
            // Optionally revert the optimistic update on error
            // For now, we'll keep the optimistic update for better UX
        }
    } catch (error) {
        console.error(`Failed to sync ${action} for ${websiteUrl}:`, error);
        showErrorMessage(action + " action failed due to backend server unreachable: " + error.message);
        // Optionally revert the optimistic update on error
        // For now, we'll keep the optimistic update for better UX
    }
}

function updateStatsDisplay(stats, forceUpdate = false) {
    const viewsCount = document.getElementById('views-count');
    const likesDesktopCount = document.getElementById('likesDesktop-count');
    const dislikesDesktopCount = document.getElementById('dislikesDesktop-count');

    // Only update if forceUpdate is true or if the new value is higher (preserve optimistic updates)
    if (viewsCount && (forceUpdate || (stats.views || 0) > parseInt(viewsCount.textContent))) {
        viewsCount.textContent = stats.views || 0;
    }
    if (likesDesktopCount && (forceUpdate || (stats.likesDesktop || 0) > parseInt(likesDesktopCount.textContent))) {
        likesDesktopCount.textContent = stats.likesDesktop || 0;
    }
    if (dislikesDesktopCount && (forceUpdate || (stats.dislikesDesktop || 0) > parseInt(dislikesDesktopCount.textContent))) {
        dislikesDesktopCount.textContent = stats.dislikesDesktop || 0;
    }
}

function updateButtonStates(action) {
    const likesDesktopBtn = document.getElementById('likesDesktop-btn');
    const dislikesDesktopBtn = document.getElementById('dislikesDesktop-btn');

    // Check if buttons exist before manipulating them
    if (!likesDesktopBtn || !dislikesDesktopBtn) {
        console.error('likesDesktop or dislikesDesktop buttons not found');
        return;
    }

    if (action === 'likesDesktop') {
        likesDesktopBtn.classList.add('likesDesktopd');
        dislikesDesktopBtn.classList.remove('dislikesDesktopd');
    } else if (action === 'dislikesDesktop') {
        dislikesDesktopBtn.classList.add('dislikesDesktopd');
        likesDesktopBtn.classList.remove('likesDesktopd');
    }
}

function likesDesktopWebsite() {
    if (currentWebsiteUrl) {
        updateWebsiteStats(currentWebsiteUrl, 'likesDesktop');
    } else {
        console.error('No current website ID available for likesDesktop action');
    }
}

function dislikesDesktopWebsite() {
    if (currentWebsiteUrl) {
        updateWebsiteStats(currentWebsiteUrl, 'dislikesDesktop');
    } else {
        console.error('No current website ID available for dislikesDesktop action');
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
        setTimeout(loadRandomWebsite, RESET_DELAY);
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

    // Track the current website url for stats
    currentWebsiteUrl = website.url;

    // Update UI first
    updateCurrentSiteInfo(website);

    // Optimistically increment views immediately
    if (website.url && ENABLE_VIEW_TRACKING) {
        const viewsElement = document.getElementById('views-count');
        if (viewsElement) {
            const currentViews = parseInt(viewsElement.textContent) || 0;
            viewsElement.textContent = currentViews + 1;

            // Sync with server in the background
            updateWebsiteStats(website.url, 'view');
        }
    }

    // Open the website in a new window/tab
    let urlToOpen = website.url;
    if (!/^https?:\/\//i.test(urlToOpen)) {
        urlToOpen = 'https://' + urlToOpen;
    }

    console.log('Opening website:', urlToOpen);
    window.open(urlToOpen, '_blank');
}

function updateCurrentSiteInfo(website) {
    const link = document.getElementById('current-site-link');
    const statsDiv = document.getElementById('website-stats');
    const likesDesktopBtn = document.getElementById('likesDesktop-btn');
    const dislikesDesktopBtn = document.getElementById('dislikesDesktop-btn');

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
    // Add tags as a separate element
    const tags = document.getElementById('website-tags');
    tags.textContent = `Tags: [${website.tags.join(', ')}]`;


    // Show stats and reset button states
    if (statsDiv) {
        statsDiv.style.display = 'block';
    }

    // Reset button states
    if (likesDesktopBtn) likesDesktopBtn.classList.remove('likesDesktopd');
    if (dislikesDesktopBtn) dislikesDesktopBtn.classList.remove('dislikesDesktopd');

    // Update stats with current data (force update for initial load)
    updateStatsDisplay({
        views: website.views || 0,
        likesDesktop: website.likesDesktop || 0,
        dislikesDesktop: website.dislikesDesktop || 0
    }, true);
}

// Add Website Modal Functions
function showAddWebsiteForm() {
    const modal = document.getElementById('add-website-modal');
    modal.style.display = 'flex';

    // Clear any existing error messages
    hideModalError();

    // Clear any existing tags
    selectedTags = [];

    // Add custom validation listeners
    addCustomValidation();

    // Focus on first input
    setTimeout(() => {
        document.getElementById('website-name').focus();
    }, FOCUS_DELAY);
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
    switch (fieldName) {
        case 'name':
            errorMessage = 'Please enter a link name';
            break;
        case 'url':
            // Check what specific validation error occurred
            if (input.validity.valueMissing) {
                errorMessage = 'Please enter a URL';
            } else {
                // For URL pattern validation, provide a clearer message
                errorMessage = 'Please enter a valid URL (e.g., example.com or https://example.com)';
            }
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

    // Reset tags
    selectedTags = [];

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

function normalizeUrl(url) {
    if (!url) return url;

    // If it doesn't start with http:// or https://, add https://
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
        return 'https://' + url;
    }
    return url;
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
            handleInvalidInput({ target: firstInvalid, preventDefault: () => { } });
        }
        return;
    }

    const submitBtn = document.getElementById('submit-btn');
    const originalText = submitBtn.textContent;

    // Show loading state
    submitBtn.disabled = true;
    submitBtn.classList.add('btn-loading');
    submitBtn.textContent = 'Adding...';

    const tagsInput = document.getElementById('add-website-tags');
    selectedTags = tagsInput.value.split(",")
        .map(tag => tag.trim().replace(/\s+/g, "")) // remove internal spaces
        .filter(tag => tag.length > 0);

    try {
        const formData = new FormData(event.target);
        const websiteData = {
            name: formData.get('name'),
            url: normalizeUrl(formData.get('url')),
            description: formData.get('description'),
            tags: selectedTags.length > 0 ? selectedTags : ['user-submitted'],
            views: 0,
            likesDesktop: 0,
            dislikesDesktop: 0
        };

        const response = await fetch(`${API_BASE_URL}/addwebsite`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(websiteData)
        });

        const result = await response.json();

        if (response.ok) {
            // Success
            showSuccessMessage('Link submitted for spam review successfully! The link will be live globally after review approval 🎉');
            hideAddWebsiteForm();

            // Reset tags
            selectedTags = [];
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
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';

    const boxDiv = document.createElement('div');
    boxDiv.className = 'website-box success-box';

    const messagePara = document.createElement('p');
    messagePara.className = 'success-text';
    messagePara.textContent = message;

    boxDiv.appendChild(messagePara);
    successDiv.appendChild(boxDiv);

    const header = document.querySelector('.header');
    if (header) {
        header.after(successDiv);
    }

    setTimeout(() => {
        successDiv.remove();
    }, RESET_DELAY_LONG);
}

// Close modal when clicking outside
document.addEventListener('click', function (event) {
    const modal = document.getElementById('add-website-modal');
    if (event.target === modal) {
        hideAddWebsiteForm();
    }
    if (event.target.classList.contains("btn")) {
        document.querySelectorAll(".error-message").forEach(el => el.remove());
    }
});

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
        case 'Escape':
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
        }, RESET_DELAY);
    }
});


// Settings screen toggle
document.getElementById('settings-toggle').addEventListener('click', () => {
    const settingsScreen = document.getElementById('settings-screen');
    const btn = document.getElementById('settings-toggle');

    if (settingsScreen.style.display === 'none') {
        settingsScreen.style.display = 'flex';
        btn.textContent = '⬅️';
    } else {
        settingsScreen.style.display = 'none';
        btn.textContent = '⚙️';
    }
});

async function applyTagFilter() {
    const tagInputAllowlist = document.getElementById('filter-tags-allowlist');
    const tagInputBlocklist = document.getElementById('filter-tags-blocklist');
    const successBox = document.getElementById('filter-success');

    const tagsAllowlist = tagInputAllowlist.value
        .split(',')
        .map(t => t.trim())
        .filter(t => t.length > 0);
    const tagsBlocklist = tagInputBlocklist.value
        .split(',')
        .map(t => t.trim())
        .filter(t => t.length > 0);

    console.log('Applying tag filter:', tagsAllowlist, tagsBlocklist);

    // ✅ Wait for API and get website count
    const websiteCount = await loadWebsitesFromAPI(tagsAllowlist, tagsBlocklist);

    if (tagsAllowlist.length > 0 || tagsBlocklist.length > 0) {
        successBox.textContent = `✅ Filter applied: ${websiteCount} website${websiteCount !== 1 ? 's' : ''} found`;
    } else {
        successBox.textContent = `✅ Showing all ${websiteCount} website${websiteCount !== 1 ? 's' : ''}`;
    }

    successBox.style.display = 'inline-block';

    // Auto-hide after 3 seconds
    setTimeout(() => {
        successBox.style.display = 'none';
    }, 3000);
}

