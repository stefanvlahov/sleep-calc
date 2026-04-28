# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew build          # Build
./gradlew bootRun        # Run locally
./gradlew test           # All tests
./gradlew test --tests "ClassName"              # Single test class
./gradlew test --tests "ClassName.methodName"   # Single test method
./gradlew clean build    # Clean build
```

## Architecture

Spring Boot 3.5 / Java 21 REST API with layered architecture:

**Controller → Service (interface + impl) → Repository (Spring Data JPA) → PostgreSQL**

- `controller/` — REST endpoints: `/api/auth`, `/api/sleep`, `/api/reports`
- `service/` — Business logic via interfaces (`SleepService`, `ReportService`) with `*Impl` classes
- `repository/` — Spring Data JPA repositories with custom JPQL queries
- `entity/` — JPA entities: `User`, `SleepData`
- `dto/` — Java Records for API responses (`WeeklyReportDTO`, `MonthlyReportDTO`, `WeeklyReportItem`, `DailyReportItem`); `SleepState` and `SleepHistoryEntry` are inner records of the `SleepService` interface
- `config/` — Security, JWT, CORS configuration
- `exception/` — Centralized exception handling

## Authentication

JWT-based stateless auth. All `/api/auth/**` routes are public; all others require a Bearer token. `JwtAuthFilter` runs before `UsernamePasswordAuthenticationFilter`. JWT secret is injected via `jwt.secret.key` (Base64-encoded, 256-bit minimum), stored in `application-local-secrets.properties` which is not committed.

## Profiles & Configuration

| Profile | Database | Usage |
|---------|----------|-------|
| `local` (default) | PostgreSQL | Local development |
| `test` | H2 in-memory | Automated tests |
| `prod` | PostgreSQL | Production |

Schema is auto-managed via Hibernate `ddl-auto=update` — no manual migrations.

## Core Business Logic

**Target sleep**: 7.5 hours/night. Sleep entries are always date-ordered; updating a past entry triggers recalculation of all subsequent entries.

- **Shortfall**: Uses available surplus first; remainder becomes debt
- **Surplus**: If debt exists, extra sleep pays it down via a recovery factor (100% at ≤1 hour debt → 30% at 20+ hours debt) to prevent unrealistic catch-up; remainder becomes surplus
- Input accepts time format (`8:30`) or decimal (`8.5`)

## Testing Patterns

- Controller tests: `MockMvc` in standalone mode with `StandaloneSetup`
- Service tests: Mockito mocks for repositories
- Integration tests: `@SpringBootTest` + `@ActiveProfiles("test")` (H2)
- Security tests: `@WithMockUser` for injecting security context
- `TestJwtDynamicProps` base class generates a fresh random 256-bit JWT secret per test run via `@DynamicPropertySource`

## CORS

Allowed origins: `http://localhost:5173` and `https://timely-pika-a4086d.netlify.app` (configured in `SecurityConfig`).
