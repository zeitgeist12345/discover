const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const MANAGED_IDENTITY_CLIENT_ID = process.env.AZURE_CLIENT_ID;

module.exports = async function (context, req) {
    // Handle CORS preflight requests
    if (req.method === 'OPTIONS') {
        context.res = {
            status: 200,
            headers: {
                'Access-Control-Allow-Origin': 'https://zeitgeist12345.github.io',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type',
                'Access-Control-Max-Age': '86400'
            },
            body: {}
        };
        return;
    }

    const id = req.query.id || (req.body && req.body.id);
    const partitionKey = req.query.category || (req.body && req.body.category) || 'curated';
    if (!id) {
        context.res = {
            status: 400,
            headers: { 
                'Access-Control-Allow-Origin': 'https://zeitgeist12345.github.io',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            },
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
                headers: { 
                    'Access-Control-Allow-Origin': 'https://zeitgeist12345.github.io',
                    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                    'Access-Control-Allow-Headers': 'Content-Type'
                },
                body: { error: 'Website not found' }
            };
            return;
        }
        // Increment views
        website.views = (website.views || 0) + 1;
        // Replace the item
        await container.item(id, partitionKey).replace(website);
        context.res = {
            status: 200,
            headers: { 
                'Access-Control-Allow-Origin': 'https://zeitgeist12345.github.io',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            },
            body: { success: true, id, views: website.views }
        };
    } catch (err) {
        context.log.error('Error incrementing views:', err);
        context.res = {
            status: 500,
            headers: { 
                'Access-Control-Allow-Origin': 'https://zeitgeist12345.github.io',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            },
            body: { error: 'Failed to increment views', details: err.message }
        };
    }
}; 