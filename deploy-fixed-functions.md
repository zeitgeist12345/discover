# 🚀 Deploy Fixed Azure Functions with CORS Support

## ✅ What's Fixed

### **CORS Configuration**
- ✅ **Localhost support**: `http://localhost:8000`, `http://localhost:3000`
- ✅ **Production support**: `https://zeitgeist12345.github.io`
- ✅ **Dynamic origin detection**: Automatically detects and allows the correct origin
- ✅ **Preflight support**: OPTIONS requests handled properly

### **Function Updates**
- ✅ **getWebsites**: Enhanced with proper CORS headers
- ✅ **incrementView**: Supports view, like, dislike actions with CORS
- ✅ **function.json**: Updated to support OPTIONS method
- ✅ **Error handling**: Proper error responses with CORS headers

## 📋 Deployment Steps

### Step 1: Create Updated Package
```bash
cd api
zip -r api-functions-fixed.zip . -x "node_modules/*" "*.zip"
```

### Step 2: Deploy to Azure
```bash
az functionapp deployment source config-zip \
  --resource-group "discover-rg" \
  --name "discover-api" \
  --src "api-functions-fixed.zip"
```

### Step 3: Verify Deployment
```bash
# Test getWebsites endpoint
curl -H "Origin: http://localhost:8000" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     https://discover-api.azurewebsites.net/api/getWebsites

# Test incrementView endpoint
curl -H "Origin: http://localhost:8000" \
     -H "Access-Control-Request-Method: POST" \
     -X OPTIONS \
     https://discover-api.azurewebsites.net/api/incrementView
```

## 🧪 Testing

### Test Local Development
1. Start local server: `python3 -m http.server 8000`
2. Visit: `http://localhost:8000`
3. Check console: Should see "Running on localhost - API mode enabled for testing"
4. Try loading a random website
5. Test like/dislike buttons

### Test Production
1. Visit: [https://zeitgeist12345.github.io/discover/](https://zeitgeist12345.github.io/discover/)
2. Check console: Should see "Running in production - API mode enabled"
3. Test all functionality

## 🔧 CORS Origins Supported

The functions now support these origins:
- `https://zeitgeist12345.github.io` (production)
- `http://localhost:8000` (local testing)
- `http://localhost:3000` (alternative local port)
- `http://127.0.0.1:8000` (IP-based localhost)
- `http://127.0.0.1:3000` (IP-based alternative)

## 📊 Expected Behavior

### **Local Development** (`http://localhost:8000`)
- ✅ API calls work without CORS errors
- ✅ View tracking enabled
- ✅ Like/dislike functionality works
- ✅ Optimistic UI updates

### **Production** (`https://zeitgeist12345.github.io`)
- ✅ API calls work without CORS errors
- ✅ View tracking enabled
- ✅ Like/dislike functionality works
- ✅ Optimistic UI updates

### **Direct File Access** (`file://`)
- ✅ Falls back to static mode
- ✅ No CORS issues
- ✅ Basic functionality works

## 🐛 Troubleshooting

### CORS Still Not Working?
1. **Check deployment**: Ensure functions are deployed successfully
2. **Verify origin**: Check if your origin is in the allowed list
3. **Test manually**: Use curl commands above to test CORS
4. **Check logs**: `az webapp log tail --resource-group "discover-rg" --name "discover-api"`

### Function Not Responding?
1. **Check function status**: `az functionapp function list --resource-group "discover-rg" --name "discover-api"`
2. **Test direct access**: `curl https://discover-api.azurewebsites.net/api/getWebsites`
3. **Check authentication**: Ensure managed identity is configured

## 🎯 Success Indicators

After successful deployment:
- ✅ No CORS errors in browser console
- ✅ "Running on localhost - API mode enabled for testing" (local)
- ✅ "Running in production - API mode enabled" (production)
- ✅ Like/dislike buttons work with optimistic updates
- ✅ View counts increment automatically
- ✅ All 31 websites load from API

## 📈 Monitoring

- **Azure Portal** → Function App → Monitor → Logs
- **Application Insights** (if enabled) for detailed analytics
- **Cosmos DB** → Data Explorer to see view/like/dislike counts 