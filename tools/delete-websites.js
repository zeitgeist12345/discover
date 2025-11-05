const fs = require('fs');
const path = require('path');
const readline = require('readline');

// Configuration
const API_BASE_URL = 'https://backend.discoverall.space';

// Import the static sites data
const { WEBSITES_TO_KEEP } = require('./static-sites.js');

// URLs to keep (normalized for comparison)
const URLS_TO_KEEP = WEBSITES_TO_KEEP.map(site => site.url);

async function fetchAllWebsites() {
    try {
        console.log('📡 Fetching all websites from API...');
        const response = await fetch(`${API_BASE_URL}/getWebsites?all=true`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const websites = await response.json();
        console.log(`✅ Fetched ${websites.length} websites`);
        return websites;
    } catch (error) {
        console.error('❌ Error fetching websites:', error.message);
        throw error;
    }
}

async function deleteWebsite(id, url) {
    try {
        console.log(`🗑️  Deleting: ${id} (${url})`);

        // Better to never delete anything. Just filter.
        const response = await fetch(`${API_BASE_URL}/deletewebsite`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ id, url })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(`HTTP ${response.status}: ${errorData.error || 'Unknown error'}`);
        }

        const result = await response.json();
        console.log(`✅ Deleted: ${id} (${url})`);
        return { success: true, id, url, result };
    } catch (error) {
        console.error(`❌ Error deleting ${url}:`, error.message);
        return { success: false, id, url, error: error.message };
    }
}

// 🟩 Utility to wait for confirmation
async function askForConfirmation(question) {
    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });

    return new Promise(resolve => {
        rl.question(question, answer => {
            rl.close();
            resolve(answer.trim().toLowerCase());
        });
    });
}

async function main() {
    try {
        console.log('🗑️  Website Cleanup Tool');
        console.log('='.repeat(50));
        console.log(`📋 Keeping ${WEBSITES_TO_KEEP.length} websites from static list`);
        console.log(`🔗 URLs to keep: \n${URLS_TO_KEEP.join('\n')}`);
        console.log('');

        // Fetch all websites
        const allWebsites = await fetchAllWebsites();

        // Separate websites to keep vs delete
        const websitesToKeep = [];
        const websitesToDelete = [];

        allWebsites.forEach(website => {
            const shouldKeep = URLS_TO_KEEP.includes(website.url);

            if (shouldKeep) {
                websitesToKeep.push(website);
            } else {
                websitesToDelete.push(website);
            }
        });

        console.log(`📊 Analysis:`);
        console.log(`   ✅ Websites to keep: ${websitesToKeep.length}`);
        console.log(`   🗑️  Websites to delete: ${websitesToDelete.length}`);
        console.log(`   📈 Total websites: ${allWebsites.length}`);
        console.log('');

        if (websitesToDelete.length === 0) {
            console.log('🎉 No websites to delete! Database is already clean.');
            return;
        }

        // Show websites that will be deleted
        console.log('🗑️  Websites to be deleted:');
        websitesToDelete.forEach((website, index) => {
            console.log(`   ${index + 1}. ${website.name} (${website.url})`);
        });
        console.log('');

        console.log('⚠️  WARNING: This will permanently delete the above websites from the database!');
        console.log('');

        // ✅ TODO completed: Wait for approval from console.
        const confirmation = await askForConfirmation('Type "YES" to confirm deletion: ');
        if (confirmation !== 'yes') {
            console.log('❎ Deletion cancelled by user.');
            return;
        }

        // Starting actual deletion process
        console.log('🗑️  Starting deletion process...');

        const deletionResults = [];
        for (const website of websitesToDelete) {
            const result = await deleteWebsite(website.id, website.url);
            deletionResults.push(result);

            // Add a small delay to avoid overwhelming the API
            await new Promise(resolve => setTimeout(resolve, 100));
        }

        const successfulDeletions = deletionResults.filter(r => r.success).length;
        const failedDeletions = deletionResults.filter(r => !r.success).length;

        console.log('');
        console.log('📊 Deletion Results:');
        console.log(`   ✅ Successfully deleted: ${successfulDeletions}`);
        console.log(`   ❌ Failed deletions: ${failedDeletions}`);

        if (failedDeletions > 0) {
            console.log('');
            console.log('❌ Failed deletions:');
            deletionResults.filter(r => !r.success).forEach(result => {
                console.log(`   - ${result.url}: ${result.error}`);
            });
        }

        // Save results to files
        const results = {
            timestamp: new Date().toISOString(),
            totalWebsites: allWebsites.length,
            websitesToKeep: websitesToKeep.length,
            websitesToDelete: websitesToDelete.length,
            websitesToKeep: websitesToKeep,
            websitesToDelete: websitesToDelete
        };

        fs.writeFileSync('tools/cleanup-results.json', JSON.stringify(results, null, 2));
        console.log('💾 Results saved to: tools/cleanup-results.json');

        // Show summary
        console.log('');
        console.log('📋 Summary:');
        console.log(`   Total websites: ${allWebsites.length}`);
        console.log(`   Keep: ${websitesToKeep.length}`);
        console.log(`   Delete: ${websitesToDelete.length}`);
        console.log(`   Cleanup percentage: ${Math.round(websitesToDelete.length / allWebsites.length * 100)}%`);

    } catch (error) {
        console.error('❌ Error in cleanup process:', error.message);
        process.exit(1);
    }
}

// Run the script
if (require.main === module) {
    main();
}

module.exports = { main, WEBSITES_TO_KEEP, URLS_TO_KEEP, askForConfirmation }; 