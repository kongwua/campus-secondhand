# CAMPUS SECONDHAND PLATFORM

**Generated:** 2026-04-23
**Commit:** 71d7da5
**Branch:** main

## OVERVIEW

Campus secondhand trading platform - Spring Boot 3 + Vue 3 full-stack app for Huawei Cloud deployment (ECS + RDS + OBS).

## STRUCTURE

```
campus/
├── campus-secondhand-backend/     # Java 21 Spring Boot API
│   ├── src/main/java/.../         # Controller → Service → Mapper → Entity
│   ├── src/main/resources/        # application.yml + huawei-cloud.yml + mapper XML
│   └── pom.xml                    # Maven build
├── campus-secondhand-frontend/    # Vue 3 + Vite + TypeScript
│   ├── src/views/                 # 9 page components
│   ├── src/api/                   # Axios API wrappers
│   ├── src/store/                 # Pinia state (auth)
│   └── src/router/                # Vue Router config
├── sql/                           # MySQL schema + seed data
├── deploy/nginx/                  # Nginx reverse proxy config
├── scripts/                       # setup-env.sh validation script
└── docker-compose.yml             # Local dev (MySQL + Redis)
```

## WHERE TO LOOK

| Task | Location |
|------|----------|
| API endpoints | `campus-secondhand-backend/src/main/java/com/campus/secondhand/controller/` |
| Business logic | `campus-secondhand-backend/src/main/java/com/campus/secondhand/service/` |
| DB operations | `campus-secondhand-backend/src/main/java/com/campus/secondhand/mapper/` + `resources/mapper/*.xml` |
| Huawei Cloud config | `campus-secondhand-backend/src/main/resources/huawei-cloud.yml` |
| Frontend pages | `campus-secondhand-frontend/src/views/*.vue` |
| API client | `campus-secondhand-frontend/src/api/*.ts` |
| Auth state | `campus-secondhand-frontend/src/store/auth.ts` |
| DB init | `sql/schema.sql`, `sql/seed.sql` |

## CONVENTIONS

- **Backend layers**: Controller (REST) → Service (business) → Mapper (MyBatis) → Entity (POJO)
- **DTO pattern**: Request/Response classes in `dto/request/`, `dto/response/`
- **Error handling**: `BusinessException` + `GlobalExceptionHandler` → `ApiResponse<T>`
- **Auth**: JWT via `JwtInterceptor`, `UserContext` for request-scoped user
- **Frontend**: Vue 3 Composition API, Element Plus components, Axios wrapper in `api/request.ts`

## ANTI-PATTERNS

- DO NOT commit real Huawei Cloud credentials
- DO NOT skip tests in production builds (`-DskipTests` used currently)
- DO NOT hardcode API URLs (use Vite env vars)
- DO NOT put business logic in controllers

## COMMANDS

```bash
# Backend
cd campus-secondhand-backend
mvn clean package -DskipTests
java -jar target/campus-secondhand-backend.jar

# Frontend
cd campus-secondhand-frontend
npm install && npm run dev

# Database init
mysql -h<host> -u<user> -p campus < sql/schema.sql

# Environment check
./scripts/setup-env.sh
```

## NOTES

- **Port**: Backend 8080, Frontend dev 3000, Nginx 80
- **Cloud services**: ECS (hosting), RDS (MySQL), OBS (image storage)
- **JWT**: 7-day expiry, stored in localStorage
- **Test status**: Dependencies present but no tests implemented