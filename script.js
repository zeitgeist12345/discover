// script.js
// Global variables
let links = [];
let currentIndex = -1;
let linkHistory = [];
let visitedLinks = [];
let isLoading = false;
let currentLinkUrl = null;
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
    loadLinksFromAPI();
}

async function loadLinksFromAPI(tagsAllowlist = [], tagsBlocklist = []) {
    try {
        isLoading = true;

        const query = `&tagsAllowlist=${encodeURIComponent(tagsAllowlist.join(','))}&tagsBlocklist=${encodeURIComponent(tagsBlocklist.join(','))}`;
        const response = await fetch(`${API_BASE_URL}/getLinks?platform=desktop${query}`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        links = await response.json();
        console.log(`Loaded ${links.length} links from API.`);

        enableControls();
        document.getElementById('api-status-indicator').classList.add('online');

        // ✅ Return the count so it can be shown in the success message
        return links.length;
    } catch (error) {
        console.error('Failed to load links from API:', error);
        showErrorMessage("Failed to load links from API.<br>Cannot reach backend server: " + error + "<br>Fallback to static list.");

        if (ENABLE_FALLBACK) loadStaticLinks();

        document.getElementById('api-status-indicator').classList.add('offline');
        return 0;
    } finally {
        isLoading = false;
    }
}

async function loadStaticLinks() {
    if (STATIC.SAMPLE_LINKS && STATIC.SAMPLE_LINKS.length > 0) {
        links = STATIC.SAMPLE_LINKS;
        console.log(`Loaded ${links.length} links from static.js fallback`);
        enableControls();
    } else {
        showErrorMessage('No static links available. Please check your static.js file.');
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
        <div class="link-box" style="border-color:#ef4444; background:rgba(239,68,68,0.1);">
            <p style="color:#ef4444; margin:0;">${message}</p>
        </div>
    `;

    document.body.appendChild(errorDiv);
}



async function updateLinkStats(linkUrl, action) {
    if (!ENABLE_VIEW_TRACKING) {
        return;
    }

    // Check if user has already performed this action for this link
    const actionKey = `${linkUrl}-${action}`;
    if (userActions.has(actionKey)) {
        console.log(`User already performed ${action} for ${linkUrl}`);
        return;
    }

    // Find the link to get its URL
    const link = links.find(w => w.url === linkUrl);
    if (!link) {
        console.error(`Link not found with url: ${linkUrl}`);
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
        const response = await fetch(`${API_BASE_URL}/incrementView?url=${encodeURIComponent(link.url)}&action=${action}`, {
            method: 'POST'
        });

        if (response.ok) {
            const result = await response.json();
            console.log(`Synced ${action} for ${linkUrl}:`, result);

            // Update UI with actual server response (in case there were any server-side adjustments)
            updateStatsDisplay(result);

        } else {
            console.error(`Failed to sync ${action} for ${linkUrl}:`, response.status);
            // Optionally revert the optimistic update on error
            // For now, we'll keep the optimistic update for better UX
        }
    } catch (error) {
        console.error(`Failed to sync ${action} for ${linkUrl}:`, error);
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

function likesDesktopLink() {
    if (currentLinkUrl) {
        updateLinkStats(currentLinkUrl, 'likesDesktop');
    } else {
        console.error('No current link ID available for likesDesktop action');
    }
}

function dislikesDesktopLink() {
    if (currentLinkUrl) {
        updateLinkStats(currentLinkUrl, 'dislikesDesktop');
    } else {
        console.error('No current link ID available for dislikesDesktop action');
    }
}

function loadRandomLink() {
    console.log('loadRandomLink called');
    console.log('isLoading:', isLoading);
    console.log('links.length:', links.length);
    console.log('visitedLinks:', visitedLinks);

    if (isLoading || links.length === 0) {
        console.log('Cannot load random link - loading or no links');
        return;
    }

    // Get a random link that hasn't been visited yet
    const unvisitedLinks = links.filter((_, index) => !visitedLinks.includes(index));
    console.log('unvisitedLinks.length:', unvisitedLinks.length);

    if (unvisitedLinks.length === 0) {
        // All links have been visited, reset
        console.log('All links visited, resetting...');
        visitedLinks = [];
        linkHistory = [];
        currentIndex = -1;
        setTimeout(loadRandomLink, RESET_DELAY);
        return;
    }

    const randomIndex = Math.floor(Math.random() * unvisitedLinks.length);
    const link = unvisitedLinks[randomIndex];
    const originalIndex = links.indexOf(link);

    console.log('Selected random link:', link.name, 'at index:', originalIndex);

    loadLink(originalIndex);
}

function loadNextLink() {
    if (linkHistory.length === 0) {
        loadRandomLink();
        return;
    }

    if (currentIndex < linkHistory.length - 1) {
        currentIndex++;
        const linkIndex = linkHistory[currentIndex];
        loadLink(linkIndex, false);
    } else {
        loadRandomLink();
    }
}

function loadPreviousLink() {
    if (linkHistory.length === 0 || currentIndex <= 0) {
        return;
    }

    currentIndex--;
    const linkIndex = linkHistory[currentIndex];
    loadLink(linkIndex, false);
}

function loadLink(index, addToHistory = true) {
    console.log('loadLink called with index:', index, 'addToHistory:', addToHistory);

    // Validate index and link
    if (index < 0 || index >= links.length) {
        console.error('Invalid link index:', index);
        return;
    }

    const link = links[index];
    if (!link) {
        console.error('Link not found at index:', index);
        return;
    }

    console.log('Link to load:', link);

    if (addToHistory) {
        // Add to history if it's a new link
        if (currentIndex < linkHistory.length - 1) {
            // Remove any forward history if we're going back and then to a new site
            linkHistory = linkHistory.slice(0, currentIndex + 1);
        }
        linkHistory.push(index);
        currentIndex = linkHistory.length - 1;
    }

    // Mark as visited
    if (!visitedLinks.includes(index)) {
        visitedLinks.push(index);
    }

    // Track the current link url for stats
    currentLinkUrl = link.url;

    // Update UI first
    updateCurrentSiteInfo(link);

    // Optimistically increment views immediately
    if (link.url && ENABLE_VIEW_TRACKING) {
        const viewsElement = document.getElementById('views-count');
        if (viewsElement) {
            const currentViews = parseInt(viewsElement.textContent) || 0;
            viewsElement.textContent = currentViews + 1;

            // Sync with server in the background
            updateLinkStats(link.url, 'view');
        }
    }

    // Open the link in a new window/tab
    let urlToOpen = link.url;
    if (!/^https?:\/\//i.test(urlToOpen)) {
        urlToOpen = 'https://' + urlToOpen;
    }

    console.log('Opening link:', urlToOpen);
    window.open(urlToOpen, '_blank');
}

function updateCurrentSiteInfo(link) {
    const linkElement = document.getElementById('current-site-link');
    const statsDiv = document.getElementById('link-stats');
    const likesDesktopBtn = document.getElementById('likesDesktop-btn');
    const dislikesDesktopBtn = document.getElementById('dislikesDesktop-btn');

    // Check if link element exists before accessing its properties
    if (link) {
        link.href = link.url;
        link.textContent = `${link.name} - ${link.url}`;
    } else {
        console.error('current-site-link element not found');
        return;
    }

    // Add description as a separate element
    const description = document.getElementById('link-description');
    if (description) {
        description.textContent = link.description;
    }
    // Add tags as a separate element
    const tags = document.getElementById('link-tags');
    tags.textContent = `Tags: [${link.tags.join(', ')}]`;


    // Show stats and reset button states
    if (statsDiv) {
        statsDiv.style.display = 'block';
    }

    // Reset button states
    if (likesDesktopBtn) likesDesktopBtn.classList.remove('likesDesktopd');
    if (dislikesDesktopBtn) dislikesDesktopBtn.classList.remove('dislikesDesktopd');

    // Update stats with current data (force update for initial load)
    updateStatsDisplay({
        views: link.views || 0,
        likesDesktop: link.likesDesktop || 0,
        dislikesDesktop: link.dislikesDesktop || 0
    }, true);
}

// Add Link Modal Functions
function showAddLinkForm() {
    const modal = document.getElementById('add-link-modal');
    modal.style.display = 'flex';

    // Clear any existing error messages
    hideModalError();

    // Clear any existing tags
    selectedTags = [];

    // Add custom validation listeners
    addCustomValidation();

    // Focus on first input
    setTimeout(() => {
        document.getElementById('link-name').focus();
    }, FOCUS_DELAY);
}

function addCustomValidation() {
    const form = document.getElementById('add-link-form');
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

function hideAddLinkForm() {
    const modal = document.getElementById('add-link-modal');
    modal.style.display = 'none';

    // Reset form
    document.getElementById('add-link-form').reset();

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

async function submitLink(event) {
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

    const tagsInput = document.getElementById('add-link-tags');
    selectedTags = tagsInput.value.split(",")
        .map(tag => tag.trim().replace(/\s+/g, "")) // remove internal spaces
        .filter(tag => tag.length > 0);

    try {
        const formData = new FormData(event.target);
        const linkData = {
            name: formData.get('name'),
            url: normalizeUrl(formData.get('url')),
            description: formData.get('description'),
            tags: selectedTags.length > 0 ? selectedTags : ['user-submitted'],
            views: 0,
            likesDesktop: 0,
            dislikesDesktop: 0
        };

        const response = await fetch(`${API_BASE_URL}/addlink`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(linkData)
        });

        const result = await response.json();

        if (response.ok) {
            // Success
            showSuccessMessage('Link submitted for spam review successfully! The link will be live globally after review approval 🎉');
            hideAddLinkForm();

            // Reset tags
            selectedTags = [];
        } else {
            // Handle specific error cases
            if (response.status === 409) {
                showModalError('This link already exists in the database. Please try a different URL.');
            } else {
                showModalError(result.error || 'Failed to add link');
            }
        }

    } catch (error) {
        console.error('Error submitting link:', error);
        showModalError('Failed to add link. Please try again.');
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
    boxDiv.className = 'link-box success-box';

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
    const modal = document.getElementById('add-link-modal');
    if (event.target === modal) {
        hideAddLinkForm();
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
            loadNextLink();
            break;
        case 'ArrowLeft':
            event.preventDefault();
            loadPreviousLink();
            break;
        case ' ':
            event.preventDefault();
            loadRandomLink();
            break;
        case 'Escape':
            const modal = document.getElementById('add-link-modal');
            if (modal.style.display === 'flex') {
                hideAddLinkForm();
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

    // ✅ Wait for API and get link count
    const linkCount = await loadLinksFromAPI(tagsAllowlist, tagsBlocklist);

    if (tagsAllowlist.length > 0 || tagsBlocklist.length > 0) {
        successBox.textContent = `✅ Filter applied: ${linkCount} link${linkCount !== 1 ? 's' : ''} found`;
    } else {
        successBox.textContent = `✅ Showing all ${linkCount} link${linkCount !== 1 ? 's' : ''}`;
    }

    successBox.style.display = 'inline-block';

    // Auto-hide after 3 seconds
    setTimeout(() => {
        successBox.style.display = 'none';
    }, 3000);
}

