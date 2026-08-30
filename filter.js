let individualBlockedUrls = [];

function getFilterList() {
    const mode = document.querySelector('input[name="filter-mode"]:checked')?.value || 'lists';

    if (mode === 'individual') {
        return {
            tagsAllowlist: [],
            tagsBlocklist: [],
            urlsAllowlist: [],
            urlsBlocklist: individualBlockedUrls
        };
    }

    // lists mode
    const tagsInputAllowlist = document.getElementById('filter-tags-allowlist');
    const tagsInputBlocklist = document.getElementById('filter-tags-blocklist');
    const urlsInputAllowlist = document.getElementById('filter-urls-allowlist');
    const urlsInputBlocklist = document.getElementById('filter-urls-blocklist');

    const tagsAllowlist = tagsInputAllowlist.value.split(',').map(t => t.trim()).filter(Boolean);
    const tagsBlocklist = tagsInputBlocklist.value.split(',').map(t => t.trim()).filter(Boolean);
    const urlsAllowlist = urlsInputAllowlist.value.split(' ').map(t => t.trim()).filter(Boolean);
    const urlsBlocklist = urlsInputBlocklist.value.split(' ').map(t => t.trim()).filter(Boolean);

    saveSettings();   // persist current field values
    return { tagsAllowlist, tagsBlocklist, urlsAllowlist, urlsBlocklist };
}

async function loadIndividualFilter() {
    const container = document.getElementById('individual-link-list');
    if (!container) return;

    try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), API_TIMEOUT);
        const response = await fetch(`${API_BASE_URL}/getLinks?reviewStatusEnable=1`, { signal: controller.signal });
        clearTimeout(timeoutId);

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

        const allLinks = await response.json();
        allLinks.sort((a, b) => (a.name || '').localeCompare(b.name || ''));

        const blockedSet = new Set(individualBlockedUrls);
        container.innerHTML = '';

        // Count line
        const count = document.createElement('p');
        count.className = 'individual-count';
        count.textContent = `${allLinks.length - individualBlockedUrls.length} of ${allLinks.length} links allowed`;
        container.appendChild(count);

        // Select all row
        const selectAllRow = document.createElement('div');
        selectAllRow.className = 'individual-link-row select-all-row';

        const selectAllLabel = document.createElement('label');
        selectAllLabel.className = 'individual-link-label';

        const selectAllCheckbox = document.createElement('input');
        selectAllCheckbox.type = 'checkbox';
        selectAllCheckbox.id = 'select-all-checkbox';
        selectAllCheckbox.checked = true;

        const selectAllText = document.createElement('span');
        selectAllText.textContent = 'Select All';
        selectAllText.style.color = '#fff';

        selectAllLabel.appendChild(selectAllCheckbox);
        selectAllLabel.appendChild(selectAllText);
        selectAllRow.appendChild(selectAllLabel);
        container.appendChild(selectAllRow);

        const linkCheckboxes = [];

        allLinks.forEach(link => {
            const row = document.createElement('div');
            row.className = 'individual-link-row';

            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.checked = !blockedSet.has(link.url);
            linkCheckboxes.push(checkbox);

            checkbox.addEventListener('change', () => {
                if (checkbox.checked) {
                    individualBlockedUrls = individualBlockedUrls.filter(u => u !== link.url);
                } else {
                    if (!individualBlockedUrls.includes(link.url)) {
                        individualBlockedUrls.push(link.url);
                    }
                }
                saveSettings();
                loadLinksFromAPI();
                updateSelectAllState();
            });

            const label = document.createElement('label');
            label.className = 'individual-link-label';
            label.appendChild(checkbox);

            const info = document.createElement('div');
            info.className = 'individual-link-info';

            const name = document.createElement('span');
            name.className = 'individual-link-name';
            name.textContent = link.name || 'Untitled';

            const url = document.createElement('a');
            url.className = 'individual-link-url';
            url.href = link.url;
            url.target = '_blank';
            url.rel = 'noopener noreferrer';
            url.textContent = `(${link.url})`;

            info.appendChild(name);
            info.appendChild(document.createTextNode(' '));
            info.appendChild(url);

            label.appendChild(info);

            row.appendChild(label);
            container.appendChild(row);
        });

        function updateSelectAllState() {
            if (linkCheckboxes.length === 0) return;
            const total = linkCheckboxes.length;
            const checkedCount = linkCheckboxes.filter(cb => cb.checked).length;

            if (checkedCount === total) {
                selectAllCheckbox.checked = true;
                selectAllCheckbox.indeterminate = false;
            } else if (checkedCount === 0) {
                selectAllCheckbox.checked = false;
                selectAllCheckbox.indeterminate = false;
            } else {
                selectAllCheckbox.checked = false;
                selectAllCheckbox.indeterminate = true;
            }

            count.textContent = `${checkedCount} of ${total} links allowed`;
        }

        selectAllCheckbox.addEventListener('change', () => {
            const checked = selectAllCheckbox.checked;
            linkCheckboxes.forEach(cb => { cb.checked = checked; });

            if (checked) {
                individualBlockedUrls = [];
            } else {
                individualBlockedUrls = allLinks.map(l => l.url);
            }

            saveSettings();
            loadLinksFromAPI();
            updateSelectAllState();
        });

        updateSelectAllState();
    } catch (error) {
        container.innerHTML = '<p style="color:#f87171;">Failed to load links. Please try again.</p>';
        console.error('Failed to load individual filter links:', error);
    }
}

