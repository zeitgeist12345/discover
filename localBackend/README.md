# 🌐 Local Backend Setup

A containerized **Node.js backend** with a **MySQL database** for the Discover website.  
The backend automatically creates the database and inserts sample data on first run.

## ⚙️ Prerequisites

- Docker
- Docker Compose

## 🚀 Quick Start

### 1. Start the containers
```bash
cd discover/localBackend
sudo docker-compose up -d --build
```

### 2. Verify it's working
```bash
curl http://backend.discoverall.space/getWebsitesDesktop
curl http://backend.discoverall.space/health
```

## 🔧 Management Commands

```bash
# View logs
sudo docker-compose logs app
sudo docker-compose logs db

# Stop containers
sudo docker-compose down

# Restart containers
sudo docker-compose restart

# Rebuild after code changes
sudo docker-compose up -d --build
```
