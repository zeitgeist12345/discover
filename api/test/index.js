const { app } = require('@azure/functions');

app.http('test', {
    methods: ['GET', 'POST', 'OPTIONS'],
    authLevel: 'anonymous',
    handler: async (request, context) => {
        context.log.info('Test function started');
        context.log.info('Request method:', request.method);
        context.log.info('Request headers:', JSON.stringify(Object.fromEntries(request.headers.entries())));
        context.log.info('Environment variables:');
        context.log.info('NODE_ENV:', process.env.NODE_ENV);
        context.log.info('COSMOS_ENDPOINT:', process.env.COSMOS_ENDPOINT ? 'Set' : 'Not set');
        context.log.info('AZURE_CLIENT_ID:', process.env.AZURE_CLIENT_ID ? 'Set' : 'Not set');
        
        try {
            return {
                status: 200,
                headers: { 'Content-Type': 'application/json' },
                body: { 
                    message: 'Test function is working!',
                    timestamp: new Date().toISOString(),
                    environment: process.env.NODE_ENV || 'production',
                    cosmosEndpoint: process.env.COSMOS_ENDPOINT ? 'Set' : 'Not set',
                    azureClientId: process.env.AZURE_CLIENT_ID ? 'Set' : 'Not set'
                }
            };
        } catch (err) {
            context.log.error('Error in test function:', err);
            context.log.error('Error stack:', err.stack);
            return {
                status: 500,
                headers: { 'Content-Type': 'application/json' },
                body: { 
                    error: 'Test function failed',
                    details: err.message,
                    stack: err.stack
                }
            };
        }
    }
}); 