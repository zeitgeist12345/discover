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
    'http://localhost:8080',
    'http://localhost:5500',
    'http://127.0.0.1:8000',
    'http://127.0.0.1:3000',
    'http://127.0.0.1:8080',
    'http://127.0.0.1:5500',
    'null' // For file:// protocol
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
    context.log.info('incrementView function started');
    context.log.info('Request method:', req.method);
    context.log.info('Request query:', JSON.stringify(req.query));
    context.log.info('Request body:', JSON.stringify(req.body));
    
    const origin = req.headers.origin || req.headers.Origin;
    context.log.info('Origin:', origin);
    const corsHeaders = getCorsHeaders(origin);
    
    // Handle CORS preflight requests
    if (req.method === 'OPTIONS') {
        context.log.info('Handling CORS preflight request');
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
    
    context.log.info('Parameters:', { id, partitionKey, action });
    
    if (!id) {
        context.log.error('Missing id parameter');
        context.res = {
            status: 400,
            headers: corsHeaders,
            body: { error: 'Missing id parameter' }
        };
        return;
    }
    
    try {
        context.log.info('Environment variables check:');
        context.log.info('COSMOS_ENDPOINT:', COSMOS_ENDPOINT ? 'Set' : 'Not set');
        context.log.info('MANAGED_IDENTITY_CLIENT_ID:', MANAGED_IDENTITY_CLIENT_ID ? 'Set' : 'Not set');
        
        context.log.info('Creating Azure credential...');
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: MANAGED_IDENTITY_CLIENT_ID
        });
        context.log.info('Azure credential created successfully');
        
        context.log.info('Creating Cosmos DB client...');
        const client = new CosmosClient({
            endpoint: COSMOS_ENDPOINT,
            aadCredentials: credential
        });
        context.log.info('Cosmos DB client created successfully');
        
        context.log.info('Getting container reference...');
        const container = client.database(DATABASE_NAME).container(CONTAINER_NAME);
        context.log.info('Container reference obtained');
        
        // Read the item
        context.log.info(`Reading item with id: ${id}, partitionKey: ${partitionKey}`);
        const { resource: website } = await container.item(id, partitionKey).read();
        if (!website) {
            context.log.error('Website not found');
            context.res = {
                status: 404,
                headers: corsHeaders,
                body: { error: 'Website not found' }
            };
            return;
        }
        context.log.info('Website found:', JSON.stringify(website));
        
        // Initialize counters if they don't exist
        website.views = website.views || 0;
        website.likes = website.likes || 0;
        website.dislikes = website.dislikes || 0;
        
        // Increment based on action
        switch (action) {
            case 'view':
                website.views += 1;
                context.log.info(`Incremented views to: ${website.views}`);
                break;
            case 'like':
                website.likes += 1;
                context.log.info(`Incremented likes to: ${website.likes}`);
                break;
            case 'dislike':
                website.dislikes += 1;
                context.log.info(`Incremented dislikes to: ${website.dislikes}`);
                break;
            default:
                context.log.error('Invalid action:', action);
                context.res = {
                    status: 400,
                    headers: corsHeaders,
                    body: { error: 'Invalid action. Use view, like, or dislike' }
                };
                return;
        }
        
        // Replace the item
        context.log.info('Replacing website in Cosmos DB...');
        await container.item(id, partitionKey).replace(website);
        context.log.info('Website updated successfully');
        
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
        context.log.info('Response prepared successfully');
    } catch (err) {
        context.log.error('Error in incrementView function:', err);
        context.log.error('Error stack:', err.stack);
        context.log.error('Error message:', err.message);
        context.log.error('Error name:', err.name);
        
        context.res = {
            status: 500,
            headers: corsHeaders,
            body: { 
                error: 'Failed to update website stats', 
                details: err.message,
                stack: err.stack,
                name: err.name
            }
        };
    }
}; 