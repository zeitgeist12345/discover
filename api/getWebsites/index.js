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
    context.log.info('getWebsites function started');
    context.log.info('Request method:', req.method);
    context.log.info('Request headers:', JSON.stringify(req.headers));
    
    const origin = req.headers.origin || req.headers.Origin;
    context.log.info('Origin:', origin);
    const corsHeaders = getCorsHeaders(origin);
    context.log.info('CORS headers:', JSON.stringify(corsHeaders));
    
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

    try {
        context.log.info('Environment variables check:');
        context.log.info('COSMOS_ENDPOINT:', COSMOS_ENDPOINT ? 'Set' : 'Not set');
        context.log.info('MANAGED_IDENTITY_CLIENT_ID:', MANAGED_IDENTITY_CLIENT_ID ? 'Set' : 'Not set');
        context.log.info('DATABASE_NAME:', DATABASE_NAME);
        context.log.info('CONTAINER_NAME:', CONTAINER_NAME);
        
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
        
        context.log.info('Fetching websites from Cosmos DB...');
        const { resources: websites } = await container.items.readAll().fetchAll();
        context.log.info(`Successfully fetched ${websites ? websites.length : 0} websites`);
        
        context.res = {
            status: 200,
            headers: { 
                'Content-Type': 'application/json',
                ...corsHeaders
            },
            body: websites
        };
        context.log.info('Response prepared successfully');
    } catch (err) {
        context.log.error('Error in getWebsites function:', err);
        context.log.error('Error stack:', err.stack);
        context.log.error('Error message:', err.message);
        context.log.error('Error name:', err.name);
        
        context.res = {
            status: 500,
            headers: corsHeaders,
            body: { 
                error: 'Failed to fetch websites', 
                details: err.message,
                stack: err.stack,
                name: err.name
            }
        };
    }
}; 