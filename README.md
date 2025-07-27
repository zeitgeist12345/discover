# 🌐 Discover - Random Website Discovery App

A modern web application that helps you discover amazing websites from a curated collection. Features include random website loading, view tracking, like/dislike functionality, and the ability to add new websites.

## ✨ Features

- 🎲 **Random Website Discovery**: Click to load random websites from a curated collection
- 📊 **View Tracking**: Track views, likes, and dislikes for each website with optimistic UI updates
- ➕ **Add Websites**: Submit new websites to the database through a user-friendly form
- 🎨 **Modern UI**: Clean, responsive design with smooth animations and intuitive layout
- ☁️ **Cloud Backend**: Azure Functions with Cosmos DB for scalable data storage
- 🔐 **Secure Authentication**: User-Assigned Managed Identity (UAMI) for secure database access
- 🛡️ **Content Filtering**: Automatic filtering of harmful content based on user feedback
- 📊 **Data Analysis Tools**: Built-in tools for analyzing website data and filtering statistics

## 🛡️ Content Filtering Algorithm

The app uses a sophisticated content filtering system to prevent harmful content from being displayed:

### Filtering Logic:
- **Sites with ≤3 total votes**: Always shown (need more data to assess)
- **Sites with >3 total votes**: Filtered based on dislike percentage
- **Threshold**: Sites with ≥80% dislikes are filtered out

### Formula:
```
undesirable_score = dislikes / total_votes
if undesirable_score >= 0.8 (80% dislikes) → Filtered out
if undesirable_score < 0.8 → Shown
```

### Examples:
- **Site with 2 likes, 8 dislikes**: 80% dislikes → **Filtered out**
- **Site with 5 likes, 5 dislikes**: 50% dislikes → **Shown**
- **Site with 8 likes, 2 dislikes**: 20% dislikes → **Shown**
- **Site with 0 votes**: Always **shown**

This ensures that truly problematic content is automatically hidden while allowing healthy debate and diverse opinions.

## 🛠️ Tools

### Website Data Dump Tool

Located in `tools/dump-websites.js`, this tool provides comprehensive data analysis:

```bash
# Run the dump tool
node tools/dump-websites.js
```

**Output Files:**
- `tools/websites-dump.json` - Raw website data (all websites including filtered ones)
- `tools/websites-summary.json` - Analysis and statistics

**Features:**
- Fetches ALL websites from database (including filtered ones)
- Applies content filtering algorithm locally
- Generates detailed statistics and analysis
- Shows filtering reasons for each website
- Provides breakdown by dislike score and vote count

**Example Usage:**
```bash
# Get only filtered websites
cat tools/websites-dump.json | jq '[.[] | select(.filterAnalysis.isFiltered == true)]'

# Get summary statistics
cat tools/websites-summary.json | jq '.analysis'

# Count total websites
cat tools/websites-summary.json | jq '.metadata.totalWebsites'
```

## 🚀 Quick Start

### Prerequisites
- Azure CLI installed and logged in
- Node.js and npm installed
- Azure subscription with appropriate permissions

### 1. Clone and Setup
```bash
git clone <repository-url>
cd discover
```

### 2. Install Dependencies
```bash
cd api
npm install
```

### 3. Deploy Azure Functions
```bash
# Create deployment package
zip -r deploy.zip . -x "*.git*" "*.vscode*" "local.settings.json"

# Deploy to Azure
az functionapp deployment source config-zip --resource-group discover-rg --name discover-api --src deploy.zip
```

### 4. Configure CORS
```bash
# Add allowed origins for local development
az functionapp cors add --resource-group discover-rg --name discover-api --allowed-origins "http://127.0.0.1:8000"
az functionapp cors add --resource-group discover-rg --name discover-api --allowed-origins "http://localhost:8000"
```

### 5. Run Frontend
```bash
# Start a local server (e.g., Python)
python -m http.server 8000

# Or use any other local server
# Open http://127.0.0.1:8000 in your browser
```

## 📡 API Endpoints

**Base URL**: `https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api`

### Get All Websites (Filtered)
```bash
curl -X GET "https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api/getwebsites"
```

### Get All Websites (Including Filtered Ones)
```bash
curl -X GET "https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api/getwebsites?all=true"
```

### Add New Website
```bash
curl -X POST "https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api/addwebsite" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Example Site",
    "url": "https://example.com",
    "description": "A great website"
  }'
```

### Increment View/Like/Dislike
```bash
curl -X POST "https://discover-api-g0c4bgbhgpeah7dt.uaenorth-01.azurewebsites.net/api/incrementview" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "website-id",
    "url": "https://example.com",
    "action": "like"
  }'
```

## 🏗️ Architecture

- **Frontend**: HTML, CSS, JavaScript (vanilla)
- **Backend**: Azure Functions (Node.js)
- **Database**: Azure Cosmos DB (NoSQL)
- **Authentication**: User-Assigned Managed Identity (UAMI)
- **Deployment**: Azure Function App

## 🔧 Configuration

The app uses environment variables for configuration:
- `COSMOS_ENDPOINT`: Azure Cosmos DB endpoint
- `AZURE_CLIENT_ID`: User-Assigned Managed Identity client ID

## 📊 Data Analysis

The project includes tools for analyzing website data and filtering statistics:

- **Content Filtering Analysis**: Understand how the filtering algorithm works
- **Website Statistics**: View distribution by vote count and dislike percentage
- **Filtering Reasons**: See why specific websites are shown or filtered
- **Data Export**: Export raw data for further analysis

## 📝 License

This project is open source and available under the MIT License.
