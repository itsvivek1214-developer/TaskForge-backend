# TaskForge Backend — Spring Boot REST API

AI-powered task management backend with JWT authentication, role-based access control, Kanban workflow, and a rule-based AI prioritization engine.

---

## ⚡ Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. Create the database
```sql
CREATE DATABASE taskforge_db;
```

### 2. Configure credentials
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=YOUR_MYSQL_USER
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 3. Run the application
```bash
mvn spring-boot:run
```

The server starts on **http://localhost:8080**

Tables are auto-created on first run (`ddl-auto=update`).

### 4. Demo accounts (auto-seeded)
| Email | Password | Role |
|---|---|---|
| user@demo.com | password123 | USER |
| admin@demo.com | admin123 | ADMIN |

---

## 🗂️ Project Structure

```
src/main/java/com/taskforge/
├── TaskForgeApplication.java       # Entry point
├── config/
│   ├── SecurityConfig.java         # Spring Security + CORS
│   ├── JpaConfig.java              # JPA auditing
│   └── DataSeeder.java             # Demo data on startup
├── controller/
│   ├── AuthController.java         # POST /api/auth/**
│   └── TaskController.java         # /api/tasks/**
├── service/
│   ├── AuthService.java            # Register, login, JWT
│   ├── TaskService.java            # CRUD + workflow logic
│   └── AIService.java              # Priority + suggestion engine
├── entity/
│   ├── User.java                   # users table
│   └── Task.java                   # tasks table
├── repository/
│   ├── UserRepository.java
│   └── TaskRepository.java         # Paginated filter queries
├── dto/
│   ├── request/
│   │   ├── AuthRequest.java        # Register, Login
│   │   └── TaskRequest.java        # Create, Update, UpdateStatus, AI
│   └── response/
│       ├── AuthResponse.java       # Login (token + user), Message
│       ├── TaskResponse.java       # Task DTO
│       └── AIResponse.java         # Prioritize, Suggest
├── security/
│   ├── JwtUtil.java                # Token generation + validation
│   ├── JwtAuthFilter.java          # Per-request JWT filter
│   └── UserDetailsServiceImpl.java # Loads user by email
└── exception/
    └── GlobalExceptionHandler.java # @ControllerAdvice
```

---

## 🔌 API Reference

### Auth

| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/api/auth/register` | `{name, email, password}` | `{message}` |
| POST | `/api/auth/login` | `{email, password}` | `{token, user}` |

**Login response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "name": "Demo User",
    "email": "user@demo.com",
    "role": "USER"
  }
}
```

All subsequent requests require the header:
```
Authorization: Bearer <token>
```

---

### Tasks

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get all tasks (paginated) |
| POST | `/api/tasks` | Create a task |
| GET | `/api/tasks/{id}` | Get task by ID |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |
| PATCH | `/api/tasks/{id}/status` | Update status only |

**GET /api/tasks query params:**
```
?status=TODO|IN_PROGRESS|DONE
&priority=LOW|MEDIUM|HIGH
&search=keyword
&page=0
&size=20
&sortBy=createdAt
&sortDir=desc
```

**Create task body:**
```json
{
  "title": "Fix login bug",
  "description": "Users redirected to 404 after login",
  "priority": "HIGH",
  "status": "TODO",
  "deadline": "2025-03-20T18:00:00"
}
```

**Task response shape:**
```json
{
  "id": 1,
  "title": "Fix login bug",
  "description": "Users redirected to 404 after login",
  "priority": "HIGH",
  "status": "TODO",
  "deadline": "2025-03-20T18:00:00",
  "createdAt": "2025-03-15T10:00:00",
  "updatedAt": "2025-03-15T10:00:00",
  "userId": 1
}
```

---

### AI Endpoints

**POST /api/tasks/prioritize**
```json
// Request
{ "title": "Deploy to prod", "description": "...", "deadline": "2025-03-16T09:00:00" }

// Response
{
  "priority": "HIGH",
  "reasoning": "Deadline is in 18 hours. Critical urgency threshold reached.",
  "confidence": 95
}
```

**POST /api/tasks/suggest**
```json
// Request
{ "title": "api integration", "description": "Connect payment gateway to checkout flow" }

// Response
{
  "suggestedTitle": "Implement: api integration",
  "summary": "This task involves connecting payment gateway to checkout flow...",
  "recommendedDeadline": "2025-03-18T10:00:00",
  "tips": [
    "Identify all API contracts and dependencies before starting.",
    "Break this task into smaller, trackable sub-tasks.",
    "Schedule a mid-point check-in at 50% completion."
  ]
}
```

---

## 🔄 Workflow Rules

Valid status transitions:
```
TODO → IN_PROGRESS
IN_PROGRESS → DONE
IN_PROGRESS → TODO   (revert)
DONE → (blocked)     // Cannot move out of DONE directly
```

---

## 🤖 AI Priority Rules

| Condition | Priority |
|-----------|----------|
| Task is overdue | HIGH |
| Deadline < 24 hours | HIGH |
| Deadline < 72 hours (3 days) | MEDIUM |
| Long/complex description keywords | MEDIUM |
| Deadline > 3 days, simple task | LOW |
| No deadline, simple task | LOW |

---

## 🔐 Security

- BCrypt password hashing (strength 10)
- Stateless JWT sessions (24h expiry by default)
- Role-based: USER sees own tasks only; ADMIN sees all
- CORS configured for `http://localhost:3000`
- All endpoints except `/api/auth/**` require valid JWT

---

## ⚙️ Configuration Reference

```properties
# Change DB credentials
spring.datasource.username=root
spring.datasource.password=root

# Change JWT secret (min 32 characters)
app.jwt.secret=your-super-secret-key-here-min-32-chars
app.jwt.expiration-ms=86400000   # 24 hours

# Allow additional frontend origins
app.cors.allowed-origins=http://localhost:3000,https://yourapp.com
```
