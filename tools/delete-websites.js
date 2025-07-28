const fs = require('fs');
const path = require('path');

// Configuration
const API_BASE_URL = 'https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api';

// Static websites to keep (from user's list)
const WEBSITES_TO_KEEP = [
    {
        name: "skribbl.io",
        url: "https://skribbl.io/",
        description: "Free multiplayer drawing and guessing game"
    },
    {
        name: "zeitgeist12345",
        url: "https://zeitgeist12345.github.io/",
        description: "The personal website of the creator of this project"
    },
    {
        name: "Sci-Hub",
        url: "https://sci-hub.se/",
        description: "Removing barriers in the way of science by providing free access to research papers"
    },
    {
        name: "Library Genesis",
        url: "https://libgen.li/",
        description: "Massive digital library of books, articles, and media"
    },
    {
        name: "Internet Archive",
        url: "https://archive.org/",
        description: "Digital library of free & borrowable books, movies, music & Wayback Machine"
    },
    {
        name: "The Useless Web",
        url: "https://theuselessweb.com/",
        description: "Random fun and bizarre websites with one click"
    },
    {
        name: "Play Counter-Strike 1.6",
        url: "https://play-cs.com/",
        description: "Play classic CS 1.6 online without downloading"
    },
    {
        name: "Product Hunt",
        url: "https://producthunt.com",
        description: "The best new products in tech"
    },
    {
        name: "Hacker News",
        url: "https://news.ycombinator.com",
        description: "Social news website focusing on computer science"
    },
    {
        name: "Overleaf",
        url: "https://www.overleaf.com/",
        description: "Online LaTeX editor with real-time collaboration"
    },
    {
        name: "The Longest Blockchain",
        url: "https://cryptoservices.github.io/blockchain/consensus/2019/05/21/bitcoin-length-weight-confusion.html",
        description: "Interesting perspective on blockchain strength"
    },
    {
        name: "Bored Button",
        url: "https://www.boredbutton.com/",
        description: "Collection of random fun websites and games"
    },
    {
        name: "Radio Garden",
        url: "http://radio.garden/",
        description: "Listen to live radio stations across the globe"
    },
    {
        name: "Window Swap",
        url: "https://window-swap.com/",
        description: "See the view from someone else's window around the world"
    },
    {
        name: "The Pudding",
        url: "https://pudding.cool/",
        description: "Visual essays that explain ideas with data and visuals"
    },
    {
        name: "xkcd",
        url: "https://xkcd.com/",
        description: "A webcomic of romance, sarcasm, math, and language"
    },
    {
        name: "Stellarium Web",
        url: "https://stellarium-web.org/",
        description: "Real-time 3D simulation of space with planetarium view"
    },
    {
        name: "Patatap",
        url: "https://patatap.com",
        description: "Turn your keyboard into a sound machine with colorful animations"
    },
    {
        name: "Little Alchemy 2",
        url: "https://littlealchemy2.com",
        description: "Combine elements to discover new objects (e.g., Earth + Fire = Lava)"
    },
    {
        name: "Pointer Pointer",
        url: "https://pointerpointer.com",
        description: "Photos of people pointing at your cursor wherever you move it"
    },
    {
        name: "Ncase.me",
        url: "https://ncase.me",
        description: "Interactive simulations about trust and human behavior"
    },
    {
        name: "Connected Papers",
        url: "https://www.connectedpapers.com",
        description: "Visual tool to explore academic research connections"
    },
    {
        name: "Quick, Draw!",
        url: "https://quickdraw.withgoogle.com",
        description: "AI game that guesses your doodles"
    },
    {
        name: "A Soft Murmur",
        url: "https://asoftmurmur.com",
        description: "Mix ambient sounds (rain, waves) for focus"
    },
    {
        name: "10 Minute Mail",
        url: "https://10minutemail.com",
        description: "Disposable email for spam-free signups"
    },
    {
        name: "The Deep Sea",
        url: "https://neal.fun/deep-sea",
        description: "Interactive dive into ocean depths with fascinating facts"
    },
    {
        name: "Don't Even Reply",
        url: "https://dontevenreply.com",
        description: "Hilarious fictional email exchanges"
    },
    {
        name: "Scream Into the Void",
        url: "https://screamintothevoid.com",
        description: "Type your frustrations and hear a scream"
    },
    {
        name: "This Is Sand",
        url: "https://thisissand.com",
        description: "Digital sand art creator"
    },
    {
        name: "DeepSeek",
        url: "https://www.deepseek.com/en",
        description: "AI research and development company"
    },
    {
        name: "Al Jazeera",
        url: "https://www.aljazeera.com",
        description: "International news and current affairs network"
    },
    {
        name: "In depth flow of computers",
        url: "https://github.com/alex/what-happens-when",
        description: "An attempt to answer the age old interview question - What happens when you type google.com into your browser and press enter? This page explains how the computer systems work together."
    },
    {
        name: "Code build systems",
        url: "https://bazel.build/basics",
        description: "The best guide on how software build systems work and the their evolution. Bazel is the best build system by Google offering 0.5 second incremental build times using functional programming concepts."
    }
];

// URLs to keep (normalized for comparison)
const URLS_TO_KEEP = WEBSITES_TO_KEEP.map(site => normalizeUrl(site.url));

function normalizeUrl(url) {
    // Remove trailing slash and protocol for comparison
    return url.replace(/^https?:\/\//, '').replace(/\/$/, '');
}

async function fetchAllWebsites() {
    try {
        console.log('📡 Fetching all websites from API...');
        const response = await fetch(`${API_BASE_URL}/getwebsites?all=true`);
        
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

async function main() {
    try {
        console.log('🗑️  Website Cleanup Tool');
        console.log('='.repeat(50));
        console.log(`📋 Keeping ${WEBSITES_TO_KEEP.length} websites from static list`);
        console.log(`🔗 URLs to keep: ${URLS_TO_KEEP.join(', ')}`);
        console.log('');

        // Fetch all websites
        const allWebsites = await fetchAllWebsites();
        
        // Separate websites to keep vs delete
        const websitesToKeep = [];
        const websitesToDelete = [];
        
        allWebsites.forEach(website => {
            const normalizedUrl = normalizeUrl(website.url);
            const shouldKeep = URLS_TO_KEEP.includes(normalizedUrl);
            
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
        
        // Ask for confirmation
        console.log('⚠️  WARNING: This will permanently delete the above websites from the database!');
        console.log('');
        
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

module.exports = { main, WEBSITES_TO_KEEP, URLS_TO_KEEP }; 