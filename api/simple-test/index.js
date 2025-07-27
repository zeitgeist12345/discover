module.exports = async function (context, req) {
    context.log.info('Simple test function started');
    
    try {
        const response = {
            message: 'Simple test function is working!',
            timestamp: new Date().toISOString(),
            method: req.method,
            url: req.url,
            headers: req.headers,
            environment: {
                nodeVersion: process.version,
                platform: process.platform,
                arch: process.arch
            }
        };
        
        context.log.info('Response prepared:', JSON.stringify(response));
        
        context.res = {
            status: 200,
            headers: { 'Content-Type': 'application/json' },
            body: response
        };
        
        context.log.info('Simple test function completed successfully');
    } catch (err) {
        context.log.error('Error in simple test function:', err);
        context.log.error('Error stack:', err.stack);
        
        context.res = {
            status: 500,
            headers: { 'Content-Type': 'application/json' },
            body: { 
                error: 'Simple test function failed',
                details: err.message,
                stack: err.stack
            }
        };
    }
}; 