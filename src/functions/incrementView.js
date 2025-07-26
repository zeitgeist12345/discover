const { app } = require('@azure/functions');
const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const MANAGED_IDENTITY_CLIENT_ID = process.env.AZURE_CLIENT_ID;

app.http('incrementView', {
    methods: ['GET', 'POST'],
    authLevel: 'function',
    handler: async (request, context) => {
        const id = request.query.get('id') || (await request.text() && JSON.parse(await request.text()).id);
        const partitionKey = request.query.get('category') || 'curated';
        
        if (!id) {
            return {
                status: 400,
                jsonBody: { error: 'Missing id parameter' }
            };
        }
        
        try {
            context.log(`Incrementing views for website: ${id}`);
            
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
                context.log(`Website not found: ${id}`);
                return {
                    status: 404,
                    jsonBody: { error: 'Website not found' }
                };
            }
            
            // Increment views
            website.views = (website.views || 0) + 1;
            
            // Replace the item
            await container.item(id, partitionKey).replace(website);
            
            context.log(`Successfully incremented views for ${id}. New count: ${website.views}`);
            
            return {
                jsonBody: { success: true, id, views: website.views }
            };
        } catch (err) {
            context.error('Error incrementing views:', err);
            return {
                status: 500,
                jsonBody: { error: 'Failed to increment views', details: err.message }
            };
        }
    }
});
