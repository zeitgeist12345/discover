/* Copyright (c) 2025 Mohammad Sheraj Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details. */

const fs = require('fs');
const path = require('path');

// Configuration
const API_BASE_URL = 'https://backend.discoverall.space';
const OUTPUT_FILE = path.join(__dirname, 'links-dump.json');

async function fetchLinks() {
    try {
        console.log('🔍 Fetching links from API...');

        const response = await fetch(`${API_BASE_URL}/getLinks?reviewStatusEnable=1`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const links = await response.json();
        console.log(`✅ Fetched ${links.length} links from API`);

        return links;
    } catch (error) {
        console.error('❌ Error fetching links:', error.message);
        throw error;
    }
}



// Filtering criteria
function needToIgnore(likesMobile, dislikesMobile) {
    const total = likesMobile + dislikesMobile;
    // If less votes, consider it okay
    if (total <= 3) {
        return false;
    }
    const undesirable_score = dislikesMobile / Math.max(total, 1);

    return undesirable_score > 0.8;
}

function analyzeLinks(links) {
    console.log('📊 Analyzing links...');

    const analysis = {
        total: links.length,
        filteredMobile: 0,
        filteredDesktop: 0,
        blockedMobileUrls: [],
        blockedDesktopUrls: [],
        byReason: {},
        byScore: {
            '0%': 0,
            '1-25%': 0,
            '26-50%': 0,
            '51-75%': 0,
            '76-79%': 0,
            '80-100%': 0
        },
        byVoteCount: {
            '0 votes': 0,
            '1-3 votes': 0,
            '4-10 votes': 0,
            '11-50 votes': 0,
            '50+ votes': 0
        }
    };

    links.forEach(link => {
        const likesMobile = link.likesMobile || 0;
        const dislikesMobile = link.dislikesMobile || 0;
        const likesDesktop = link.likesDesktop || 0;
        const dislikesDesktop = link.dislikesDesktop || 0;

        const total = likesMobile + dislikesMobile;
        const score = total > 0 ? (dislikesMobile / total) * 100 : 0;

        // Apply filtering logic
        if (needToIgnore(likesMobile, dislikesMobile)) {
            analysis.filteredMobile++;
            analysis.blockedMobileUrls.push(link.url);
        }
        if (needToIgnore(likesDesktop, dislikesDesktop)) {
            analysis.filteredDesktop++;
            analysis.blockedDesktopUrls.push(link.url);
        }

        // Count by score
        if (score === 0) analysis.byScore['0%']++;
        else if (score <= 25) analysis.byScore['1-25%']++;
        else if (score <= 50) analysis.byScore['26-50%']++;
        else if (score <= 75) analysis.byScore['51-75%']++;
        else if (score < 80) analysis.byScore['76-79%']++;
        else analysis.byScore['80-100%']++;

        // Count by vote count
        if (total === 0) analysis.byVoteCount['0 votes']++;
        else if (total <= 3) analysis.byVoteCount['1-3 votes']++;
        else if (total <= 10) analysis.byVoteCount['4-10 votes']++;
        else if (total <= 50) analysis.byVoteCount['11-50 votes']++;
        else analysis.byVoteCount['50+ votes']++;
    });

    return analysis;
}

// Simple URL fixing function
function fixUrl(url) {
    let fixedUrl = url.trim();
    // Remove any extra whitespace
    fixedUrl = fixedUrl.replace(/\s+/g, '');
    // Check if URL has a protocol, add https:// if not
    if (!fixedUrl.match(/^https?:\/\//i)) {
        fixedUrl = 'https://' + fixedUrl;
    }

    try {
        // Use URL constructor to validate
        const urlObj = new URL(fixedUrl);

        // Ensure it's a valid web URL (http or https)
        if (!['http:', 'https:'].includes(urlObj.protocol)) {
            throw new Error('Invalid protocol');
        }

        return urlObj.href;
    } catch (error) {
        throw new Error('Invalid URL format');
    }
}

async function fetchVisitorsAnalytics() {
    try {
        const response = await fetch(`${API_BASE_URL}/visitors-analytics`);
        if (response.ok) {
            return await response.json();
        }
        return {};
    } catch (error) {
        console.warn('⚠️ Could not fetch visitors analytics:', error.message);
        return {};
    }
}

async function saveToFile(data) {
    try {
        // 🔥 Remove created_at from each link object
        const cleanedLinks = data.links.map(({ created_at, ...rest }) => ({
            ...rest,
            url: fixUrl(rest.url) // Fix the URL here
        }));

        // Save raw link data only
        fs.writeFileSync(OUTPUT_FILE, JSON.stringify(cleanedLinks, null, 2));
        console.log(`💾 Raw data saved to: ${OUTPUT_FILE}`);

        // Get errors from API
        let errorsData = { count: 0, unresolved: 0, recent: [] };
        try {
            const response = await fetch(`${API_BASE_URL}/errors?limit=1000`);
            if (response.ok) {
                const errors = await response.json();
                errorsData = {
                    count: errors.length,
                    unresolved: errors.filter(e => !e.resolved).length,
                    recent: errors.slice(0, 10)
                };
            }
        } catch (error) {
            console.warn('⚠️ Could not fetch errors:', error.message);
        }

        // Get visitors analytics
        const visitorsAnalytics = await fetchVisitorsAnalytics();

        // Save summary WITH errors data
        const summaryFile = path.join(__dirname, 'links-summary.json');
        fs.writeFileSync(summaryFile, JSON.stringify({
            metadata: {
                generatedAt: new Date().toISOString(),
                apiUrl: API_BASE_URL,
                totalLinks: data.links.length
            },
            analysis: data.analysis,
            visitors: visitorsAnalytics,
            errors: errorsData
        }, null, 2));
        console.log(`📋 Summary saved to: ${summaryFile}`);

    } catch (error) {
        console.error('❌ Error saving to file:', error.message);
        throw error;
    }
}

async function main() {
    try {
        console.log('🚀 Starting link data dump...\n');

        // First, try to get all links (including filtered ones)
        let allLinks = await fetchLinks();

        allLinks.sort((a, b) => (a.url || 0) - (b.url || 0));

        // Analyze the links
        const analysis = analyzeLinks(allLinks);

        // 🔥 Sort blocked lists alphabetically by URL
        analysis.blockedMobileUrls.sort();
        analysis.blockedDesktopUrls.sort();

        // Save to file
        await saveToFile({ links: allLinks, analysis });

        console.log('\n✅ Link data dump completed successfully!');

    } catch (error) {
        console.error('\n❌ Error in main process:', error.message);
        process.exit(1);
    }
}

// Run the script
if (require.main === module) {
    main();
}

module.exports = {
    fetchLinks,
    analyzeLinks,
    needToIgnore
}; 