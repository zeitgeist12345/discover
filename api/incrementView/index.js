const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const MANAGED_IDENTITY_CLIENT_ID = process.env.AZURE_CLIENT_ID;

// CORS configuration
const ALLOWED_ORIGINS = [
    'https://zeitgeist12345.github.io',
    'http://localhost:8000',
    'http://localhost:3000',
    'http://127.0.0.1:8000',
    'http://127.0.0.1:3000'
];

function getCorsHeaders(origin) {
    const corsOrigin = ALLOWED_ORIGINS.includes(origin) ? origin : ALLOWED_ORIGINS[0];
    return {
        'Access-Control-Allow-Origin': corsOrigin,
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type',
        'Access-Control-Max-Age': '86400'
    };
}

module.exports = async function (context, req) {
    const origin = req.headers.origin || req.headers.Origin;
    const corsHeaders = getCorsHeaders(origin);
    
    // Handle CORS preflight requests
    if (req.method === 'OPTIONS') {
        context.res = {
            status: 200,
            headers: corsHeaders,
            body: {}
        };
        return;
    }

    const id = req.query.id || (req.body && req.body.id);
    const partitionKey = req.query.category || (req.body && req.body.category) || 'curated';
    const action = req.query.action || (req.body && req.body.action) || 'view'; // view, like, dislike
    
    if (!id) {
        context.res = {
            status: 400,
            headers: corsHeaders,
            body: { error: 'Missing id parameter' }
        };
        return;
    }
    
    try {
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: MANAGED_IDENTITY_CLIENT_ID
        });
        const client = new CosmosClient({
            endpoint: COSMOS_ENDPOINT,
            aadCredentials: credential
        });
        const container = client.database(DATABASE_NAME).container(CONTAINER_NAME);
        
        // Read the item
        const { resource: website } = await container.item(id, partitionKey).read();
        if (!website) {
            context.res = {
                status: 404,
                headers: corsHeaders,
                body: { error: 'Website not found' }
            };
            return;
        }
        
        // Initialize counters if they don't exist
        website.views = website.views || 0;
        website.likes = website.likes || 0;
        website.dislikes = website.dislikes || 0;
        
        // Increment based on action
        switch (action) {
            case 'view':
                website.views += 1;
                break;
            case 'like':
                website.likes += 1;
                break;
            case 'dislike':
                website.dislikes += 1;
                break;
            default:
                context.res = {
                    status: 400,
                    headers: corsHeaders,
                    body: { error: 'Invalid action. Use view, like, or dislike' }
                };
                return;
        }
        
        // Replace the item
        await container.item(id, partitionKey).replace(website);
        
        context.res = {
            status: 200,
            headers: corsHeaders,
            body: { 
                success: true, 
                id, 
                action,
                views: website.views,
                likes: website.likes,
                dislikes: website.dislikes
            }
        };
    } catch (err) {
        context.log.error('Error updating website stats:', err);
        context.res = {
            status: 500,
            headers: corsHeaders,
            body: { error: 'Failed to update website stats', details: err.message }
        };
    }
}; 