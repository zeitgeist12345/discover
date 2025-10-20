const fs = require('fs');
const path = require('path');

// Configuration
const API_BASE_URL = 'https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api';
const OUTPUT_FILE = path.join(__dirname, 'websites-dump.json');

// Content filtering algorithm (same as in getWebsites function)
function applyContentFilter(website) {
    const likes = website.likes || 0;
    const dislikes = website.dislikes || 0;
    const total = likes + dislikes;
    
    // If less votes, consider it okay
    if (total <= 3) {
        return { isFiltered: false, reason: 'Total votes ≤ 3' };
    }
    
    const undesirable_score = dislikes / Math.max(total, 1);
    
    if (undesirable_score >= 0.8) {
        return { 
            isFiltered: true, 
            reason: `Undesirable score ≥ 0.8 (${(undesirable_score * 100).toFixed(1)}%)` 
        };
    }
    
    return { 
        isFiltered: false, 
        reason: `Undesirable score < 0.8 (${(undesirable_score * 100).toFixed(1)}%)` 
    };
}

async function fetchWebsites() {
    try {
        console.log('🔍 Fetching websites from API...');
        
        const response = await fetch(`${API_BASE_URL}/getwebsites`);
        
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

async function fetchAllWebsitesFromDB() {
    try {
        console.log('🔍 Fetching ALL websites from database (including filtered ones)...');
        
        // We need to make a direct request to get all websites without filtering
        // For now, we'll use the same endpoint but we'll need to modify the getWebsites function
        // to accept a query parameter to bypass filtering
        
        const response = await fetch(`${API_BASE_URL}/getwebsites?all=true`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const allWebsites = await response.json();
        console.log(`✅ Fetched ${allWebsites.length} total websites from database`);
        
        return allWebsites;
    } catch (error) {
        console.error('❌ Error fetching all websites:', error.message);
        throw error;
    }
}

function analyzeWebsites(websites) {
    console.log('📊 Analyzing websites...');
    
    const analysis = {
        total: websites.length,
        filtered: 0,
        shown: 0,
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
        const likes = website.likes || 0;
        const dislikes = website.dislikes || 0;
        const total = likes + dislikes;
        const score = total > 0 ? (dislikes / total) * 100 : 0;
        
        // Apply filtering logic
        const filterResult = applyContentFilter(website);
        website.filterAnalysis = filterResult;
        
        if (filterResult.isFiltered) {
            analysis.filtered++;
        } else {
            analysis.shown++;
        }
        
        // Count by reason
        analysis.byReason[filterResult.reason] = (analysis.byReason[filterResult.reason] || 0) + 1;
        
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

function saveToFile(data) {
    try {
        // Save raw website data only
        fs.writeFileSync(OUTPUT_FILE, JSON.stringify(data.websites, null, 2));
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

function printSummary(analysis) {
    console.log('\n📊 WEBSITES ANALYSIS SUMMARY');
    console.log('=' .repeat(50));
    console.log(`Total Websites: ${analysis.total}`);
    console.log(`✅ Shown: ${analysis.shown}`);
    console.log(`❌ Filtered: ${analysis.filtered}`);
    console.log(`📈 Filter Rate: ${((analysis.filtered / analysis.total) * 100).toFixed(1)}%`);
    
    console.log('\n🔍 Filtering Reasons:');
    Object.entries(analysis.byReason).forEach(([reason, count]) => {
        console.log(`  ${reason}: ${count} sites`);
    });
    
    console.log('\n📊 Distribution by Dislike Score:');
    Object.entries(analysis.byScore).forEach(([range, count]) => {
        console.log(`  ${range}: ${count} sites`);
    });
    
    console.log('\n📊 Distribution by Vote Count:');
    Object.entries(analysis.byVoteCount).forEach(([range, count]) => {
        console.log(`  ${range}: ${count} sites`);
    });
}

async function main() {
    try {
        console.log('🚀 Starting website data dump...\n');
        
        // First, try to get all websites (including filtered ones)
        let allWebsites;
        try {
            allWebsites = await fetchAllWebsitesFromDB();
        } catch (error) {
            console.log('⚠️  Could not fetch all websites, falling back to filtered websites only...');
            allWebsites = await fetchWebsites();
        }
        
        // Analyze the websites
        const analysis = analyzeWebsites(allWebsites);
        
        // Print summary
        printSummary(analysis);
        
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
    fetchAllWebsitesFromDB,
    analyzeWebsites,
    applyContentFilter
}; 