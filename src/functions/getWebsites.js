const { app } = require('@azure/functions');
const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const MANAGED_IDENTITY_CLIENT_ID = process.env.AZURE_CLIENT_ID;

app.http('getWebsites', {
    methods: ['GET'],
    authLevel: 'function',
    handler: async (request, context) => {
        try {
            context.log('Fetching websites from Cosmos DB...');
            
            const credential = new DefaultAzureCredential({
                managedIdentityClientId: MANAGED_IDENTITY_CLIENT_ID
            });
            
            const client = new CosmosClient({
                endpoint: COSMOS_ENDPOINT,
                aadCredentials: credential
            });
            
            const container = client.database(DATABASE_NAME).container(CONTAINER_NAME);
            const { resources: websites } = await container.items.readAll().fetchAll();
            
            context.log(`Successfully fetched ${websites.length} websites`);
            
            return {
                jsonBody: websites
            };
        } catch (err) {
            context.error('Error fetching websites:', err);
            return {
                status: 500,
                jsonBody: { error: 'Failed to fetch websites', details: err.message }
            };
        }
    }
});
