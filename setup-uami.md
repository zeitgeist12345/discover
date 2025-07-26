# 🔧 Cosmos DB Upload Setup Guide

## Prerequisites

1. **Azure CLI** installed and logged in
2. **Node.js** (v14 or higher)
3. **User-Assigned Managed Identity** with Cosmos DB access

## 🚀 Quick Setup

### 1. Install Dependencies
```bash
npm install
```

### 2. Get Your UAMI Client ID
```bash
# List your user-assigned managed identities
az identity list --resource-group "discover-rg" --query "[].{name:name, clientId:clientId}" -o table

# Or get a specific UAMI
az identity show --resource-group "discover-rg" --name "your-uami-name" --query "clientId" -o tsv
```

### 3. Set Environment Variable
```bash
# Replace with your actual UAMI client ID
export AZURE_CLIENT_ID="your-uami-client-id-here"
```

### 4. Run the Upload
```bash
npm run upload
```

## 🔐 UAMI Setup (if not already configured)

### Create UAMI
```bash
az identity create --resource-group "discover-rg" --name "discover-uami"
```

### Assign Cosmos DB Role
```bash
# Get the UAMI principal ID
UAMI_PRINCIPAL_ID=$(az identity show --resource-group "discover-rg" --name "discover-uami" --query "principalId" -o tsv)

# Assign Cosmos DB Contributor role
az role assignment create \
  --assignee $UAMI_PRINCIPAL_ID \
  --role "Cosmos DB Account Contributor" \
  --scope "/subscriptions/b7304d11-19c2-42f9-ae77-6649704522f8/resourceGroups/discover-rg/providers/Microsoft.DocumentDB/databaseAccounts/discover-cosmosdb"
```

## 📊 Expected Output

```
🚀 Starting upload to Cosmos DB...
🔧 Setting up Cosmos DB...
✅ Database 'discover-db' ready
✅ Container 'websites' ready
📊 Found 30 websites to upload
✅ Uploaded: skribbl.io
✅ Uploaded: zeitgeist12345
...
📈 Upload Summary:
✅ Successful: 30
❌ Failed: 0
📊 Total: 30
🎉 Upload process completed!
```

## 🔍 Troubleshooting

### Common Issues

1. **Authentication Error**: Make sure UAMI has proper permissions
2. **Network Error**: Check if you're connected to Azure
3. **Container Not Found**: Run `npm run setup` to create database/container

### Debug Mode
```bash
# Enable verbose logging
DEBUG=azure:* npm run upload
```

## 📁 File Structure

```
discover/
├── upload-to-cosmosdb.js    # Main upload script
├── websites.js              # Websites data (modified to export)
├── package.json             # Node.js dependencies
├── setup-uami.md           # This guide
└── ... (other files)
``` 