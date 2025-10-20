# 🌐 Local Backend Setup

A containerized **Node.js backend** with a **MySQL database** for the Discover website.  
The backend automatically creates the database and inserts sample data on first run.

## ⚙️ Prerequisites

- Docker
- Docker Compose

## 🚀 Quick Start

### 1. Clone and setup
```bash
git clone <your-repo>
cd discover/localBackend
```

### 2. Start the containers
```bash
sudo docker-compose up -d --build
```

### 4. Verify it's working
```bash
curl http://localhost:3001/getWebsitesDesktop
curl http://localhost:3001/health
```

## 🌐 API Endpoints

- `GET /` — API info  
- `GET /getWebsitesDesktop` — Get all websites  
- `POST /incrementViewDesktop` — Update website stats  
- `POST /addwebsite` — Add new website  
- `POST /init` — Initialize database  
- `GET /health` — Health check  

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

## 🧩 Folder Structure

```
localBackend/
├── backend/               # Node.js app source
│   ├── package.json
│   ├── server.js
│   ├── routes/
│   └── db/
├── docker-compose.yml     # Docker Compose configuration
└── README.md              # This file
```