function onFilterModeChange() {
    const mode = document.querySelector('input[name="filter-mode"]:checked')?.value || 'lists';
    document.getElementById('lists-filter-section').style.display = mode === 'lists' ? 'block' : 'none';
    document.getElementById('individual-filter-section').style.display = mode === 'individual' ? 'block' : 'none';
    saveSettings();
    if (mode === 'individual') {
        loadIndividualFilter();
    }
    loadLinksFromAPI();
}

function resetFilterToDefaults() {
    // Reset lists fields
    document.getElementById('filter-tags-allowlist').value = 'positive';
    document.getElementById('filter-tags-blocklist').value = '';
    document.getElementById('filter-urls-allowlist').value = '';
    document.getElementById('filter-urls-blocklist').value = '';

    // Reset mode to lists
    const listsRadio = document.querySelector('input[name="filter-mode"][value="lists"]');
    if (listsRadio) listsRadio.checked = true;

    // Reset individual blocked URLs (in‑memory and storage)
    individualBlockedUrls = [];

    saveSettings();
    loadLinksFromAPI();
}

function saveSettings() {
    try {
        const mode = document.querySelector('input[name="filter-mode"]:checked')?.value || 'lists';
        localStorage.setItem('discover-settings', JSON.stringify({
            filterMode: mode,
            lists: {
                tagsAllowlist: document.getElementById('filter-tags-allowlist').value,
                tagsBlocklist: document.getElementById('filter-tags-blocklist').value,
                urlsAllowlist: document.getElementById('filter-urls-allowlist').value,
                urlsBlocklist: document.getElementById('filter-urls-blocklist').value,
            },
            individual: {
                blockedUrls: individualBlockedUrls
            }
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

        // set radio
        const mode = settings.filterMode || 'lists';
        const modeRadio = document.querySelector(`input[name="filter-mode"][value="${mode}"]`);
        if (modeRadio) modeRadio.checked = true;

        // set lists fields
        const lists = settings.lists || {};
        document.getElementById('filter-tags-allowlist').value = lists.tagsAllowlist || 'positive';
        document.getElementById('filter-tags-blocklist').value = lists.tagsBlocklist || '';
        document.getElementById('filter-urls-allowlist').value = lists.urlsAllowlist || '';
        document.getElementById('filter-urls-blocklist').value = lists.urlsBlocklist || '';

        // set individual blocked urls
        individualBlockedUrls = settings.individual?.blockedUrls || [];

        onFilterModeChange();   // show/hide sections and load individual if needed
    } catch (e) {
        console.warn('Could not load settings', e);
    }
}