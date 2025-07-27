# Discover - Random Website Discovery App

A web application that helps you discover amazing websites from a curated collection. Features include random website loading, view tracking, and like/dislike functionality.

## Features

- 🎲 **Random Website Discovery**: Click to load random websites from a curated collection
- 📊 **View Tracking**: Track views, likes, and dislikes for each website
- 🎨 **Modern UI**: Clean, responsive design with smooth animations
- ☁️ **Cloud Backend**: Azure Functions with Cosmos DB for data storage
- 🔐 **Secure Authentication**: User-Assigned Managed Identity (UAMI) for database access

## Architecture

- **Frontend**: HTML, CSS, JavaScript (vanilla)
- **Backend**: Azure Functions (Node.js)
- **Database**: Azure Cosmos DB (NoSQL)
- **Authentication**: User-Assigned Managed Identity (UAMI)
- **Deployment**: Azure Function App

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd discover
   ```

2. **Open the frontend**
   - Open `index.html` in your browser
   - Or serve it using a local server

3. **Use the application**
   - Click "🎲 Load Random Website" to discover sites
   - Websites open in new tabs for the best experience
   - Use like/dislike buttons to rate websites

## Azure Deployment

### Prerequisites

- Azure CLI installed and logged in
- Azure subscription with appropriate permissions
- Node.js installed

### 1. Create Azure Resources

```bash
# Create resource group
az group create --name discover-rg --location uaenorth

# Create Cosmos DB account
az cosmosdb create --name discover-cosmos --resource-group discover-rg --locations regionName=uaenorth

# Create Cosmos DB database and container
az cosmosdb sql database create --account-name discover-cosmos --resource-group discover-rg --name discover-db
az cosmosdb sql container create --account-name discover-cosmos --resource-group discover-rg --database-name discover-db --name websites --partition-key-path "/url"

# Create Function App
az functionapp create --name discover-api --resource-group discover-rg --consumption-plan-location uaenorth --runtime node --runtime-version 18 --functions-version 4 --os-type Linux
```

### 2. Setup User-Assigned Managed Identity

```bash
# Create UAMI
az identity create --name discover-uami --resource-group discover-rg

# Get UAMI details
UAMI_ID=$(az identity show --name discover-uami --resource-group discover-rg --query id -o tsv)
UAMI_PRINCIPAL_ID=$(az identity show --name discover-uami --resource-group discover-rg --query principalId -o tsv)

# Assign UAMI to Function App
az functionapp identity assign --name discover-api --resource-group discover-rg --identities $UAMI_ID

# Grant Cosmos DB permissions to UAMI
az cosmosdb sql role assignment create --account-name discover-cosmos --resource-group discover-rg --scope "/" --principal-id $UAMI_PRINCIPAL_ID --role-definition-id "00000000-0000-0000-0000-000000000002"
```

### 3. Configure Application Settings

```bash
# Get Cosmos DB connection details
COSMOS_ENDPOINT=$(az cosmosdb show --name discover-cosmos --resource-group discover-rg --query documentEndpoint -o tsv)
COSMOS_KEY=$(az cosmosdb keys list --name discover-cosmos --resource-group discover-rg --query primaryMasterKey -o tsv)

# Set Function App settings
az functionapp config appsettings set --name discover-api --resource-group discover-rg --settings \
  "COSMOS_ENDPOINT=$COSMOS_ENDPOINT" \
  "COSMOS_KEY=$COSMOS_KEY" \
  "DATABASE_NAME=discover-db" \
  "CONTAINER_NAME=websites" \
  "UAMI_CLIENT_ID=$(az identity show --name discover-uami --resource-group discover-rg --query clientId -o tsv)"
```

### 4. Deploy Functions

```bash
# Navigate to api directory
cd api

# Install dependencies
npm install

# Create deployment package
zip -r deploy.zip . -x "*.git*" "*.vscode*" "local.settings.json"

# Deploy to Azure
az functionapp deployment source config-zip -g discover-rg -n discover-api --src ./deploy.zip
```

### 5. Upload Sample Data

```bash
# Upload websites data to Cosmos DB
node upload-to-cosmosdb.js
```

### 6. Test Deployment

```bash
# Test getWebsites function
curl -X GET "https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api/getwebsites"

# Test incrementView function
curl -X POST "https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api/incrementview?id=website-1&url=https://example.com&action=view"
```

## Configuration

### Frontend Configuration (`config.js`)

```javascript
const CONFIG = {
    API_BASE_URL: 'https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api',
    USE_API: true,
    ENABLE_VIEW_TRACKING: true,
    ENABLE_FALLBACK: true
};
```

### Backend Configuration

The Azure Functions use the following environment variables:
- `COSMOS_ENDPOINT`: Cosmos DB endpoint
- `COSMOS_KEY`: Cosmos DB access key
- `DATABASE_NAME`: Database name (discover-db)
- `CONTAINER_NAME`: Container name (websites)
- `UAMI_CLIENT_ID`: User-Assigned Managed Identity client ID

## API Endpoints

### GET /api/getwebsites
Returns all active websites from the database.

### POST /api/incrementview
Increments view, like, or dislike count for a website.

**Parameters:**
- `id`: Website ID
- `url`: Website URL (used as partition key)
- `action`: Action type (view, like, dislike)

## Troubleshooting

### Common Issues

1. **CORS Errors**: CORS is configured in Azure Portal and in function code
2. **404 Errors**: Ensure function names are lowercase in URLs
3. **Authentication Errors**: Verify UAMI is properly assigned and has correct permissions
4. **Partition Key Errors**: Ensure Cosmos DB container uses `/url` as partition key

### Logs

```bash
# View Function App logs
az webapp log tail --resource-group discover-rg --name discover-api
```

## Development

### Local Development

1. Install dependencies: `npm install`
2. Set up local.settings.json with your Azure configuration
3. Run functions locally: `func start`

### Adding New Websites

1. Edit `websites.js` to add new websites
2. Run `node upload-to-cosmosdb.js` to upload to database
3. Redeploy functions if needed

## License

This project is open source and available under the [MIT License](LICENSE).

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

---

**Note**: This application is designed for educational and discovery purposes. Please respect the websites you visit and their terms of service.
