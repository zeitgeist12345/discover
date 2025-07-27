# 🌐 Discover - Random Website Discovery App

A modern web application that helps you discover amazing websites from a curated collection. Features include random website loading, view tracking, like/dislike functionality, and the ability to add new websites.

## ✨ Features

- 🎲 **Random Website Discovery**: Click to load random websites from a curated collection
- 📊 **View Tracking**: Track views, likes, and dislikes for each website with optimistic UI updates
- ➕ **Add Websites**: Submit new websites to the database through a user-friendly form
- 🎨 **Modern UI**: Clean, responsive design with smooth animations and intuitive layout
- ☁️ **Cloud Backend**: Azure Functions with Cosmos DB for scalable data storage
- 🔐 **Secure Authentication**: User-Assigned Managed Identity (UAMI) for secure database access
- 🌍 **CORS Ready**: Properly configured for cross-origin requests

## 🏗️ Architecture

- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Backend**: Azure Functions (Node.js v3 programming model)
- **Database**: Azure Cosmos DB (NoSQL with URL partition key)
- **Authentication**: User-Assigned Managed Identity (UAMI)
- **Deployment**: Azure Function App with consumption plan

## 🚀 Quick Start

### Option 1: Use the Live Application
1. Open `index.html` in your browser
2. Click "🎲 Load Random Website" to discover sites
3. Websites open in new tabs for the best experience
4. Use like/dislike buttons to rate websites
5. Add new websites using the "➕ Add Website" button

### Option 2: Local Development
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd discover
   ```

2. **Open the frontend**
   - Open `index.html` in your browser
   - Or serve it using a local server: `python -m http.server 8000`

3. **Configure the API** (if using Azure backend)
   - Update `config.js` with your Azure Function App URL
   - Set `USE_API: true` to enable backend features

## ☁️ Azure Deployment Guide

### Prerequisites

- Azure CLI installed and logged in (`az login`)
- Azure subscription with appropriate permissions
- Node.js 18+ installed

### Step 1: Create Azure Resources

```bash
# Create resource group
az group create --name discover-rg --location uaenorth

# Create Cosmos DB account
az cosmosdb create \
  --name discover-cosmos \
  --resource-group discover-rg \
  --locations regionName=uaenorth \
  --capabilities EnableServerless

# Create Cosmos DB database and container
az cosmosdb sql database create \
  --account-name discover-cosmos \
  --resource-group discover-rg \
  --name websites

az cosmosdb sql container create \
  --account-name discover-cosmos \
  --resource-group discover-rg \
  --database-name websites \
  --name list \
  --partition-key-path "/url"

# Create Function App
az functionapp create \
  --name discover-api \
  --resource-group discover-rg \
  --consumption-plan-location uaenorth \
  --runtime node \
  --runtime-version 18 \
  --functions-version 4 \
  --os-type Linux \
  --storage-account discoverstorage$(date +%s)
```

### Step 2: Setup User-Assigned Managed Identity

```bash
# Create UAMI
az identity create --name discover-uami --resource-group discover-rg

# Get UAMI details
UAMI_ID=$(az identity show --name discover-uami --resource-group discover-rg --query id -o tsv)
UAMI_PRINCIPAL_ID=$(az identity show --name discover-uami --resource-group discover-rg --query principalId -o tsv)
UAMI_CLIENT_ID=$(az identity show --name discover-uami --resource-group discover-rg --query clientId -o tsv)

# Assign UAMI to Function App
az functionapp identity assign \
  --name discover-api \
  --resource-group discover-rg \
  --identities $UAMI_ID

# Grant Cosmos DB permissions to UAMI (Contributor role)
az role assignment create \
  --assignee $UAMI_PRINCIPAL_ID \
  --role "Contributor" \
  --scope "/subscriptions/$(az account show --query id -o tsv)/resourceGroups/discover-rg/providers/Microsoft.DocumentDB/databaseAccounts/discover-cosmos"
```

### Step 3: Configure Application Settings

```bash
# Get Cosmos DB endpoint
COSMOS_ENDPOINT=$(az cosmosdb show --name discover-cosmos --resource-group discover-rg --query documentEndpoint -o tsv)

# Set Function App settings
az functionapp config appsettings set \
  --name discover-api \
  --resource-group discover-rg \
  --settings \
  "COSMOS_ENDPOINT=$COSMOS_ENDPOINT" \
  "AZURE_CLIENT_ID=$UAMI_CLIENT_ID"
```

### Step 4: Deploy Functions

```bash
# Navigate to api directory
cd api

# Install dependencies
npm install

# Create deployment package (include node_modules)
zip -r deploy.zip . -x "*.git*" "*.vscode*" "local.settings.json"

# Deploy to Azure
az functionapp deployment source config-zip \
  --resource-group discover-rg \
  --name discover-api \
  --src ./deploy.zip
