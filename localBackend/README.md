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

## 💻 Access Services

- **Node.js API**: http://localhost:3001
- **MySQL Database**: localhost:3307  
- **phpMyAdmin**: http://localhost:8081 (user: `root`, pass: `password`)  

> 📝 **Note:**  
> Port `3307` is mapped to MySQL’s internal port `3306` to avoid conflicts with local MySQL instances.  
> Similarly, phpMyAdmin runs on `8081` to prevent port clashes.

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

## ✅ Troubleshooting Tips

- **Port already in use**
  ```bash
  sudo lsof -i :3001
  sudo lsof -i :3306
  sudo lsof -i :8080
  ```
  Kill old processes:
  ```bash
  sudo pkill -f docker-proxy
  ```

- **Check container logs**
  ```bash
  sudo docker-compose logs
  ```

- **Rebuild cleanly**
  ```bash
  sudo docker-compose down -v
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