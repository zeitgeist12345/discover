/* Copyright (c) 2025 Mohammad Sheraj */
/* Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details. */

// tools/recreate-files.js
import LINKS_TO_KEEP from './links-dump-to-keep.json' assert { type: 'json' };
import { fileURLToPath } from 'url';
import path from 'path';
import fs from 'fs';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);



const PROJECT_ROOT = path.join(__dirname, '..');

// --- Helpers ---
function escapeSQL(str = '') {
    return str.replace(/'/g, "''");
}

function escapeKotlin(str = '') {
    return str.replace(/"/g, '\\"');
}

function formatTagsForSQL(tags) {
    if (!tags || !Array.isArray(tags) || tags.length === 0) {
        return "JSON_ARRAY()";
    }

    const escapedTags = tags.map(tag => {
        return `'${escapeSQL(tag)}'`;
    });

    return `JSON_ARRAY(${escapedTags.join(', ')})`;
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
function generateInitSQL(links) {
    const sqlPath = path.join(PROJECT_ROOT, 'backend', 'db', 'init.sql');
    console.log(`🧱 Writing SQL init file to ${sqlPath}...`);

    const header = `-- Copyright (c) 2025 Mohammad Sheraj
-- Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details.

CREATE TABLE IF NOT EXISTS visitors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    country VARCHAR(10),
    user_agent VARCHAR(1000),
    origin VARCHAR(500),
    platform VARCHAR(50),
    path VARCHAR(255),
    product VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS errors (
    id INT PRIMARY KEY AUTO_INCREMENT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    source TEXT,
    level TEXT,
    message TEXT NOT NULL,
    user_agent TEXT,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP NULL
);
CREATE TABLE IF NOT EXISTS links (
    url VARCHAR(500) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tags JSON DEFAULT (JSON_ARRAY()),
    views INT DEFAULT 0,
    likesMobile INT DEFAULT 0,
    dislikesMobile INT DEFAULT 0,
    likesDesktop INT DEFAULT 0,
    dislikesDesktop INT DEFAULT 0,
    reviewStatus INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO links (
        name,
        url,
        description,
        tags,
        views,
        likesMobile,
        dislikesMobile,
        likesDesktop,
        dislikesDesktop,
        reviewStatus
    )
VALUES `;

    const values = links.map(site => {
        const name = escapeSQL(site.name || '');
        const url = escapeSQL(site.url || '');
        const description = escapeSQL(site.description || '');
        const tags = formatTagsForSQL(site.tags);
        const views = site.views ?? 30;
        const likesMobile = site.likesMobile ?? 2;
        const dislikesMobile = site.dislikesMobile ?? 0;
        const likesDesktop = site.likesDesktop ?? 1;
        const dislikesDesktop = site.dislikesDesktop ?? 0;
        const reviewStatus = site.reviewStatus ?? 0

        return `(
        '${name}',
        '${url}',
        '${description}',
        ${tags},
        ${views},
        ${likesMobile},
        ${dislikesMobile},
        ${likesDesktop},
        ${dislikesDesktop},
        ${reviewStatus}
    )`;
    }).join(',\n    ');

    const content = `${header}${values} ON DUPLICATE KEY
UPDATE url = url;`;
    fs.mkdirSync(path.dirname(sqlPath), { recursive: true });
    fs.writeFileSync(sqlPath, content);
    console.log(`✅ init.sql regenerated (${links.length} entries)\n`);
}

// 2️⃣ Generate static.js
function generateConfigJS(links) {
    const configPath = path.join(PROJECT_ROOT, 'static.js');
    console.log(`⚙️ Writing web config file to ${configPath}...`);

    const sampleLinks = links
        .filter(site => !needToIgnore(site.likesDesktop, site.dislikesDesktop))
        // .filter(site => site.tags && site.tags.includes('positive'))
        .map((site, index) => ({
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
    const jsonIndented = JSON.stringify(sampleLinks, null, 4)
        .split('\n')
        .map((line, index) => (index < 1 ? line : '    ' + line)) // Skip first 4
        .join('\n');
    const js = `/* Copyright (c) 2025 Mohammad Sheraj */
/* Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details. */

// static.js
const STATIC = {
    // Auto-generated sample links
    SAMPLE_LINKS: ${jsonIndented}
};

if (typeof module !== 'undefined') {
    module.exports = { STATIC };
}
`;

    fs.writeFileSync(configPath, js.trim() + '\n');
    console.log(`✅ static.js regenerated (${sampleLinks.length} sample sites)\n`);
}

// 3️⃣ Generate StaticLinks.kt
function generateKotlin(links) {
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
        'StaticLinks.kt'
    );
    console.log(`🤖 Writing Kotlin static links to ${kotlinPath}...`);

    const kotlinEntries = links
        .filter(site => !needToIgnore(site.likesMobile, site.dislikesMobile))
        // .filter(site => site.tags && site.tags.includes('positive') && site.tags.includes('daily'))
        // .filter(site => site.tags && !site.tags.includes('optional'))
        .map((site, i) => {
            const name = escapeKotlin(site.name || '');
            const url = escapeKotlin(site.url || '');
            const desc = escapeKotlin(site.description || '');
            const tags = site.tags || [];
            const views = site.views ?? 30 + i;
            const likesMobile = site.likesMobile ?? 2;
            const dislikesMobile = site.dislikesMobile ?? 0;

            return `        Link(
            name = "${name}",
            url = "${url}",
            description = "${desc}",
            tags = listOf(${tags.map(tag => `"${escapeKotlin(tag)}"`).join(', ')}),
            views = ${views},
            likesMobile = ${likesMobile},
            dislikesMobile = ${dislikesMobile}
        )`;
        }).join(',\n\n');

    const kotlinCode = `/* Copyright (c) 2025 Mohammad Sheraj */
/* Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details. */

package com.example.discover.data

object StaticLinks {
    val links = listOf(
${kotlinEntries}
    )
}
`;

    fs.mkdirSync(path.dirname(kotlinPath), { recursive: true });
    fs.writeFileSync(kotlinPath, kotlinCode.trim() + '\n');
    console.log(`✅ StaticLinks.kt regenerated (${links.length} entries)\n`);
}

// 🧩 Main generator
function main() {
    console.log('🚀 Starting regeneration of static link files...\n');
    if (!Array.isArray(LINKS_TO_KEEP) || LINKS_TO_KEEP.length === 0) {
        console.error('❌ No links found in static-links.js');
        process.exit(1);
    }

    console.log(`📦 Found ${LINKS_TO_KEEP.length} links in static-links.js`);
    const approvedLinks = LINKS_TO_KEEP.filter(site => site.reviewStatus === 1);
    approvedLinks.sort((a, b) => (a.url || '').localeCompare(b.url || ''));
    console.log(`📦 Found ${approvedLinks.length} approved links in static-links.js`);

    generateInitSQL(approvedLinks);
    generateConfigJS(approvedLinks);
    generateKotlin(approvedLinks);
    console.log('🎉 All files successfully regenerated!\n');
}

main();