```

### Step 5: Upload Sample Data

```bash
# Upload websites data to Cosmos DB
node upload-to-cosmosdb.js
```

### Step 6: Test Deployment

```bash
# Get your Function App URL
FUNCTION_URL=$(az functionapp show --name discover-api --resource-group discover-rg --query defaultHostName -o tsv)

# Test getWebsites function
curl -X GET "https://$FUNCTION_URL/api/getwebsites"

# Test addWebsite function
curl -X POST "https://$FUNCTION_URL/api/addwebsite" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Site","url":"https://example.com","description":"A test website"}'
```

## ⚙️ Configuration

### Frontend Configuration (`config.js`)

```javascript
const CONFIG = {
    API_BASE_URL: 'https://your-function-app.azurewebsites.net/api',
    USE_API: true,                    // Enable/disable API usage
    ENABLE_VIEW_TRACKING: true,       // Enable view/like/dislike tracking
    ENABLE_FALLBACK: true             // Enable fallback to static data
};
```

### Backend Configuration

The Azure Functions use these environment variables:
- `COSMOS_ENDPOINT`: Cosmos DB endpoint URL
- `AZURE_CLIENT_ID`: User-Assigned Managed Identity client ID
- `DATABASE_NAME`: Database name (websites)
- `CONTAINER_NAME`: Container name (list)

## 🔌 API Endpoints

### GET /api/getwebsites
Returns all active websites from the database.

**Response:**
```json
[
  {
    "id": "website-1",
    "name": "Example Site",
    "url": "https://example.com",
    "description": "A great website",
    "views": 10,
    "likes": 5,
    "dislikes": 1
  }
]
```

### POST /api/incrementview
Increments view, like, or dislike count for a website.

**Parameters:**
- `id`: Website ID
- `url`: Website URL (used as partition key)
- `action`: Action type (view, like, dislike)

**Response:**
```json
{
  "success": true,
  "message": "view incremented successfully",
  "website": {
    "id": "website-1",
    "name": "Example Site",
    "views": 11,
    "likes": 5,
    "dislikes": 1
  }
}
```

### POST /api/addwebsite
Adds a new website to the database.

**Request Body:**
```json
{
  "name": "New Website",
  "url": "https://newexample.com",
  "description": "A new amazing website",
  "category": "user-submitted"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Website added successfully",
  "website": {
    "id": "website-123",
    "name": "New Website",
    "url": "https://newexample.com",
    "description": "A new amazing website",
    "category": "user-submitted"
  }
}
```

## 🛠️ Troubleshooting

### Common Issues

1. **CORS Errors**
   - CORS is configured in both Azure Portal and function code
   - Ensure your frontend domain is added to CORS allowed origins
   - Check browser console for specific CORS error messages

2. **404 Errors**
   - Function names in URLs are case-sensitive (use lowercase)
   - Verify functions are deployed correctly: `az functionapp function list --name discover-api --resource-group discover-rg`

3. **Authentication Errors**
   - Verify UAMI is properly assigned to Function App
   - Check UAMI has correct permissions on Cosmos DB
   - Ensure `AZURE_CLIENT_ID` is set correctly

4. **Partition Key Errors**
   - Cosmos DB container must use `/url` as partition key
   - All operations must include the URL as partition key

### Debugging

```bash
# View Function App logs
az webapp log tail --resource-group discover-rg --name discover-api

# Test API endpoints
node test-api.js

# Check function status
az functionapp function list --name discover-api --resource-group discover-rg
```

## 🧪 Testing

### API Testing
```bash
# Run the test script
node test-api.js
```

### Manual Testing
1. Open the application in browser
2. Test random website loading
3. Test like/dislike functionality
4. Test adding new websites
5. Verify optimistic UI updates

## 📁 Project Structure

```
discover/
├── index.html              # Main web application
├── script.js               # Frontend JavaScript logic
├── styles.css              # Application styling
├── config.js               # Configuration settings
├── README.md               # This file
├── api/                    # Azure Functions (v3)
│   ├── getWebsites/        # Get all websites
│   ├── incrementView/      # Update view/like/dislike counts
│   ├── addWebsite/         # Add new websites
│   ├── package.json        # Function dependencies
│   └── host.json           # Function host configuration
├── websites.js             # Static website data (fallback)
├── websites-node.js        # Node.js version of website data
├── upload-to-cosmosdb.js   # Data upload utility
└── test-api.js             # API testing utility
```

## 🔄 Development Workflow

### Adding New Features
1. Develop and test locally
2. Update Azure Functions if needed
3. Deploy using the deployment steps above
4. Test in production environment

### Data Management
- **Add websites**: Use the "Add Website" feature in the UI
- **Bulk upload**: Use `upload-to-cosmosdb.js` script
- **View data**: Access Cosmos DB through Azure Portal

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

If you encounter any issues:
1. Check the troubleshooting section above
2. Review Azure Function logs
3. Test API endpoints manually
4. Create an issue in the repository

---

**Note**: This application is designed for educational and discovery purposes. Please respect the websites you visit and their terms of service. Websites open in new tabs for the best experience.
