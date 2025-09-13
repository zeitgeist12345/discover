# Local Backend Setup

A containerized Node.js backend with MySQL database for the Discover website.

## Prerequisites

- Docker
- Docker Compose

## Quick Start

### 1. Clone and setup
```bash
git clone <your-repo>
cd discover/localBackend
```

### 2. Start the containers
```bash
sudo docker-compose up -d --build
```

### 3. Initialize the database
```bash
# Wait 15 seconds for MySQL to start, then:
curl -X POST http://localhost:3000/init
```

### 4. Verify it's working
```bash
curl http://localhost:3000/getWebsitesDesktop
curl http://localhost:3000/health
```

## API Endpoints

- `GET /` - API info
- `GET /getWebsitesDesktop` - Get all websites
- `POST /incrementViewDesktop` - Update website stats
- `POST /addwebsite` - Add new website  
- `POST /init` - Initialize database
- `GET /health` - Health check

## Access Services

- **Node.js API**: http://localhost:3000
- **MySQL Database**: localhost:3306
- **phpMyAdmin**: http://localhost:8080 (user: `root`, pass: `password`)

## Management Commands

```bash
# View logs
sudo docker-compose logs app
sudo docker-compose logs db

# Stop containers
sudo docker-compose down

# Restart
sudo docker-compose restart

# Rebuild after code changes
sudo docker-compose up -d --build
```

## Project Structure
```
localBackend/
├── docker-compose.yml
├── backend/
│   ├── Dockerfile
│   ├── app.js
│   └── package.json
└── database/
    └── init.sql
```

The backend automatically creates the database and sample data on first run.