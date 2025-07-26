const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const MANAGED_IDENTITY_CLIENT_ID = process.env.AZURE_CLIENT_ID;

module.exports = async function (context, req) {
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
            headers: { 'Content-Type': 'application/json' },
            body: websites
        };
    } catch (err) {
        context.log.error('Error fetching websites:', err);
        context.res = {
            status: 500,
            body: { error: 'Failed to fetch websites', details: err.message }
        };
    }
}; 