# 🌏 Local Backend Setup
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
curl http://backend.discoverall.space/getLinksDesktop
curl http://backend.discoverall.space/health
```

### 3. Reinitialize the links database

docker compose exec db mysql -u root -ppassword mydatabase -e "DROP TABLE links;"
cat db/init.sql | docker compose exec -T db mysql -u root -ppassword mydatabase

### 4. Get SQL data directly
Run in the `backend-db-1` docker container `Exec`.
```
sh-5.1# mysql -u root -p
# password is password
mysql> SHOW DATABASES;
mysql> USE mydatabase
mysql> SELECT * FROM visitors ORDER BY timestamp DESC;
mysql> SELECT * FROM visitors WHERE country NOT IN ('IN') ORDER BY timestamp DESC;
```

## 🔧 Management Commands
```bash
# View logs
docker compose logs app
docker compose logs db
docker compose logs cloudflared

# Stop containers
docker compose down

# Delete all SQL data
docker volume rm $(basename $(pwd))_mysql_data

# Rebuild after code changes
docker compose up -d --build

# Restart containers
docker compose restart

# Always use docker desktop
docker context use desktop-linux
```
