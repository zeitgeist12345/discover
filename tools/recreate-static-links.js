// tools/recreate-files.js
const fs = require('fs');
const path = require('path');
const { WEBSITES_TO_KEEP } = require('./static-links');

console.log('🚀 Starting regeneration of static website files...\n');

const PROJECT_ROOT = path.join(__dirname, '..');

// --- Helpers ---
function escapeSQL(str = '') {
    return str.replace(/'/g, "''");
}

function escapeKotlin(str = '') {
    return str.replace(/"/g, '\\"');
}

function formatTagsForSQL(tags) {
    if (!Array.isArray(tags)) {
        return '[]';
    }
    return JSON.stringify(tags);
}

function needToIgnore(likesMobile, dislikesMobile) {
    const total = likesMobile + dislikesMobile;
    // If less votes, consider it okay
    if (total <= 3) {
        return false;
    }

    const undesirable_score = dislikesMobile / Math.max(total, 1);

    return undesirable_score > 0.8;
}

// 1️⃣ Generate init.sql
function generateInitSQL(websites) {
    const sqlPath = path.join(PROJECT_ROOT, 'backend', 'db', 'init.sql');
    console.log(`🧱 Writing SQL init file to ${sqlPath}...`);

    const header = `CREATE TABLE IF NOT EXISTS websites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    description TEXT,
    tags JSON DEFAULT ('[]'),
    views INT DEFAULT 0,
    likesMobile INT DEFAULT 0,
    dislikesMobile INT DEFAULT 0,
    likesDesktop INT DEFAULT 0,
    dislikesDesktop INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_url (url(255))
);
INSERT INTO websites (
        name,
        url,
        description,
        tags,
        views,
        likesMobile,
        dislikesMobile,
        likesDesktop,
        dislikesDesktop
    )
VALUES `;

    const values = websites.map(site => {
        const name = escapeSQL(site.name || '');
        const url = escapeSQL(site.url || '');
        const description = escapeSQL(site.description || '');
        const tags = formatTagsForSQL(site.tags);
        const views = site.views ?? 30;
        const likesMobile = site.likesMobile ?? 2;
        const dislikesMobile = site.dislikesMobile ?? 0;
        const likesDesktop = site.likesDesktop ?? 1;
        const dislikesDesktop = site.dislikesDesktop ?? 0;

        return `(
        '${name}',
        '${url}',
        '${description}',
        '${tags}',
        ${views},
        ${likesMobile},
        ${dislikesMobile},
        ${likesDesktop},
        ${dislikesDesktop}
    )`;
    }).join(',\n    ');

    const content = `${header}${values} ON DUPLICATE KEY
UPDATE id = id;`;
    fs.mkdirSync(path.dirname(sqlPath), { recursive: true });
    fs.writeFileSync(sqlPath, content);
    console.log(`✅ init.sql regenerated (${websites.length} entries)\n`);
}

// 2️⃣ Generate config.js
function generateConfigJS(websites) {
    const configPath = path.join(PROJECT_ROOT, 'config.js');
    console.log(`⚙️ Writing web config file to ${configPath}...`);

    const sampleWebsites = websites
        .filter(site => !needToIgnore(site.likesDesktop, site.dislikesDesktop))
        .map((site, index) => ({
            id: index + 1,
            name: site.name,
            url: site.url,
            description: site.description,
            tags: site.tags || [],
            views: site.views ?? 30 + index,
            likesMobile: site.likesMobile ?? 2,
            dislikesMobile: site.dislikesMobile ?? 0,
            likesDesktop: site.likesDesktop ?? 1,
            dislikesDesktop: site.dislikesDesktop ?? 0
        }));

    const js = `// config.js
const CONFIG = {
    USE_API: true,
    API_BASE_URL: 'https://backend.discoverall.space',
    API_TIMEOUT: 3000,
    ENABLE_FALLBACK: true,
    ENABLE_VIEW_TRACKING: true,
    ERROR_MESSAGE: 'Unable to connect to the server. Please make sure your local backend is running.',

    // Auto-generated sample websites
    SAMPLE_WEBSITES: ${JSON.stringify(sampleWebsites, null, 4)}
};

if (typeof module !== 'undefined') {
    module.exports = { CONFIG };
}
`;

    fs.writeFileSync(configPath, js.trim() + '\n');
    console.log(`✅ config.js regenerated (${sampleWebsites.length} sample sites)\n`);
}

// 3️⃣ Generate StaticWebsites.kt
function generateKotlin(websites) {
    const kotlinPath = path.join(
        PROJECT_ROOT,
        'androidApp',
        'app',
        'src',
        'main',
        'java',
        'com',
        'example',
        'discover',
        'data',
        'StaticWebsites.kt'
    );
    console.log(`🤖 Writing Kotlin static websites to ${kotlinPath}...`);

    const kotlinEntries = websites
        .filter(site => !needToIgnore(site.likesMobile, site.dislikesMobile))
        .map((site, i) => {
            const id = `website-${i + 1}`;
            const name = escapeKotlin(site.name || '');
            const url = escapeKotlin(site.url || '');
            const desc = escapeKotlin(site.description || '');
            const tags = site.tags || [];
            const views = site.views ?? 30 + i;
            const likesMobile = site.likesMobile ?? 2;
            const dislikesMobile = site.dislikesMobile ?? 0;

            return `        Link(
            id = "${id}",
            name = "${name}",
            url = "${url}",
            description = "${desc}",
            tags = listOf(${tags.map(tag => `"${escapeKotlin(tag)}"`).join(', ')}),
            views = ${views},
            likesMobile = ${likesMobile},
            dislikesMobile = ${dislikesMobile}
        )`;
        }).join(',\n\n');

    const kotlinCode = `package com.example.discover.data

object StaticWebsites {
    val websites = listOf(
${kotlinEntries}
    )
}
`;

    fs.mkdirSync(path.dirname(kotlinPath), { recursive: true });
    fs.writeFileSync(kotlinPath, kotlinCode.trim() + '\n');
    console.log(`✅ StaticWebsites.kt regenerated (${websites.length} entries)\n`);
}

// 🧩 Main generator
function main() {
    if (!Array.isArray(WEBSITES_TO_KEEP) || WEBSITES_TO_KEEP.length === 0) {
        console.error('❌ No websites found in static-links.js');
        process.exit(1);
    }

    console.log(`📦 Found ${WEBSITES_TO_KEEP.length} websites in static-links.js`);
    generateInitSQL(WEBSITES_TO_KEEP);
    generateConfigJS(WEBSITES_TO_KEEP);
    generateKotlin(WEBSITES_TO_KEEP);
    console.log('🎉 All files successfully regenerated!\n');
}

main();
