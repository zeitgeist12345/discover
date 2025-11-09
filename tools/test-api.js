// Test script for the Azure Functions API
const API_BASE_URL = 'https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api';

async function testAPI() {
    console.log('🧪 Testing Azure Functions API...\n');

    try {
        // Test 1: Get websites
        console.log('1️⃣ Testing getWebsites...');
        const websitesResponse = await fetch(`${API_BASE_URL}/getWebsites`);
        const websites = await websitesResponse.json();
        console.log(`✅ Found ${websites.length} websites`);
        
        if (websites.length > 0) {
            const testWebsite = websites[0];
            console.log(`📝 Testing with website: ${testWebsite.name} (ID: ${testWebsite.id})`);
            
            // Test 2: Increment view
            console.log('\n2️⃣ Testing view increment...');
            const viewResponse = await fetch(`${API_BASE_URL}/incrementView?id=${testWebsite.id}&action=view`, {
                method: 'POST'
            });
            const viewResult = await viewResponse.json();
            console.log(`✅ View updated: ${viewResult.views} views`);
            
            // Test 3: Increment like
            console.log('\n3️⃣ Testing like increment...');
            const likeResponse = await fetch(`${API_BASE_URL}/incrementView?id=${testWebsite.id}&action=like`, {
                method: 'POST'
            });
            const likeResult = await likeResponse.json();
            console.log(`✅ Like updated: ${likeResult.likesMobile} likesMobile`);
            
            // Test 4: Increment dislike
            console.log('\n4️⃣ Testing dislike increment...');
            const dislikeResponse = await fetch(`${API_BASE_URL}/incrementView?id=${testWebsite.id}&action=dislike`, {
                method: 'POST'
            });
            const dislikeResult = await dislikeResponse.json();
            console.log(`✅ Dislike updated: ${dislikeResult.dislikesMobile} dislikesMobile`);
            
            // Test 5: Final stats
            console.log('\n5️⃣ Final stats:');
            console.log(`   👁️ Views: ${viewResult.views}`);
            console.log(`   👍 likesMobile: ${likeResult.likesMobile}`);
            console.log(`   👎 dislikesMobile: ${dislikeResult.dislikesMobile}`);
            
        } else {
            console.log('❌ No websites found to test with');
        }
        
    } catch (error) {
        console.error('❌ API test failed:', error);
    }
}

// Test CORS
async function testCORS() {
    console.log('\n🌐 Testing CORS...');
    
    try {
        const response = await fetch(`${API_BASE_URL}/getWebsites`, {
            method: 'OPTIONS',
            headers: {
                'Origin': 'https://zeitgeist12345.github.io',
                'Access-Control-Request-Method': 'GET',
                'Access-Control-Request-Headers': 'Content-Type'
            }
        });
        
        console.log(`✅ CORS preflight response: ${response.status}`);
        console.log('📋 CORS headers:');
        console.log(`   Access-Control-Allow-Origin: ${response.headers.get('Access-Control-Allow-Origin')}`);
        console.log(`   Access-Control-Allow-Methods: ${response.headers.get('Access-Control-Allow-Methods')}`);
        
    } catch (error) {
        console.error('❌ CORS test failed:', error);
    }
}

// Run tests
if (typeof window !== 'undefined') {
    // Browser environment
    window.testAPI = testAPI;
    window.testCORS = testCORS;
    console.log('🧪 Test functions available: testAPI() and testCORS()');
} else {
    // Node.js environment
    testAPI().then(() => testCORS());
} 