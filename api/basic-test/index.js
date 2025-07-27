module.exports = async function (context, req) {
    context.log('Basic test function called');
    
    context.res = {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
        body: { 
            message: 'Basic test function is working!',
            timestamp: new Date().toISOString()
        }
    };
}; 