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
- Spring Boot 3.5.4
- Spring Data JPA
- Spring Web
- JUnit 5 for testing

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/sleep/state` | Returns the current sleep state (debt and surplus) |
| POST | `/api/sleep` | Records sleep hours and updates the state |

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
  -d '{"timeSlept": "8:30"}'
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
  -d '{"timeSlept": "8.5"}'
```
Response:
```json
{
  "sleepDebt": 1.8,
  "sleepSurplus": 0.0
}
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

Sleep data is persisted using JPA with the following structure:
- User ID (string identifier)
- Sleep Debt (decimal hours)
- Sleep Surplus (decimal hours)

### Future Enhancements

- Front-end website developed in React.js
- User authentication and profiles
- Historical sleep data visualization
- Sleep trend analysis and recommendations
- iPhone app/Android app
- Customizable sleep targets
- Weekly/monthly sleep summaries
