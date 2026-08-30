/* Copyright (c) 2025 Mohammad Sheraj */
/* Discover is licensed under India PSL v1. You can use this software according to the terms and conditions of the India PSL v1. You may obtain a copy of India PSL v1 at: https://github.com/abirusabil123/discover/blob/main/IndiaPSL1 THIS SOFTWARE IS PROVIDED ON AN “AS IS” BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE. See the India PSL v1 for more details. */

// Test script for the Azure Functions API
const API_BASE_URL = 'https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurelinks.net/api';

async function testAPI() {
    console.log('🧪 Testing Azure Functions API...\n');

    try {
        // Test 1: Get links
        console.log('1️⃣ Testing getLinks...');
        const linksResponse = await fetch(`${API_BASE_URL}/getLinks&logUser=0`);
        const links = await linksResponse.json();
        console.log(`✅ Found ${links.length} links`);
        
        if (links.length > 0) {
            const testLink = links[0];
            console.log(`📝 Testing with link: ${testLink.name} (ID: ${testLink.id})`);
            
            // Test 2: Increment view
            console.log('\n2️⃣ Testing view increment...');
            const viewResponse = await fetch(`${API_BASE_URL}/incrementView?action=view`, {
                method: 'POST'
            });
            const viewResult = await viewResponse.json();
            console.log(`✅ View updated: ${viewResult.views} views`);
            
            // Test 3: Increment likes
            console.log('\n3️⃣ Testing likes increment...');
            const likeResponse = await fetch(`${API_BASE_URL}/incrementView?action=likes`, {
                method: 'POST'
            });
            const likeResult = await likeResponse.json();
            console.log(`✅ Likes updated: ${likeResult.likesMobile} likesMobile`);
            
            // Test 4: Increment dislikes
            console.log('\n4️⃣ Testing dislikes increment...');
            const dislikeResponse = await fetch(`${API_BASE_URL}/incrementView?action=dislikes`, {
                method: 'POST'
            });
            const dislikeResult = await dislikeResponse.json();
            console.log(`✅ Dislikes updated: ${dislikeResult.dislikesMobile} dislikesMobile`);
            
            // Test 5: Final stats
            console.log('\n5️⃣ Final stats:');
            console.log(`   👁️ Views: ${viewResult.views}`);
            console.log(`   👍 likesMobile: ${likeResult.likesMobile}`);
            console.log(`   👎 dislikesMobile: ${dislikeResult.dislikesMobile}`);
            
        } else {
            console.log('❌ No links found to test with');
        }
        
    } catch (error) {
        console.error('❌ API test failed:', error);
    }
}

// Test CORS
async function testCORS() {
    console.log('\n🌏 Testing CORS...');
    
    try {
        const response = await fetch(`${API_BASE_URL}/getLinks&logUser=0`, {
            method: 'OPTIONS',
            headers: {
                'Origin': 'https://abirusabil123.github.io',
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