const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

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

        // Check if we want all websites (including filtered ones)
        const getAllWebsites = req.query.all === 'true';
        
        let websitesToReturn;
        
        if (getAllWebsites) {
            // Return all websites without filtering
            websitesToReturn = resources;
        } else {
            // Apply content filtering
            websitesToReturn = resources.filter(website => {
                const likes = website.likes || 0;
                const dislikes = website.dislikes || 0;
                const total = likes + dislikes;
                
                // If less votes, consider it okay
                if (total <= 3) {
                    return true;
                }
                
                const undesirable_score = dislikes / Math.max(total, 1);
                
                return undesirable_score < 0.8;
            });
        }

        context.res = {
            status: 200,
            body: websitesToReturn,
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            }
        };

    } catch (error) {
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