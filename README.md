# Campus Secondhand Trading Platform

A secondhand trading platform for campus students, built with Spring Boot 3 + Vue 3, deployed on cloud services.

---

## Project Overview

This platform provides complete features for campus secondhand trading:
- Product publishing with image upload
- Search and filter products
- User authentication (JWT)
- Real-time messaging
- Transaction management
- Review system

---

## Directory Structure

```
campus-secondhand/
│
├── campus-secondhand-backend/     # Backend (Spring Boot 3)
│   ├── src/main/java/
│   │   └── com/campus/secondhand/
│   │       ├── controller/        # REST API
│   │       ├── service/           # Business logic
│   │       ├── mapper/            # MyBatis Mapper
│   │       ├── entity/            # Data entities
│   │       ├── dto/               # Request/Response DTOs
│   │       ├── exception/         # Exception handling
│   │       └── utils/             # Utilities (JWT)
│   ├── src/main/resources/
│   │   ├── application.yml        # Base config
│   │   ├── huawei-cloud-example.yml # Config template
│   │   └── mapper/*.xml           # MyBatis XML
│   └── pom.xml                    # Maven config
│
├── campus-secondhand-frontend/    # Frontend (Vue 3)
│   ├── src/
│   │   ├── views/                 # Page views
│   │   ├── api/                   # API requests
│   │   ├── router/                # Vue Router
│   │   ├── store/                 # Pinia state
│   │   └── styles/                # Global styles
│   ├── vite.config.ts             # Vite config
│   └── package.json               # npm config
│
├── sql/                           # Database scripts
│   ├── schema.sql                 # Table creation
│   └── seed.sql                   # Seed data
│
├── deploy/                        # Deployment config
│   ├── nginx/
│   │   └── campus-secondhand.conf
│   └── scripts/
│       ├── backend-start.sh
│       ├── frontend-build.sh
│       └── init-rds.sh
│
├── docker-compose.yml             # Docker Compose
│
└── README.md                      # This file
```

---

## Tech Stack

### Backend

| Technology | Version | Description |
|------------|---------|-------------|
| Spring Boot | 3.2.5 | Java framework |
| MyBatis | 3.0.3 | ORM framework |
| MySQL | 8.0 | Database |
| JWT | jjwt 0.12.5 | Authentication |
| OBS SDK | 3.23.9 | Object storage |

### Frontend

| Technology | Version | Description |
|------------|---------|-------------|
| Vue 3 | 3.4.x | Frontend framework |
| Pinia | 2.x | State management |
| Vue Router | 4.x | Routing |
| Element Plus | 2.6.x | UI components |
| Vite | 5.x | Build tool |

### Cloud Services

| Service | Description |
|---------|-------------|
| ECS | Cloud server |
| RDS | MySQL database |
| OBS | Object storage (images) |

---

## Quick Start

### Prerequisites

- JDK 21+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

### Backend Setup

```bash
cd campus-secondhand-backend

# Create config file from template
cp src/main/resources/huawei-cloud-example.yml src/main/resources/huawei-cloud.yml
# Edit huawei-cloud.yml with your credentials

# Initialize database
mysql -uroot -p -e "CREATE DATABASE campus"
mysql -uroot -p campus < ../sql/schema.sql
mysql -uroot -p campus < ../sql/seed.sql

# Build and run
mvn clean package -DskipTests
java -jar target/campus-secondhand-backend.jar
```

Backend runs on http://localhost:8080

### Frontend Setup

```bash
cd campus-secondhand-frontend

# Install dependencies
npm install

# Development server
npm run dev

# Production build
npm run build
```

Frontend runs on http://localhost:3000

### Docker Compose

```bash
docker-compose up -d
```

Access at http://localhost

---

## Features

| Module | Features | Pages |
|--------|----------|-------|
| Auth | Register, Login | LoginView, RegisterView |
| Product | Publish, Browse, Search | HomeView, PublishView, ProductDetailView |
| Transaction | Create, Confirm, Complete, Cancel | TransactionsView |
| Message | Send, Receive | MessagesView |
| Review | Create, View | ReviewView, UserCenterView |
| User | Profile, My Products | UserCenterView |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/login` | POST | User login |
| `/api/auth/register` | POST | User registration |
| `/api/products` | GET | Product list |
| `/api/products` | POST | Create product |
| `/api/products/{id}` | GET | Product detail |
| `/api/products/upload-url` | GET | Get OBS upload URL |
| `/api/transactions` | POST | Create transaction |
| `/api/messages` | GET/POST | Messages |
| `/api/reviews` | POST | Create review |

---

## Configuration

### huawei-cloud.yml Template

```yaml
huawei-cloud:
  rds:
    host: your-rds-host
    port: 3306
    database: campus
    username: your-username
    password: your-password
  
  obs:
    access-key: your-access-key
    secret-key: your-secret-key
    endpoint: obs.region.myhuaweicloud.com
    bucket: your-bucket-name
```

---

## Deployment

1. Set up cloud server (ECS)
2. Create MySQL database (RDS)
3. Create object storage bucket (OBS)
4. Configure security groups (ports 80, 8080, 3306)
5. Deploy backend with Maven
6. Build frontend with Vite
7. Configure Nginx reverse proxy
8. Start services

---

## License

MIT License

---

**Last Updated**: 2026-04-23