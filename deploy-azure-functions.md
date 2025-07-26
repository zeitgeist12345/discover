# 🚀 Azure Functions Deployment Guide

## Prerequisites

- Azure CLI installed and logged in
- Your existing Cosmos DB account (`discover-cosmosdb`)
- Your User-Assigned Managed Identity (UAMI)

## Step 1: Create Azure Function App

```bash
# Create the Function App (replace 'discover-api' with your preferred name)
az functionapp create \
  --resource-group "discover-rg" \
  --consumption-plan-location "eastus" \
  --runtime "node" \
  --runtime-version "18" \
  --functions-version "4" \
  --name "discover-api" \
  --storage-account "discoverstorage$(date +%s)" \
  --os-type "Linux"
```

## Step 2: Configure Function App Settings

```bash
# Set the Cosmos DB endpoint
az functionapp config appsettings set \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --settings COSMOS_ENDPOINT="https://discover-cosmosdb.documents.azure.com:443/"

# Set the UAMI client ID
az functionapp config appsettings set \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --settings AZURE_CLIENT_ID="1165b1e9-8e50-4481-9705-62d2fe3467ef"
```

## Step 3: Assign Managed Identity to Function App

```bash
# Get your UAMI principal ID
UAMI_PRINCIPAL_ID=$(az identity show \
  --resource-group "discover-rg" \
  --name "your-uami-name" \
  --query "principalId" \
  --output tsv)

# Assign the UAMI to the Function App
az functionapp identity assign \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --identities "[systemassigned]"

# Or if you want to use your existing UAMI:
az functionapp identity assign \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --identities "/subscriptions/b7304d11-19c2-42f9-ae77-6649704522f8/resourceGroups/discover-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/your-uami-name"
```

## Step 4: Grant Cosmos DB Permissions

```bash
# Get the Function App's system-assigned identity principal ID
FUNCTION_PRINCIPAL_ID=$(az functionapp identity show \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --query "principalId" \
  --output tsv)

# Assign Cosmos DB Contributor role
az role assignment create \
  --assignee $FUNCTION_PRINCIPAL_ID \
  --role "Cosmos DB Account Contributor" \
  --scope "/subscriptions/b7304d11-19c2-42f9-ae77-6649704522f8/resourceGroups/discover-rg/providers/Microsoft.DocumentDB/databaseAccounts/discover-cosmosdb"
```

## Step 5: Deploy the Functions

### Option A: Using Azure CLI (Recommended)

```bash
# Navigate to the api directory
cd api

# Deploy the functions
az functionapp deployment source config-zip \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --src "api-functions.zip"
```

### Option B: Using Azure Functions Core Tools

```bash
# Install Azure Functions Core Tools (if not already installed)
npm install -g azure-functions-core-tools@4 --unsafe-perm true

# Navigate to the api directory
cd api

# Deploy
func azure functionapp publish discover-api
```

## Step 6: Test the Functions

### Get Function URLs

```bash
# Get the function URLs
az functionapp function show \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --function-name "getWebsites" \
  --query "invokeUrlTemplate"

az functionapp function show \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --function-name "incrementView" \
  --query "invokeUrlTemplate"
```

### Test with curl

```bash
# Test getWebsites
curl "https://discover-api.azurewebsites.net/api/getWebsites"

# Test incrementView
curl "https://discover-api.azurewebsites.net/api/incrementView?id=website-1&category=curated"
```

## Step 7: Update Frontend (Next Step)

Once deployed, you'll get URLs like:
- `https://discover-api.azurewebsites.net/api/getWebsites`
- `https://discover-api.azurewebsites.net/api/incrementView`

## Troubleshooting

### Check Function App Logs

```bash
# View live logs
az webapp log tail \
  --resource-group "discover-rg" \
  --name "discover-api"
```

### Check Function Status

```bash
# List functions
az functionapp function list \
  --resource-group "discover-rg" \
  --name "discover-api"
```

### Common Issues

1. **Authentication Error**: Make sure UAMI is assigned and has proper permissions
2. **404 Error**: Check if functions are deployed correctly
3. **500 Error**: Check function logs for detailed error messages

## Cost Monitoring

- **Free Tier**: 1 million requests per month
- **Monitor**: Check Azure Portal → Function App → Metrics
- **Set Alerts**: Configure spending limits in Azure Portal

## Next Steps

After successful deployment, proceed to update your frontend code to use these API endpoints. 