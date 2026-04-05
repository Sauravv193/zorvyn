# FinanceOS — Finance Data Processing & Access Control Platform

A full-stack application demonstrating production-grade API design, Role-Based Access Control (RBAC), and financial data aggregation — built with **Java 21 + Spring Boot 3**, **PostgreSQL**, and **React + Vite**.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│  React Frontend (Vite + Tailwind + Recharts)                │
│  ├── LoginPage          (JWT auth, demo quick-login)        │
│  ├── DashboardPage      (charts, metrics, category table)   │
│  ├── TransactionsPage   (paginated CRUD table + filters)    │
│  └── UsersPage          (admin: role/status management)     │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP + Bearer JWT
┌──────────────────────────▼──────────────────────────────────┐
│  Spring Boot 3 Backend                                      │
│                                                             │
│  Controllers  →  Services  →  Repositories  →  PostgreSQL  │
│       ↓               ↓                                     │
│     DTOs          Mappers (MapStruct)                       │
│       ↓                                                     │
│  @ControllerAdvice (GlobalExceptionHandler)                 │
│                                                             │
│  Security: JwtAuthenticationFilter → SecurityFilterChain    │
│            @PreAuthorize on every endpoint                  │
└─────────────────────────────────────────────────────────────┘
```

---

## RBAC Matrix

| Endpoint                  | ADMIN | ANALYST | VIEWER |
|---------------------------|:-----:|:-------:|:------:|
| `POST /auth/login`        | ✅    | ✅      | ✅     |
| `GET  /records`           | ✅    | ✅      | ✅     |
| `GET  /records/{id}`      | ✅    | ✅      | ✅     |
| `POST /records`           | ✅    | ❌      | ❌     |
| `PUT  /records/{id}`      | ✅    | ❌      | ❌     |
| `DELETE /records/{id}`    | ✅    | ❌      | ❌     |
| `GET  /dashboard/summary` | ✅    | ✅      | ❌     |
| `GET  /users`             | ✅    | ❌      | ❌     |
| `PATCH /users/{id}/role`  | ✅    | ❌      | ❌     |

---

## Tech Stack

### Backend
| Concern | Technology |
|---------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security 6 + JJWT 0.12 |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL 15+ |
| DTO Mapping | MapStruct 1.5 |
| Validation | Jakarta Bean Validation (`@Valid`) |
| Boilerplate | Lombok |

### Frontend
| Concern | Technology |
|---------|-----------|
| Framework | React 18 + Vite 5 |
| Styling | Tailwind CSS 3 |
| Charts | Recharts 2 |
| Icons | Lucide React |
| HTTP | Axios |
| Routing | React Router v6 |
| Dates | date-fns |

---

## Prerequisites

- **Java 21** (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- **PostgreSQL 15+** running locally
- **Node.js 20+** and **npm** (`node -v`)

---

## Setup Instructions

### 1. Database

```bash
# Create the database
psql -U postgres -c "CREATE DATABASE financedb;"

# Run schema (creates tables, ENUMs, seed data)
psql -U postgres -d financedb -f backend/src/main/resources/schema.sql
```

### 2. Backend

```bash
cd backend

# Configure database credentials (or use env vars)
# Edit src/main/resources/application.yml OR export:
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your_64_char_hex_secret_here

mvn clean install -DskipTests
mvn spring-boot:run
```

The API starts at **http://localhost:8080/api**

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

The UI starts at **http://localhost:5173**

---

## API Documentation

### Authentication

#### `POST /api/auth/login`
```json
// Request
{ "username": "admin", "password": "password123" }

// Response 200
{
  "success": true,
  "data": {
    "token": "eyJhbGci...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": { "id": 1, "username": "admin", "role": "ADMIN", "status": "ACTIVE" }
  }
}
```

#### `POST /api/auth/register`
```json
// Request
{
  "username": "newuser",
  "password": "securepass",
  "email": "user@example.com",
  "role": "ANALYST"
}
```

---

### Financial Records

#### `GET /api/records` — Paginated + Filtered

Query parameters:
| Param | Type | Description |
|-------|------|-------------|
| `type` | `INCOME` \| `EXPENSE` | Filter by record type |
| `category` | string | Partial match (case-insensitive) |
| `dateFrom` | `YYYY-MM-DD` | Range start (inclusive) |
| `dateTo`   | `YYYY-MM-DD` | Range end (inclusive) |
| `page`     | int (default 0) | Page number |
| `size`     | int (default 20, max 100) | Page size |
| `sortBy`   | `recordDate` \| `amount` \| `category` | Sort field |
| `sortDir`  | `asc` \| `desc` | Sort direction |

```bash
# Example: all expenses in Food for last 3 months
GET /api/records?type=EXPENSE&category=food&dateFrom=2024-01-01&sortBy=amount&sortDir=desc
Authorization: Bearer <token>
```

#### `POST /api/records` — Create (ADMIN only)
```json
{
  "amount": 1500.00,
  "type": "EXPENSE",
  "category": "Rent",
  "recordDate": "2024-06-01",
  "description": "Monthly rent payment"
}
```

#### `PUT /api/records/{id}` — Update (ADMIN only)
Same body as POST.

#### `DELETE /api/records/{id}` — Delete (ADMIN only)

---

### Dashboard

#### `GET /api/dashboard/summary` — ADMIN + ANALYST

```json
// Response
{
  "success": true,
  "data": {
    "totalIncome": 595000.00,
    "totalExpenses": 243500.00,
    "netBalance": 351500.00,
    "totalRecords": 23,
    "categoryTotals": [
      { "category": "Salary", "type": "INCOME", "totalAmount": 510000.00, "count": 6 },
      { "category": "Rent",   "type": "EXPENSE","totalAmount": 72000.00,  "count": 4 }
    ],
    "monthlyTrends": [
      {
        "month": "2024-01",
        "monthLabel": "Jan 2024",
        "income": 85000.00,
        "expenses": 38500.00,
        "net": 46500.00
      }
    ]
  }
}
```

---

### Users (ADMIN only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/users` | List all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `PATCH` | `/api/users/{id}/role?role=ANALYST` | Update role |
| `PATCH` | `/api/users/{id}/status?status=INACTIVE` | Toggle status |
| `DELETE` | `/api/users/{id}` | Delete user |

