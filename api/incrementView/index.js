const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

const COSMOS_ENDPOINT = process.env.COSMOS_ENDPOINT;
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const MANAGED_IDENTITY_CLIENT_ID = process.env.AZURE_CLIENT_ID;

module.exports = async function (context, req) {
    const id = req.query.id || (req.body && req.body.id);
    const partitionKey = req.query.category || (req.body && req.body.category) || 'curated';
    const action = req.query.action || (req.body && req.body.action) || 'view'; // view, like, dislike
    
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
        
        // Initialize counters if they don't exist
        website.views = website.views || 0;
        website.likes = website.likes || 0;
        website.dislikes = website.dislikes || 0;
        
        // Increment based on action
        switch (action) {
            case 'view':
                website.views += 1;
                break;
            case 'like':
                website.likes += 1;
                break;
            case 'dislike':
                website.dislikes += 1;
                break;
            default:
                context.res = {
                    status: 400,
                    body: { error: 'Invalid action. Use view, like, or dislike' }
                };
                return;
        }
        
        // Replace the item
        await container.item(id, partitionKey).replace(website);
        
        context.res = {
            status: 200,
            body: { 
                success: true, 
                id, 
                action,
                views: website.views,
                likes: website.likes,
                dislikes: website.dislikes
            }
        };
    } catch (err) {
        context.log.error('Error in incrementView function:', err);
        
        context.res = {
            status: 500,
            body: { 
                error: 'Failed to update website stats', 
                details: err.message
            }
        };
    }
}; 