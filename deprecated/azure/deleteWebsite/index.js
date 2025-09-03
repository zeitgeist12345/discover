const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const MANAGED_IDENTITY_CLIENT_ID = process.env.AZURE_CLIENT_ID;

module.exports = async function (context, req) {
    // Add CORS headers
    context.res = {
        headers: {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'DELETE, POST, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type'
        }
    };

    // Handle preflight OPTIONS request
    if (req.method === 'OPTIONS') {
        context.res.status = 200;
        return;
    }

    try {
        // Validate request
        if (req.method !== 'DELETE' && req.method !== 'POST') {
            context.res.status = 405;
            context.res.body = { error: 'Method not allowed. Use DELETE or POST.' };
            return;
        }

        const { id, url } = req.body;

        if (!id || !url) {
            context.res.status = 400;
            context.res.body = { error: 'Website ID and URL are required' };
            return;
        }

        const partitionKey = url; // Use the url as partition key

        // Initialize with UAMI
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: MANAGED_IDENTITY_CLIENT_ID
        });

        const client = new CosmosClient({
            endpoint: COSMOS_ENDPOINT,
            aadCredentials: credential
        });

        const container = client.database(DATABASE_NAME).container(CONTAINER_NAME);

        // Delete the website by ID
        try {
            await container.item(id, partitionKey).delete();
            
            context.res.status = 200;
            context.res.body = { 
                success: true, 
                message: 'Website deleted successfully',
                deletedId: id,
                deletedUrl: url
            };
            
            context.log(`Website deleted: ${id} (${url})`);
            
        } catch (deleteError) {
            if (deleteError.code === 404) {
                context.res.status = 404;
                context.res.body = { 
                    error: 'Website not found',
                    id: id,
                    url: url
                };
            } else {
                throw deleteError;
            }
        }

    } catch (error) {
        context.log.error('Error deleting website:', error);
        context.res.status = 500;
        context.res.body = { 
            error: 'Internal server error',
            details: error.message
        };
    }
}; 