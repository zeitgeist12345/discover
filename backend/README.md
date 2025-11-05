# 🌐 Local Backend Setup
A containerized Node.js (v18, Alpine) backend with MySQL 8.0, automatically creating the database and inserting sample data on first run.

Publicly exposed at https://backend.discoverall.space via Cloudflare Tunnel to forward requests to the local Node.js app on port 3000.

## ⚙️ Prerequisites
- Docker
- Docker Compose

## 🚀 Quick Start

### 1. Start the containers
```bash
cd backend
docker compose up -d --build
```

### 2. Verify it's working
```bash
curl http://backend.discoverall.space/getWebsitesDesktop
curl http://backend.discoverall.space/health
```

## 🔧 Management Commands
```bash
# View logs
docker compose logs app
docker compose logs db
docker compose logs cloudflared

# Stop containers
docker compose down

# Restart containers
docker compose restart

# Rebuild after code changes
docker compose up -d --build

# Always use docker desktop
docker context use desktop-linux
```
