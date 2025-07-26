# 🔧 CORS Setup for Azure Functions

To enable your static website to access the Azure Functions API, you need to configure CORS (Cross-Origin Resource Sharing).

## Option 1: Configure CORS in Azure Functions (Recommended)

### Update your Azure Functions to include CORS headers:

#### For `getWebsites` function (`api/getWebsites/index.js`):

```javascript
module.exports = async function (context, req) {
    try {
        const credential = new DefaultAzureCredential({
            managedIdentityClientId: MANAGED_IDENTITY_CLIENT_ID
        });
        const client = new CosmosClient({
            endpoint: COSMOS_ENDPOINT,
            aadCredentials: credential
        });
        const container = client.database(DATABASE_NAME).container(CONTAINER_NAME);
        const { resources: websites } = await container.items.readAll().fetchAll();
        
        context.res = {
            status: 200,
            headers: { 
                'Content-Type': 'application/json',
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            },
            body: websites
        };
    } catch (err) {
        context.log.error('Error fetching websites:', err);
        context.res = {
            status: 500,
            headers: { 
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            },
            body: { error: 'Failed to fetch websites', details: err.message }
        };
    }
};
```

#### For `incrementView` function (`api/incrementView/index.js`):

```javascript
module.exports = async function (context, req) {
    const id = req.query.id || (req.body && req.body.id);
    const partitionKey = req.query.category || (req.body && req.body.category) || 'curated';
    
    if (!id) {
        context.res = {
            status: 400,
            headers: { 
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            },
            body: { error: 'Missing id parameter' }
        };
        return;
    }
    
    try {
        // ... existing code ...
        
        context.res = {
            status: 200,
            headers: { 
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            },
            body: { success: true, id, views: website.views }
        };
    } catch (err) {
        context.log.error('Error incrementing views:', err);
        context.res = {
            status: 500,
            headers: { 
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
                'Access-Control-Allow-Headers': 'Content-Type'
            },
            body: { error: 'Failed to increment views', details: err.message }
        };
    }
};
```

## Option 2: Configure CORS in Azure Portal

1. Go to Azure Portal → Your Function App
2. Navigate to **API** → **CORS**
3. Add allowed origins:
   - `http://localhost:*` (for local development)
   - `https://yourdomain.com` (for production)
   - `*` (for testing - not recommended for production)
4. Save the changes

## Option 3: Use a Local Development Server

To avoid CORS issues during development, you can serve your static files using a local server:

### Using Python:
```bash
# Python 3
python -m http.server 8000

# Python 2
python -m SimpleHTTPServer 8000
```

### Using Node.js:
```bash
# Install http-server globally
npm install -g http-server

# Serve the current directory
http-server -p 8000
```

### Using PHP:
```bash
php -S localhost:8000
```

Then access your site at `http://localhost:8000`

## Testing CORS

After setting up CORS, you can test it with:

```bash
# Test the API directly
curl -H "Origin: http://localhost:8000" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     https://discover-api.azurewebsites.net/api/getWebsites
```

## Security Note

For production, replace `'*'` with specific domains:
- `'https://yourdomain.com'`
- `'https://www.yourdomain.com'`

## Enable API Mode

Once CORS is configured, update `config.js`:

```javascript
const CONFIG = {
    USE_API: true,  // Change this to true
    // ... other settings
};
``` 