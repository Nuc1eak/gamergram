# Gamergram

Gamergram is a pet project: a social network for gamers. Users share screenshots and clips, tag them with games, follow each other, like and comment on posts, and keep lists of favorite and wishlist games on their profiles.

## Features

- Registration and login with JWT authentication
- User profiles with avatar, bio, follower and following counts
- Media posts (image/video upload up to 100 MB) tagged with a game
- Explore feed (all posts) and personal feed (posts from people you follow)
- Likes, comments, follows
- Game catalog with search; favorite and wishlist games per user
- User search by username

## Tech stack

| Layer | Technologies |
|---|---|
| Backend | Java 17, Spring Boot 3.3 (Web, Data JPA, Security, Validation, Actuator), JJWT, Lombok |
| Database | PostgreSQL 16 (Docker Compose) |
| Frontend | React 19, TypeScript, Vite, Tailwind CSS 4, TanStack Query, React Router |
| Tests | JUnit 5, Mockito, Spring Security Test, Testcontainers |

## Project structure

```
.
├── src/main/java/com/nikita/gamergram/   # Spring Boot backend
│   ├── controller/   # REST controllers
│   ├── service/      # business logic
│   ├── repository/   # Spring Data JPA repositories
│   ├── entity/       # JPA entities
│   ├── dto/          # request/response objects
│   ├── security/     # JWT filter and service
│   ├── config/       # security, CORS, uploads
│   └── exception/    # error handling
├── src/main/resources/application.yml   # backend configuration
├── src/test/                            # unit (*Test) and integration (*IT) tests
├── frontend/                            # React + Vite web client
├── docker-compose.yml                   # local PostgreSQL
└── API.md                               # REST API reference
```

## Requirements

- Java 17+
- Maven 3.9+
- Node.js 20+ and npm
- Docker (for PostgreSQL and integration tests)

## Getting started

### 1. Start the database

```bash
docker compose up -d
```

PostgreSQL listens on `localhost:5433` (database, user, and password are all `gamergram`).

### 2. Start the backend

```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080`. Hibernate creates the schema automatically (`ddl-auto: update`). Uploaded files go to the `uploads/` directory and are served at `/uploads/**`.

### 3. Start the frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Open `http://localhost:5173`.

### 4. (Optional) Add demo data

With the backend running:

```bash
cd frontend
npm run seed
```

This creates demo users (`pixelvex`, `nightowl`, `tinkerbyte`, `ghostframe`, `mira_gg`) with the password `demo-password`, plus games, posts, likes, comments, and follows. Set `API_URL` to seed a different server.

## Configuration

The backend reads these environment variables. The defaults are for local development only.

| Variable | Default | Description |
|---|---|---|
| `DB_USERNAME` | `gamergram` | PostgreSQL user |
| `DB_PASSWORD` | `gamergram` | PostgreSQL password |
| `JWT_SECRET` | dev-only key | JWT signing key. **Always set it outside local development.** |
| `UPLOAD_DIR` | `uploads` | Directory for uploaded media |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origins |

The frontend reads `VITE_API_URL` from `frontend/.env` (default `http://localhost:8080`).

## Tests

```bash
mvn test     # unit tests (*Test)
mvn verify   # unit + integration tests (*IT); Docker must be running for Testcontainers
```

Frontend lint:

```bash
cd frontend && npm run lint
```

## Production build

```bash
mvn package                      # target/gamergram-0.0.1-SNAPSHOT.jar
java -jar target/gamergram-0.0.1-SNAPSHOT.jar

cd frontend && npm run build     # static files in frontend/dist
```

## API

All endpoints except `/api/auth/**` and `GET /uploads/**` require the `Authorization: Bearer <token>` header. Get a token from `POST /api/auth/login`.

See [API.md](API.md) for the full endpoint reference, request and response formats, and error codes.
