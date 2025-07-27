const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

module.exports = async function (context, req) {
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
            body: resources
        };

    } catch (error) {
        context.res = {
            status: 500,
            body: { error: error.message }
        };
    }
};