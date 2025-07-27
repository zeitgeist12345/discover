const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

// Cosmos DB configuration
const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;

module.exports = async function (context, req) {
    context.log('addWebsite function called');
    context.log('Request method:', req.method);
    context.log('Request body:', req.body);
    
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
        context.log('Handling OPTIONS request');
        context.res.status = 200;
        return;
    }

    try {
        // Initialize with UAMI (same pattern as incrementView)
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: process.env.AZURE_CLIENT_ID
        });

        const client = new CosmosClient({
            endpoint: process.env.COSMOS_ENDPOINT,
            aadCredentials: credential
        });

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

        // Generate unique ID
        const id = `website-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

        // Create website object
        const website = {
            id: id,
            name: name.trim(),
            url: url.trim(),
            description: description.trim(),
            category: category,
            active: true,
            views: 0,
            likes: 0,
            dislikes: 0,
            createdAt: new Date().toISOString(),
            createdBy: 'user'
        };

        // Get container reference (same as incrementView)
        const container = client.database('websites').container('list');

        // Add website to database
        const { resource: createdWebsite } = await container.items.create(website);

        context.log(`Website added successfully: ${createdWebsite.id}`);

        context.res = {
            status: 201,
            body: {
                success: true,
                message: 'Website added successfully',
                website: {
                    id: createdWebsite.id,
                    name: createdWebsite.name,
                    url: createdWebsite.url,
                    description: createdWebsite.description,
                    category: createdWebsite.category
                }
            },
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            }
        };

    } catch (error) {
        context.log.error('Error adding website:', error);
        
        context.res = {
            status: 500,
            body: { 
                error: 'Failed to add website. Please try again.' 
            },
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            }
        };
    }
}; 