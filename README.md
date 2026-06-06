# CampusXchange Backend

Spring Boot REST API for a student peer-to-peer marketplace.

## Stack

- Java 17, Spring Boot 3.1.5
- PostgreSQL 12+
- JWT auth + WebSocket (STOMP over SockJS)
- Cloudinary (image uploads handled client-side)
- Swagger UI at `http://localhost:8080/swagger-ui.html`

## Local Development

```powershell
# 1. Create the database
createdb campusxchange

# 2. Set required environment variables (PowerShell)
$env:DB_URL="jdbc:postgresql://localhost:5432/campusxchange"
$env:DB_USERNAME="campusxchange_user"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your-secret-key-at-least-32-chars"
$env:GOOGLE_CLIENT_ID="your-google-client-id" # optional

# 3. Run
mvn spring-boot:run
# API available at http://localhost:8080
```

For a database-free local run, use the H2-backed test profile:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=test"
```

Copy `.env.example` to `.env` and fill in the values. The application imports
that local file automatically, while real environment variables still take
precedence:

```powershell
mvn spring-boot:run
```

## Production With Neon

In the Neon dashboard, open **Connect**, enable connection pooling, and use the
generated connection details. `DB_URL` must be a JDBC URL:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://your-endpoint-pooler.region.aws.neon.tech/neondb?sslmode=require"
$env:DB_USERNAME="neondb_owner"
$env:DB_PASSWORD="your-neon-password"
$env:JWT_SECRET="a-new-random-secret-at-least-32-characters-long"
$env:CORS_ALLOWED_ORIGINS="https://your-frontend.example.com"
mvn spring-boot:run
```

Do not use a `postgresql://user:password@host/database` URI directly as
`DB_URL`. Spring expects `jdbc:postgresql://host/database?...`; username and
password are provided separately.

## Deploy To Render

The repository includes a Java 17 `Dockerfile` and `render.yaml`.

1. Push the project to GitHub.
2. In Render, select **New > Blueprint** and connect the repository.
3. Enter `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `CORS_ALLOWED_ORIGINS`
   when prompted. Google OAuth values are optional.
4. Deploy. Render generates `JWT_SECRET`, activates the `prod` profile, and
   checks `/actuator/health`.

Use Neon's pooled JDBC URL with `sslmode=require`. Render supplies `PORT`
automatically, and demo-data seeding is disabled in production.

## Configuration

Key settings in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/campusxchange}
    username: ${DB_USERNAME:campusxchange_user}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: update
```

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/auth/register` | — | Register |
| POST | `/api/auth/login` | — | Login |
| POST | `/api/auth/google` | — | Google OAuth login/register |
| POST | `/api/auth/forgot-password` | — | Request password reset |
| POST | `/api/auth/refresh` | — | Refresh token |
| GET | `/api/products` | — | List products |
| GET | `/api/products/{id}` | — | Get product |
| GET | `/api/products/search?query=` | — | Search |
| POST | `/api/products` | ✓ | Create product |
| PUT | `/api/products/{id}` | ✓ | Update product |
| DELETE | `/api/products/{id}` | ✓ | Delete product |
| GET | `/api/users/{id}` | — | Get profile |
| PUT | `/api/users/{id}` | ✓ | Update profile |
| GET | `/api/messages/conversations` | ✓ | List chats |
| GET | `/api/messages/conversation/{partnerId}` | ✓ | Chat history |
| POST | `/api/messages/send` | ✓ | Send message (REST fallback) |

### WebSocket (real-time chat)

Connect to `/ws` via SockJS + STOMP.  
Publish to `/app/chat/send` with header `X-User-Id: <userId>`.  
Subscribe to `/topic/chat/{min(uid1,uid2)}_{max(uid1,uid2)}`.

## Auth Header

All protected endpoints require:
```
Authorization: Bearer <access_token>
X-User-Id: <userId>
```

## Common Issues

**Port 8080 in use**
```bash
lsof -ti:8080 | xargs kill -9
```

**Neon connection failed** — verify the pooled hostname, `sslmode=require`,
credentials, and whether the current network allows outbound TCP port 5432.

**CORS error** — set `CORS_ALLOWED_ORIGINS` to the exact frontend origin,
without a trailing slash.
