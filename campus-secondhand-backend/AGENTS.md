# CAMPUS SECONDHAND BACKEND

**Generated:** 2026-04-23

## OVERVIEW

Java 21 Spring Boot 3.2.5 REST API for campus secondhand trading. JWT auth, MyBatis ORM, Huawei OBS integration.

## STRUCTURE

```
src/main/java/com/campus/secondhand/
├── controller/        # 7 REST controllers (Auth, Product, User, Message, Transaction, Review)
├── service/           # 8 business services (Auth, Product, Obs, User, Message, Transaction, Review)
├── mapper/            # 6 MyBatis mappers (User, Product, Category, Message, Transaction, Review)
├── entity/            # 6 entities (User, Product, Category, Message, Transaction, Review)
├── dto/
│   ├── request/       # 8 request DTOs (Login, Register, ProductCreate, etc.)
│   └── response/      # 6 response DTOs (Login, ProductDetail, PageResult, etc.)
├── config/            # 6 configs (JwtInterceptor, HuaweiCloud, RdsDataSource, Obs, Web, YamlFactory)
├── exception/         # BusinessException + GlobalExceptionHandler
└── utils/             # JwtUtil
```

## WHERE TO LOOK

| Task | Location |
|------|----------|
| Add REST endpoint | `controller/` → corresponding `service/` → `mapper/` |
| Auth flow | `AuthController` → `AuthService` → `JwtUtil` |
| Product CRUD | `ProductController` → `ProductService` → `ProductMapper` |
| OBS upload | `ObsService` (Huawei SDK integration) |
| DB config | `RdsDataSourceConfig` + `huawei-cloud.yml` |
| Error handling | `GlobalExceptionHandler` → `ApiResponse<T>` |

## CONVENTIONS

- **Layer order**: Controller → Service → Mapper → Entity (strict separation)
- **Response format**: All APIs return `ApiResponse<T>` with code/message/data
- **Auth**: `@UserContext` annotation injects current user from JWT
- **DTO naming**: `*Request` for input, `*Response` for output
- **MyBatis XML**: Mapper interfaces in Java, SQL in `resources/mapper/*.xml`

## ANTI-PATTERNS

- DO NOT put SQL in Java files (use mapper XML)
- DO NOT bypass service layer from controller
- DO NOT catch exceptions in controller (GlobalExceptionHandler handles all)
- DO NOT use `@Autowired` on fields (use constructor injection)

## COMMANDS

```bash
mvn clean package -DskipTests
java -jar target/campus-secondhand-backend.jar
```

## NOTES

- **Entry**: `SecondhandApplication.java`
- **Config split**: `application.yml` (app config), `huawei-cloud.yml` (cloud credentials)
- **JWT**: HS256, 7-day expiry, in `Authorization: Bearer <token>` header
- **OBS**: Pre-signed URLs for client-side upload