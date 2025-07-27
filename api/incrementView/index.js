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
            'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type'
        }
    };

    // Handle preflight OPTIONS request
    if (req.method === 'OPTIONS') {
        context.res.status = 200;
        return;
    }

    const id = req.query.id || (req.body && req.body.id);
    const url = req.query.url || (req.body && req.body.url);
    const partitionKey = url; // Use the url as partition key
    const action = req.query.action || (req.body && req.body.action) || 'view'; // view, like, dislike
    
    if (!id || !url) {
        context.res = {
            status: 400,
            body: { error: 'Missing id or url parameter' },
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            }
        };
        return;
    }

    try {
        // Initialize with UAMI
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: MANAGED_IDENTITY_CLIENT_ID
        });

        const client = new CosmosClient({
            endpoint: COSMOS_ENDPOINT,
            aadCredentials: credential
        });

        const container = client.database(DATABASE_NAME).container(CONTAINER_NAME);

        // Get the current website
        const { resource: website } = await container.item(id, partitionKey).read();
        
        if (!website) {
            context.res = {
                status: 404,
                body: { error: 'Website not found' },
                headers: {
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                    'Access-Control-Allow-Headers': 'Content-Type'
                }
            };
            return;
        }

        // Update the counter based on action
        switch (action) {
            case 'view':
                website.views = (website.views || 0) + 1;
                break;
            case 'like':
                website.likes = (website.likes || 0) + 1;
                break;
            case 'dislike':
                website.dislikes = (website.dislikes || 0) + 1;
                break;
            default:
                context.res = {
                    status: 400,
                    body: { error: 'Invalid action. Use view, like, or dislike' },
                    headers: {
                        'Access-Control-Allow-Origin': '*',
                        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                        'Access-Control-Allow-Headers': 'Content-Type'
                    }
                };
                return;
        }

        // Update the website in the database
        await container.item(id, partitionKey).replace(website);

        context.res = {
            status: 200,
            body: { 
                success: true, 
                message: `${action} incremented successfully`,
                website: {
                    id: website.id,
                    name: website.name,
                    views: website.views,
                    likes: website.likes,
                    dislikes: website.dislikes
                }
            },
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            }
        };

    } catch (error) {
        context.log.error('Error in incrementView:', error);
        context.res = {
            status: 500,
            body: { error: error.message },
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            }
        };
    }
}; 