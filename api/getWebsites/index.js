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

    try {
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: MANAGED_IDENTITY_CLIENT_ID
        });
        const client = new CosmosClient({
            endpoint: COSMOS_ENDPOINT,
            aadCredentials: credential
        });
        const container = client.database(DATABASE_NAME).container(CONTAINER_NAME);
        const { resources: websites } = await container.items.readAll().fetchAll();
        context.res = {
            status: 200,
            headers: { 
                'Content-Type': 'application/json',
                ...corsHeaders
            },
            body: websites
        };
    } catch (err) {
        context.log.error('Error fetching websites:', err);
        context.res = {
            status: 500,
            headers: corsHeaders,
            body: { error: 'Failed to fetch websites', details: err.message }
        };
    }
}; 