const fs = require('fs');

const staticSites = [
    'skribbl.io', 'zeitgeist12345', 'Sci-Hub', 'Library Genesis', 'Internet Archive', 
    'The Useless Web', 'Play Counter-Strike 1.6', 'Product Hunt', 'Hacker News', 'Overleaf', 
    'The Longest Blockchain', 'Bored Button', 'Radio Garden', 'Window Swap', 'The Pudding', 
    'xkcd', 'Stellarium Web', 'Patatap', 'Little Alchemy 2', 'Pointer Pointer', 'Ncase.me', 
    'Connected Papers', 'Quick, Draw!', 'A Soft Murmur', '10 Minute Mail', 'The Deep Sea', 
    'Don\'t Even Reply', 'Scream Into the Void', 'This Is Sand', 'DeepSeek', 'Al Jazeera'
];

const data = JSON.parse(fs.readFileSync('tools/websites-dump.json', 'utf8'));
const dumpNames = data.map(w => w.name);

console.log('📊 Static Sites vs Dump Data Comparison');
console.log('='.repeat(60));

let found = 0;
let notFound = [];

staticSites.forEach(site => {
    const foundInDump = dumpNames.includes(site);
    if (foundInDump) {
        console.log(`✅ ${site}`);
        found++;
    } else {
        console.log(`❌ ${site}`);
        notFound.push(site);
    }
});

console.log('='.repeat(60));
console.log(`📈 Results: ${found}/${staticSites.length} static sites found in dump (${Math.round(found/staticSites.length*100)}%)`);

if (notFound.length > 0) {
    console.log('\n❌ Missing sites:');
    notFound.forEach(site => console.log(`   - ${site}`));
}

console.log('\n📋 Additional sites in dump (not in static list):');
const additionalSites = dumpNames.filter(name => !staticSites.includes(name));
additionalSites.forEach(site => console.log(`   + ${site}`));
console.log(`\n📊 Total: ${dumpNames.length} sites in dump, ${staticSites.length} in static list`); 