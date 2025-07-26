const { CosmosClient } = require('@azure/cosmos');
const { DefaultAzureCredential } = require('@azure/identity');

// Import the websites array from the local file
const { websites } = require('./websites.js');

// Cosmos DB configuration
const COSMOS_ENDPOINT = 'https://discover-cosmosdb.documents.azure.com:443/';
const DATABASE_NAME = 'websites';
const CONTAINER_NAME = 'list';
const AZURE_CLIENT_ID = "1165b1e9-8e50-4481-9705-62d2fe3467ef";

async function uploadWebsitesToCosmosDB() {
    try {
        console.log('🚀 Starting upload to Cosmos DB...');
        
        // Initialize the Azure credential (will use UAMI)
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: "1165b1e9-8e50-4481-9705-62d2fe3467ef" // Set this to your UAMI client ID
        });

        // Create Cosmos DB client
        const client = new CosmosClient({
            endpoint: COSMOS_ENDPOINT,
            aadCredentials: credential
        });

        // Get database and container references
        const database = client.database(DATABASE_NAME);
        const container = database.container(CONTAINER_NAME);

        console.log(`📊 Found ${websites.length} websites to upload`);

        // Upload each website as a document
        const uploadPromises = websites.map(async (website, index) => {
            const document = {
                id: `website-${index + 1}`,
                name: website.name,
                url: website.url,
                description: website.description,
                createdAt: new Date().toISOString(),
                category: 'curated',
                active: true
            };

            try {
                await container.items.create(document);
                console.log(`✅ Uploaded: ${website.name}`);
                return { success: true, website: website.name };
            } catch (error) {
                console.error(`❌ Failed to upload ${website.name}:`, error.message);
                return { success: false, website: website.name, error: error.message };
            }
        });

        // Wait for all uploads to complete
        const results = await Promise.all(uploadPromises);
        
        // Summary
        const successful = results.filter(r => r.success).length;
        const failed = results.filter(r => !r.success).length;
        
        console.log('\n📈 Upload Summary:');
        console.log(`✅ Successful: ${successful}`);
        console.log(`❌ Failed: ${failed}`);
        console.log(`📊 Total: ${websites.length}`);

        if (failed > 0) {
            console.log('\n❌ Failed uploads:');
            results.filter(r => !r.success).forEach(r => {
                console.log(`   - ${r.website}: ${r.error}`);
            });
        }

        console.log('\n🎉 Upload process completed!');

    } catch (error) {
        console.error('💥 Error during upload:', error);
        process.exit(1);
    }
}

// Helper function to create database and container if they don't exist
async function setupCosmosDB() {
    try {
        console.log('🔧 Setting up Cosmos DB...');
        
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: AZURE_CLIENT_ID
        });

        const client = new CosmosClient({
            endpoint: COSMOS_ENDPOINT,
            aadCredentials: credential
        });

        // Create database if it doesn't exist
        try {
            await client.databases.createIfNotExists({
                id: DATABASE_NAME
            });
            console.log(`✅ Database '${DATABASE_NAME}' ready`);
        } catch (error) {
            console.log(`ℹ️  Database '${DATABASE_NAME}' already exists`);
        }

        // Create container if it doesn't exist
        try {
            await client.database(DATABASE_NAME).containers.createIfNotExists({
                id: CONTAINER_NAME,
                partitionKey: '/category'
            });
            console.log(`✅ Container '${CONTAINER_NAME}' ready`);
        } catch (error) {
            console.log(`ℹ️  Container '${CONTAINER_NAME}' already exists`);
        }

    } catch (error) {
        console.error('💥 Error setting up Cosmos DB:', error);
        throw error;
    }
}

// Main execution
async function main() {
    try {
        // Check if UAMI client ID is set
        if (!AZURE_CLIENT_ID) {
            console.error('❌ AZURE_CLIENT_ID environment variable not set');
            console.log('💡 Set it to your User-Assigned Managed Identity client ID');
            process.exit(1);
        }

        // Setup database and container
        await setupCosmosDB();
        
        // Upload websites
        await uploadWebsitesToCosmosDB();
        
    } catch (error) {
        console.error('💥 Fatal error:', error);
        process.exit(1);
    }
}

// Export functions for potential reuse
module.exports = {
    uploadWebsitesToCosmosDB,
    setupCosmosDB
};

// Run if this file is executed directly
if (require.main === module) {
    main();
} 