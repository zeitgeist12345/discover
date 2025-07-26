const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const MANAGED_IDENTITY_CLIENT_ID = process.env.AZURE_CLIENT_ID;

module.exports = async function (context, req) {
    const id = req.query.id || (req.body && req.body.id);
    const partitionKey = req.query.category || (req.body && req.body.category) || 'curated';
    if (!id) {
        context.res = {
            status: 400,
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
            body: { success: true, id, views: website.views }
        };
    } catch (err) {
        context.log.error('Error incrementing views:', err);
        context.res = {
            status: 500,
            body: { error: 'Failed to increment views', details: err.message }
        };
    }
}; 