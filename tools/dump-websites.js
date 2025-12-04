const fs = require('fs');
const path = require('path');

// Configuration
const API_BASE_URL = 'https://backend.discoverall.space';
const OUTPUT_FILE = path.join(__dirname, 'websites-dump.json');

async function fetchWebsites() {
    try {
        console.log('🔍 Fetching websites from API...');

        const response = await fetch(`${API_BASE_URL}/getWebsites?reviewStatusEnable=1`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const websites = await response.json();
        console.log(`✅ Fetched ${websites.length} websites from API`);

        return websites;
    } catch (error) {
        console.error('❌ Error fetching websites:', error.message);
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

function analyzeWebsites(websites) {
    console.log('📊 Analyzing websites...');

    const analysis = {
        total: websites.length,
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

    websites.forEach(website => {
        const likesMobile = website.likesMobile || 0;
        const dislikesMobile = website.dislikesMobile || 0;
        const likesDesktop = website.likesDesktop || 0;
        const dislikesDesktop = website.dislikesDesktop || 0;

        const total = likesMobile + dislikesMobile;
        const score = total > 0 ? (dislikesMobile / total) * 100 : 0;

        // Apply filtering logic
        if (needToIgnore(likesMobile, dislikesMobile)) {
            analysis.filteredMobile++;
            analysis.blockedMobileUrls.push(website.url);
        }
        if (needToIgnore(likesDesktop, dislikesDesktop)) {
            analysis.filteredDesktop++;
            analysis.blockedDesktopUrls.push(website.url);
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

function saveToFile(data) {
    try {
        // 🔥 Remove created_at from each website object
        const cleanedWebsites = data.websites.map(({ created_at, ...rest }) => ({
            ...rest,
            url: fixUrl(rest.url) // Fix the URL here
        }));

        // Save raw website data only
        fs.writeFileSync(OUTPUT_FILE, JSON.stringify(cleanedWebsites, null, 2));
        console.log(`💾 Raw data saved to: ${OUTPUT_FILE}`);

        // Save comprehensive summary with metadata
        const summaryFile = path.join(__dirname, 'websites-summary.json');
        fs.writeFileSync(summaryFile, JSON.stringify({
            metadata: {
                generatedAt: new Date().toISOString(),
                apiUrl: API_BASE_URL,
                totalWebsites: data.websites.length
            },
            analysis: data.analysis
        }, null, 2));
        console.log(`📋 Summary saved to: ${summaryFile}`);

    } catch (error) {
        console.error('❌ Error saving to file:', error.message);
        throw error;
    }
}

async function main() {
    try {
        console.log('🚀 Starting website data dump...\n');

        // First, try to get all websites (including filtered ones)
        let allWebsites = await fetchWebsites();

        allWebsites.sort((a, b) => (a.url || 0) - (b.url || 0));

        // Analyze the websites
        const analysis = analyzeWebsites(allWebsites);

        // 🔥 Sort blocked lists alphabetically by URL
        analysis.blockedMobileUrls.sort();
        analysis.blockedDesktopUrls.sort();

        // Save to file
        saveToFile({ websites: allWebsites, analysis });

        console.log('\n✅ Website data dump completed successfully!');

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
    fetchWebsites,
    analyzeWebsites,
    needToIgnore
}; 