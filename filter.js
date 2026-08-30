function getFilterList() {
    const tagsInputAllowlist = document.getElementById('filter-tags-allowlist');
    const tagsInputBlocklist = document.getElementById('filter-tags-blocklist');
    const urlsInputAllowlist = document.getElementById('filter-urls-allowlist');
    const urlsInputBlocklist = document.getElementById('filter-urls-blocklist');

    const tagsAllowlist = tagsInputAllowlist.value
        .split(',')
        .map(t => t.trim())
        .filter(t => t.length > 0);
    const tagsBlocklist = tagsInputBlocklist.value
        .split(',')
        .map(t => t.trim())
        .filter(t => t.length > 0);

    const urlsAllowlist = urlsInputAllowlist.value
        .split(' ')
        .map(t => t.trim())
        .filter(t => t.length > 0);
    const urlsBlocklist = urlsInputBlocklist.value
        .split(' ')
        .map(t => t.trim())
        .filter(t => t.length > 0);

    saveSettings();

    return { tagsAllowlist, tagsBlocklist, urlsAllowlist, urlsBlocklist };
}

function resetFilterListToDefaults() {
    document.getElementById('filter-tags-allowlist').value = 'positive';
    document.getElementById('filter-tags-blocklist').value = '';
    document.getElementById('filter-urls-allowlist').value = '';
    document.getElementById('filter-urls-blocklist').value = '';
    saveSettings();
    loadLinksFromAPI();
}

function saveSettings() {
    try {
        localStorage.setItem('discover-settings', JSON.stringify({
            tagsAllowlist: document.getElementById('filter-tags-allowlist').value,
            tagsBlocklist: document.getElementById('filter-tags-blocklist').value,
            urlsAllowlist: document.getElementById('filter-urls-allowlist').value,
            urlsBlocklist: document.getElementById('filter-urls-blocklist').value
        }));
    } catch (e) {
        console.warn('Could not save settings', e);
    }
}

function loadSettings() {
    try {
        const saved = localStorage.getItem('discover-settings');
        if (!saved) return;
        const settings = JSON.parse(saved);
        document.getElementById('filter-tags-allowlist').value = settings.tagsAllowlist || 'positive';
        document.getElementById('filter-tags-blocklist').value = settings.tagsBlocklist || '';
        document.getElementById('filter-urls-allowlist').value = settings.urlsAllowlist || '';
        document.getElementById('filter-urls-blocklist').value = settings.urlsBlocklist || '';
    } catch (e) {
        console.warn('Could not load settings', e);
    }
}