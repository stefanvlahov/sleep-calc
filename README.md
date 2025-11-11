# Sleep Debt Calculator

A Spring Boot web application that helps track and manage both sleep debt and sleep surplus. This application calculates sleep patterns based on the difference between actual sleep hours and a target of 7.5 hours per night, now featuring a comprehensive sleep surplus system and flexible input formats.

### Overview

Sleep debt (or sleep deficit) is the difference between the amount of sleep someone needs and the amount they actually get. This application has evolved to also track sleep surplus - extra sleep hours that can be "banked" when you sleep more than your target. This creates a more realistic model where good sleep nights can help offset future shortfalls.

### Features

- Track daily sleep hours with **flexible input formats**
- **NEW: Time format input** - enter sleep as "8:30" for 8 hours and 30 minutes
- **NEW: Decimal format input** - enter sleep as "8.5" for 8.5 hours
- Calculate sleep debt based on a target of 7.5 hours per night
- **Sleep surplus tracking** - accumulate extra sleep hours when sleeping above target
- **Sleep surplus utilization** - use banked sleep hours to offset future shortfalls
- View current sleep debt and surplus status
- Realistic sleep debt model with diminished recovery for high debt levels
- Intelligent surplus management that prioritizes debt repayment

### Technology Stack

- Java 21
- Spring Boot 3.5.5
- Spring Data JPA
- Spring Web
- **Spring Security 6.5.3** (NEW)
- **JWT Authentication with JJWT 0.12.7** (NEW)
- **BCrypt Password Encryption** (NEW)
- PostgreSQL (Production database)
- H2 Database (Testing)
- JUnit 5 for testing

### Authentication System

The application now features a comprehensive JWT-based authentication system that provides secure user registration, login, and personalized sleep tracking.

#### Features

- **User Registration**: Create new user accounts with username and password
- **JWT Authentication**: Secure token-based authentication with 24-hour token expiration
- **Password Security**: BCrypt encryption for secure password storage
- **User-Specific Data**: Each user maintains their own separate sleep debt and surplus tracking
- **Protected Endpoints**: All sleep tracking endpoints require authentication
- **Stateless Sessions**: JWT tokens eliminate the need for server-side session storage

#### Security Configuration

- **Protected Routes**: All `/api/sleep/*` endpoints require valid JWT authentication
- **Public Routes**: `/api/auth/*` endpoints (registration and login) are publicly accessible
- **CORS Support**: Configured for frontend applications running on `http://localhost:5173`
- **Password Encoding**: Uses BCrypt with default strength for secure password hashing

### API Endpoints

#### Authentication Endpoints

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| POST | `/api/auth/register` | Register a new user account | No |
| POST | `/api/auth/login` | Login and receive JWT token | No |

#### Sleep Tracking Endpoints

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| GET | `/api/sleep/state` | Returns the current user's sleep state | Yes (JWT Token) |
| GET | `/api/sleep/history` | Returns the last 5 recorded sleep entries for the current user | Yes (JWT Token) |
| POST | `/api/sleep` | Records sleep hours for the authenticated user | Yes (JWT Token) |

**Note**: All sleep tracking endpoints now require a valid JWT token in the Authorization header.

### Input Formats

The application now accepts sleep time in two flexible formats:

#### Time Format (HH:mm)
- Examples: "8:30", "7:45", "9:15"
- Automatically converts hours and minutes to decimal hours
- Supports both single and double digit hours (e.g., "8:30" or "08:30")

#### Decimal Format
- Examples: "8.5", "7.75", "9.25"
- Direct decimal hour input
- Must be a positive number

### Sleep Calculation Rules

