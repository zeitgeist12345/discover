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

        // For now, return success without database operations
        // This ensures the frontend works while we debug the UAMI/Cosmos DB issue
        context.log('Validation passed, returning success response');

        const id = `website-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

        context.res = {
            status: 201,
            body: {
                success: true,
                message: 'Website validation successful (database integration pending)',
                website: {
                    id: id,
                    name: name.trim(),
                    url: url.trim(),
                    description: description.trim(),
                    category: category
                }
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