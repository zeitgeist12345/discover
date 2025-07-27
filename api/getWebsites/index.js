const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

// Approved CORS origins (must match exact format)
const ALLOWED_ORIGINS = [
    'https://zeitgeist12345.github.io',
    'http://localhost:3000',
    'https://*.yourdomain.com' // Example wildcard
];

module.exports = async function (context, req) {
    // Validate origin
    const origin = req.headers.origin || '';
    const isValidOrigin = ALLOWED_ORIGINS.some(allowed => 
        new RegExp('^' + allowed.replace('.', '\\.').replace('*', '.*') + '$').test(origin)
    );

    const corsHeaders = {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': isValidOrigin ? origin : ALLOWED_ORIGINS[0],
        'Access-Control-Allow-Methods': 'GET'
    };

    // Handle preflight
    if (req.method === 'OPTIONS') {
        context.res = { status: 204, headers: corsHeaders };
        return;
    }

    try {
        // Initialize with UAMI
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: process.env.AZURE_CLIENT_ID
        });

        const client = new CosmosClient({
            endpoint: process.env.COSMOS_ENDPOINT,
            aadCredentials: credential
        });

        const { resources } = await client
            .database('websites')
            .container('list')
            .items
            .query("SELECT * FROM c")
            .fetchAll();

        context.res = {
            status: 200,
            body: resources,
            headers: corsHeaders
        };

    } catch (error) {
        context.res = {
            status: 500,
            body: { error: error.message },
            headers: corsHeaders
        };
    }
};