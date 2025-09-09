# Docker Setup for Logo API

This document explains how to run the Logo API using Docker and Docker Compose.

## Prerequisites

- Docker and Docker Compose installed
- Ports 5433, 8083, and 8082 available on your machine

## Quick Start

### 1. Start All Services

```bash
# Start PostgreSQL, Adminer, and the Logo API
docker-compose up -d
```

This will start:
- **PostgreSQL** on port 5433
- **Adminer** (database admin) on port 8083  
- **Logo API** on port 8082

### 2. Access the Services

- **API**: http://localhost:8082
- **API Health**: http://localhost:8082/q/health (if health extension is added)
- **Adminer**: http://localhost:8083
- **API Documentation**: http://localhost:8082/q/swagger-ui (if swagger extension is added)

### 3. Test the API

```bash
# Test the hello endpoint
curl http://localhost:8082/hello

# Create a logo
curl -X POST http://localhost:8082/hello/logos \
  -H "Content-Type: application/json" \
  -d '{"externalIdentifier": "test-logo-001", "resourceUrl": "https://example.com/logo.png", "filePath": "/files/logo.png"}'

# Get all logos
curl http://localhost:8082/hello/logos

# Get logo by external identifier
curl http://localhost:8082/hello/logos/external/test-logo-001
```

## Database Access

### Using Adminer
1. Open http://localhost:8083
2. Login with:
   - **System**: PostgreSQL
   - **Server**: logo-api-postgresql
   - **Username**: logo_user
   - **Password**: logo_password
   - **Database**: logo_db

### Direct Connection
```bash
# Connect to PostgreSQL directly
docker exec -it logo-api-postgres psql -U logo_user -d logo_db
```

## Development Commands

### Build Only the Application
```bash
# Build the Docker image
docker-compose build logo-api-app
```

### Start Only Database Services
```bash
# Start only PostgreSQL and Adminer
docker-compose up -d logo-api-postgresql logo-api-adminer
```

### View Logs
```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f logo-api-app
docker-compose logs -f logo-api-postgresql
```

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes database data)
docker-compose down -v
```

## Configuration

### Environment Variables

The application can be configured using environment variables:

```bash
# Database
PGHOST=logo-api-postgresql
PGPORT=5432
PGDATABASE=logo_db
DATABASE_USERNAME=logo_user
DATABASE_PASSWORD=logo_password

# Application
PORT=8082
QUARKUS_PROFILE=prod
DEV_SERVICES_ENABLED=false
```

### Custom Configuration

To use custom environment variables, create a `.env` file:

```bash
# .env
DATABASE_PASSWORD=your_secure_password
PORT=9090
```

Then start with:
```bash
docker-compose up -d
```

## Database Schema

The database schema is automatically managed by Liquibase. On first startup, it will:

1. Create the `logo` table with columns:
   - `id` (BIGSERIAL, primary key)
   - `external_identifier` (VARCHAR, unique, indexed)
   - `resource_url` (VARCHAR)
   - `file_path` (VARCHAR) 
   - `created_at` (TIMESTAMP WITH TIME ZONE)
   - `updated_at` (TIMESTAMP WITH TIME ZONE)

2. Create indexes and constraints for optimal performance

## Troubleshooting

### Port Conflicts
If you get port binding errors:
```bash
# Check what's using the ports
lsof -i :5432
lsof -i :8080
lsof -i :8082

# Or modify docker-compose.yml to use different ports
```

### Database Connection Issues
```bash
# Check if PostgreSQL is ready
docker-compose exec logo-api-postgresql pg_isready -U logo_user

# View database logs
docker-compose logs logo-api-postgresql
```

### Application Won't Start
```bash
# Check application logs
docker-compose logs logo-api-app

# Rebuild the application
docker-compose build --no-cache logo-api-app
```

## Production Deployment

For production deployment:

1. Update the database credentials in `docker-compose.yml`
2. Set `QUARKUS_PROFILE=prod` 
3. Consider using Docker secrets for sensitive data
4. Add proper volume mounts for data persistence
5. Configure reverse proxy (nginx) for SSL termination