#### Sleep Debt (when sleeping less than 7.5 hours):
- Sleep shortfall first uses available sleep surplus
- Any remaining shortfall becomes sleep debt
- When debt is high, recovery is diminished (it's harder to pay back sleep debt)

#### Sleep Surplus (when sleeping more than 7.5 hours):
- If you have existing sleep debt, extra sleep pays down debt first (with recovery factor applied)
- Any remaining extra sleep after debt repayment becomes surplus
- If you have no debt, all extra sleep becomes surplus
- Sleep surplus can be used to offset future sleep shortfalls

#### Recovery Factor:
- Recovery effectiveness decreases as sleep debt increases
- Maximum effectiveness: 100% (no existing debt)
- Minimum effectiveness: 30% (high debt levels)
- Prevents unrealistic "catch-up" scenarios

### Example API Responses

#### Get Current Sleep State:
```bash
curl -X GET http://localhost:8080/api/sleep/state
```
Response:
```json
{
  "sleepDebt": 2.5,
  "sleepSurplus": 0.0
}
```

#### Record Sleep Hours (Time Format):
```bash
curl -X POST http://localhost:8080/api/sleep \
  -H "Content-Type: application/json" \
  -d '{"timeSlept": "8:30", "date": "2025-11-10"}'
```
Response:
```json
{
  "sleepDebt": 1.8,
  "sleepSurplus": 0.0
}
```

#### Record Sleep Hours (Decimal Format):
```bash
curl -X POST http://localhost:8080/api/sleep \
  -H "Content-Type: application/json" \
  -d '{"timeSlept": "8.5", "date": "2025-11-10"}'
```
Response:
```json
{
  "sleepDebt": 1.8,
  "sleepSurplus": 0.0
}
```

Note:
- The `date` field is required and must be in `yyyy-MM-dd` format.
- The `timeSlept` field accepts either `HH:mm` (e.g., `8:30`) or decimal hours (e.g., `8.5`).

#### Get Sleep History (Last 5 entries):
```bash
curl -X GET http://localhost:8080/api/sleep/history \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```
Response:
```json
[
  {
    "sleepDate": "2025-11-10",
    "hoursSlept": 8.5,
    "sleepDebt": 1.8,
    "sleepSurplus": 0.0
  },
  {
    "sleepDate": "2025-11-09",
    "hoursSlept": 6.0,
    "sleepDebt": 3.3,
    "sleepSurplus": 0.0
  }
]
```

Sleep history details:
- Returns up to the 5 most recent entries for the authenticated user, ordered by `sleepDate` descending.
- Each entry includes: `sleepDate` (yyyy-MM-dd), `hoursSlept` (decimal hours), `sleepDebt`, and `sleepSurplus` after that day's sleep is applied.
- Numeric values are rounded to two decimals.

### Authentication Examples

#### User Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "john_doe", "password": "securepassword123"}'
```

Response:
```
User registered successfully
```

#### User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "john_doe", "password": "securepassword123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Authenticated Sleep Tracking Requests

Once you have a JWT token, include it in the Authorization header for all sleep tracking requests:

```bash
# Get current sleep state (authenticated)
curl -X GET http://localhost:8080/api/sleep/state \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Record sleep hours (authenticated)
curl -X POST http://localhost:8080/api/sleep \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{"timeSlept": "8:30", "date": "2025-11-10"}'
```

### Usage Scenarios

#### Scenario 1: Building Surplus
- Current state: 0 debt, 0 surplus
- Sleep "9:00" or "9.0" hours (1.5 hours extra)
- Result: 0 debt, 1.5 surplus

#### Scenario 2: Using Surplus
- Current state: 0 debt, 2.0 surplus
- Sleep "6:00" or "6.0" hours (1.5 hours short)
- Result: 0 debt, 0.5 surplus

#### Scenario 3: Paying Down Debt
- Current state: 3.0 debt, 0 surplus
- Sleep "9:00" or "9.0" hours (1.5 hours extra)
- Result: ~2.1 debt, 0 surplus (recovery factor applied)

### Frontend Integration

The API includes CORS configuration for a frontend application running on `http://localhost:3000`.

### Building and Running

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The application will be available at `http://localhost:8080`

### Testing

```bash
# Run tests
./gradlew test
```

### Data Persistence

The application now supports multi-user data persistence with the following structure:

#### User Management
- **User ID**: Auto-generated unique identifier
- **Username**: Unique username for login (required)
- **Password**: BCrypt-encrypted password storage

#### Sleep Data (User-Specific)
- **User Association**: Each sleep record is tied to a specific authenticated user
- **Sleep Debt**: Decimal hours of sleep debt (user-specific)
- **Sleep Surplus**: Decimal hours of sleep surplus (user-specific)

**Important**: All sleep data is now isolated per user. Each authenticated user maintains their own independent sleep debt and surplus tracking.

### Configuration

#### Required Environment Variables

The application requires the following configuration for JWT authentication:

```properties
# JWT Secret Key (Base64 encoded, minimum 256 bits)
jwt.secret.key=your-base64-encoded-secret-key-here
```

**Security Note**: Ensure your JWT secret key is:
- At least 256 bits (32 bytes) when Base64 decoded
- Randomly generated and kept secure
- Different for each environment (development, staging, production)

#### Database Configuration

```properties
# PostgreSQL Configuration (Production)
spring.datasource.url=jdbc:postgresql://localhost:5432/sleepcalc
spring.datasource.username=your-db-username
spring.datasource.password=your-db-password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```
```

### Future Enhancements

- ✅ Front-end website developed in React.js
- ✅ **User authentication and profiles** (COMPLETED)
- Historical sleep data visualization and export
- Sleep trend analysis and personalized recommendations
- iPhone app/Android app with mobile authentication
- Customizable sleep targets per user
- Weekly/monthly sleep summaries and reports
- Social features (optional sleep goal sharing)
- Email notifications for sleep pattern insights
- Integration with wearable devices and health apps