---

## Error Response Format

All errors return a consistent JSON shape:

```json
{
  "success": false,
  "message": "FinancialRecord not found with id: 99",
  "timestamp": "2024-06-01T12:00:00"
}
```

Validation errors include field-level details:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "amount": "Amount must be greater than 0",
    "category": "Category is required"
  }
}
```

---

## Demo Accounts

All seeded with password `password123`:

| Username | Role | Dashboard | Write Records | Manage Users |
|----------|------|:---------:|:-------------:|:------------:|
| `admin`  | ADMIN   | ✅ | ✅ | ✅ |
| `analyst`| ANALYST | ✅ | ❌ | ❌ |
| `viewer` | VIEWER  | ❌ | ❌ | ❌ |

---

## Key Design Decisions

### 1. Layered Architecture
Controller → Service → Repository, with DTOs at every layer boundary. Entities are **never** exposed in API responses — MapStruct handles the conversion.

### 2. Dynamic Filtering via Spring Specifications
`FinancialRecordSpecification` composes JPA predicates at runtime, keeping the repository interface clean and avoiding a proliferation of query methods.

### 3. DashboardService Aggregation
All aggregation is done in PostgreSQL via JPQL aggregate queries (`SUM`, `GROUP BY`), not in Java — this is efficient and scales with data volume. The monthly trend builder seeds a full 6-month window from the application layer, ensuring zero-gap charts even for months with no activity.

### 4. Stateless JWT Security
The `JwtAuthenticationFilter` is a `OncePerRequestFilter` that validates tokens on every request. No sessions are created (`SessionCreationPolicy.STATELESS`). `@PreAuthorize` annotations on controllers enforce method-level RBAC in addition to URL-level rules in `SecurityFilterChain`.

### 5. Global Exception Handling
`@RestControllerAdvice` catches all exceptions — validation, security, business, and generic — and returns clean, consistent JSON with appropriate HTTP status codes.

### 6. Frontend Role-Awareness
The React app reads the `role` field from the stored user object and conditionally renders UI elements (nav items, action buttons) based on permissions. `ProtectedRoute` wraps sensitive pages and redirects unauthorized users.

---

## Project Structure

```
finance-app/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/financeapp/
│       │   ├── FinanceApplication.java
│       │   ├── config/SecurityConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── DashboardController.java
│       │   │   ├── FinancialRecordController.java
│       │   │   └── UserController.java
│       │   ├── dto/
│       │   │   ├── request/  (LoginRequest, RegisterRequest, FinancialRecordRequest)
│       │   │   └── response/ (ApiResponse, AuthResponse, DashboardResponse, ...)
│       │   ├── entity/       (User, FinancialRecord)
│       │   ├── exception/    (GlobalExceptionHandler, custom exceptions)
│       │   ├── mapper/       (FinancialRecordMapper, UserMapper)
│       │   ├── repository/   (UserRepository, FinancialRecordRepository, Specification)
│       │   ├── security/     (JwtTokenProvider, JwtAuthenticationFilter)
│       │   └── service/      (AuthService, DashboardService, FinancialRecordService, UserService)
│       └── resources/
│           ├── application.yml
│           └── schema.sql
└── frontend/
    ├── index.html
    ├── vite.config.js
    ├── tailwind.config.js
    └── src/
        ├── App.jsx
        ├── main.jsx
        ├── index.css
        ├── components/
        │   ├── AppLayout.jsx
        │   ├── ProtectedRoute.jsx
        │   ├── RecordFormModal.jsx
        │   ├── Sidebar.jsx
        │   └── ui.jsx
        ├── hooks/useAuth.jsx
        ├── pages/
        │   ├── LoginPage.jsx
        │   ├── DashboardPage.jsx
        │   ├── TransactionsPage.jsx
        │   └── UsersPage.jsx
        ├── services/api.js
        └── utils/helpers.js
```

---

## Running Tests

```bash
cd backend
mvn test
```

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | `postgres` | PostgreSQL password |
| `JWT_SECRET` | (dev default in yml) | 64-char hex secret for JWT signing |

> **Never** commit real secrets. Use `.env` files or a secrets manager in production.
