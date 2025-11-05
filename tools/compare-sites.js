const fs = require('fs');
const path = require('path');

// Import the static sites data
const { WEBSITES_TO_KEEP } = require('./static-sites.js');

// Read the dump data
const data = JSON.parse(fs.readFileSync('./websites-dump.json', 'utf8'));
const dumpUrls = data.map(w => w.url);

// Extract URLs from static sites
const staticUrls = WEBSITES_TO_KEEP.map(site => site.url);

console.log('📊 Static Sites vs Dump Data Comparison');
console.log('='.repeat(60));

let found = 0;
let notFound = [];

// Compare URLs
staticUrls.forEach(url => {
    const foundInDump = dumpUrls.includes(url);
    if (foundInDump) {
        console.log(`✅ ${url}`);
        found++;
    } else {
        console.log(`❌ ${url}`);
        notFound.push(url);
    }
});

console.log('='.repeat(60));
console.log(`📈 Results: ${found}/${staticUrls.length} static sites found in dump (${Math.round(found/staticUrls.length*100)}%)`);

if (notFound.length > 0) {
    console.log('\n❌ Missing sites:');
    notFound.forEach(url => {
        const site = WEBSITES_TO_KEEP.find(s => s.url === url);
        console.log(`   - ${site.name} (${url})`);
    });
}

console.log('\n📋 Additional sites in dump (not in static list):');
const additionalSites = data.filter(site => !staticUrls.includes(site.url));
additionalSites.forEach(site => console.log(`   + ${site.name} (${site.url})`));

console.log(`\n📊 Total: ${data.length} sites in dump, ${staticUrls.length} in static list`);