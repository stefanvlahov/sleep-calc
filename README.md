# Sleep Debt Calculator

A Spring Boot web application that helps track and manage sleep debt. This application calculates sleep debt based on the difference between actual sleep hours and a target of 7.5 hours per night.

### Overview

Sleep debt (or sleep deficit) is the difference between the amount of sleep someone needs and the amount they actually get. This application helps users track their sleep patterns and understand their accumulated sleep debt over time.

### Features

- Track daily sleep hours
- Calculate sleep debt based on a target of 7.5 hours per night
- View current sleep debt status
- Realistic sleep debt model with diminished recovery for high debt levels

### Technology Stack

- Java 21
- Spring Boot 3.4.5
- Spring Web
- JUnit 5 for testing

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/sleep/debt` | Returns the current sleep debt |
| POST | `/api/sleep` | Records sleep hours and updates the debt |

### Sleep Debt Calculation Rules

- If you sleep less than the target (7.5 hours), your sleep debt increases
- If you sleep more than the target, your sleep debt decreases
- When debt is high, recovery is diminished (it's harder to pay back sleep debt)
- Negative sleep hours are not allowed (returns 400 Bad Request)

### Example Usage

```bash
# Get current sleep debt
curl -X GET http://localhost:8080/api/sleep/debt

# Record sleep hours
curl -X POST http://localhost:8080/api/sleep \
  -H "Content-Type: application/json" \
  -d '{"hoursSlept": 6.5}'
```

### Frontend Integration

The API includes CORS configuration for a frontend application running on `http://localhost:3000`.

### Building and Running

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

### Testing

```bash
# Run tests
./gradlew test
```

### Future Enhancements

- Front-end website developed in React.js
- User authentication and profiles
- Historical sleep data visualization
- iPhone app/Android app