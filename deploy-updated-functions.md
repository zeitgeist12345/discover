# 🚀 Deploy Updated Azure Functions with CORS Support

## Prerequisites

- Azure CLI installed and logged in
- Your existing Function App (`discover-api`)
- Updated function code with CORS headers

## Step 1: Deploy Updated Functions

### Option A: Using Azure CLI (Recommended)

```bash
# Navigate to the api directory
cd api

# Create a new zip file with updated functions
zip -r api-functions-updated.zip . -x "node_modules/*" "*.zip"

# Deploy the updated functions
az functionapp deployment source config-zip \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --src "api-functions-updated.zip"
```

### Option B: Using Azure Functions Core Tools

```bash
# Navigate to the api directory
cd api

# Deploy
func azure functionapp publish discover-api
```

## Step 2: Verify CORS Configuration

### Test the API endpoints:

```bash
# Test getWebsites endpoint
curl -H "Origin: https://zeitgeist12345.github.io" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     https://discover-api.azurewebsites.net/api/getWebsites

# Test incrementView endpoint
curl -H "Origin: https://zeitgeist12345.github.io" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     https://discover-api.azurewebsites.net/api/incrementView
```

### Expected Response:
```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: https://zeitgeist12345.github.io
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type
Access-Control-Max-Age: 86400
```

## Step 3: Test the Live Website

1. Visit [https://zeitgeist12345.github.io/discover/](https://zeitgeist12345.github.io/discover/)
2. Open browser developer tools (F12)
3. Check the Console tab for any CORS errors
4. Try loading a random website

## Step 4: Monitor Function Logs

```bash
# View live logs
az webapp log tail \
  --resource-group "discover-rg" \
  --name "discover-api"
```

## Troubleshooting

### CORS Still Not Working?

1. **Check Function App CORS Settings**:
   ```bash
   # View current CORS settings
   az functionapp cors show \
     --resource-group "discover-rg" \
     --name "discover-api"
   ```

2. **Add CORS via Azure CLI**:
   ```bash
   # Add your domain to allowed origins
   az functionapp cors add \
     --resource-group "discover-rg" \
     --name "discover-api" \
     --allowed-origins "https://zeitgeist12345.github.io"
   ```

3. **Check Function App Logs**:
   ```bash
   # View recent logs
   az webapp log download \
     --resource-group "discover-rg" \
     --name "discover-api"
   ```

### Function Not Responding?

1. **Check Function Status**:
   ```bash
   # List functions
   az functionapp function list \
     --resource-group "discover-rg" \
     --name "discover-api"
   ```

2. **Test Function Directly**:
   ```bash
   # Test getWebsites
   curl "https://discover-api.azurewebsites.net/api/getWebsites"
   
   # Test incrementView
   curl -X POST "https://discover-api.azurewebsites.net/api/incrementView?id=website-1&category=curated"
   ```

## Expected Behavior

After successful deployment:

1. **Local Development** (`file://`): Uses static websites (no CORS issues)
2. **Local Server** (`localhost`): Uses API with CORS support
3. **Production** (`zeitgeist12345.github.io`): Uses API with CORS support

## Monitoring

- **Azure Portal** → Function App → Monitor → Logs
- **Application Insights** (if enabled) for detailed analytics
- **Cosmos DB** → Data Explorer to see view counts

## Next Steps

Once deployed, your website will:
- Load websites from Azure Cosmos DB via API
- Track website views automatically
- Fall back to static list if API is unavailable
- Work seamlessly across all environments 