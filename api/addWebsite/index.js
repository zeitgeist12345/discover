const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const MANAGED_IDENTITY_CLIENT_ID = process.env.AZURE_CLIENT_ID;

module.exports = async function (context, req) {
    // Add CORS headers (exactly like incrementView)
    context.res = {
        headers: {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type'
        }
    };

    // Handle preflight OPTIONS request (exactly like incrementView)
    if (req.method === 'OPTIONS') {
        context.res.status = 200;
        return;
    }

    try {
        // Validate required fields
        const { name, url, description, category = 'user-submitted' } = req.body;
        
        if (!name || !url || !description) {
            context.res = {
                status: 400,
                body: { 
                    error: 'Missing required fields: name, url, and description are required' 
                },
                headers: {
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                    'Access-Control-Allow-Headers': 'Content-Type'
                }
            };
            return;
        }

        // Validate URL format
        try {
            new URL(url);
        } catch (error) {
            context.res = {
                status: 400,
                body: { 
                    error: 'Invalid URL format' 
                },
                headers: {
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                    'Access-Control-Allow-Headers': 'Content-Type'
                }
            };
            return;
        }

        // Initialize with UAMI
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: MANAGED_IDENTITY_CLIENT_ID
        });

        const client = new CosmosClient({
            endpoint: COSMOS_ENDPOINT,
            aadCredentials: credential
        });

        const container = client.database(DATABASE_NAME).container(CONTAINER_NAME);

        // Check if website already exists
        const querySpec = {
            query: "SELECT * FROM c WHERE c.url = @url",
            parameters: [{ name: "@url", value: url.trim() }]
        };

        const { resources: existingWebsites } = await container.items.query(querySpec).fetchAll();

        if (existingWebsites.length > 0) {
            context.res = {
                status: 409,
                body: { 
                    error: 'Website with this URL already exists' 
                },
                headers: {
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                    'Access-Control-Allow-Headers': 'Content-Type'
                }
            };
            return;
        }

        // Create new website object
        const id = `website-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
        const newWebsite = {
            id: id,
            name: name.trim(),
            url: url.trim(),
            description: description.trim(),
            category: category,
            createdAt: new Date().toISOString(),
            active: true,
            views: 0,
            likes: 0,
            dislikes: 0
        };

        // Add to database
        const { resource: createdWebsite } = await container.items.create(newWebsite);

        context.log('Website added successfully:', createdWebsite.id);

        context.res = {
            status: 201,
            body: {
                success: true,
                message: 'Website added successfully',
                website: createdWebsite
            },
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            }
        };

    } catch (error) {
        context.log.error('Error in addWebsite:', error);
        
        context.res = {
            status: 500,
            body: { 
                error: 'Failed to process website. Please try again.' 
            },
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            }
        };
    }
}; 