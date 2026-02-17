# Delivery Grid Core

## 🎯 Project Overview

**Delivery Grid Core** is a delivery order management system that operates on a virtual coordinate grid. The system automatically assigns the nearest available courier to incoming orders using the Nearest Neighbor algorithm with Euclidean distance calculation.

### Key Features
- 📍 **Coordinate-based delivery system** (no real map integration)
- 🚴 **Automatic courier assignment** using nearest neighbor algorithm
- 📦 **Order lifecycle management** (NEW → ASSIGNED → DELIVERED)
- 🔄 **Real-time courier status tracking** (FREE/BUSY)
- 🛡️ **Robust error handling** with structured API responses
- 📚 **Interactive API documentation** via Swagger UI

## 🛠 Tech Stack

- **Java 17** (Records, Text Blocks, Switch Expressions)
- **Spring Boot 3.2.0** (Web, Data JPA, Validation)
- **H2 Database** (In-Memory)
- **Lombok** (Boilerplate reduction)
- **SpringDoc OpenAPI** (Swagger UI)
- **Instancio** (Test data generation)

## 🚀 How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

```bash
# Using Maven Wrapper (recommended)
./mvnw spring-boot:run

# Or using installed Maven
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Running Tests

```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw clean test
```

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

**Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Available Endpoints

#### Create Order
```http
POST /api/v1/orders
Content-Type: application/json

{
  "source": {
    "x": 10,
    "y": 10
  },
  "destination": {
    "x": 50,
    "y": 50
  }
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "source": {
    "x": 10,
    "y": 10
  },
  "destination": {
    "x": 50,
    "y": 50
  },
  "status": "ASSIGNED",
  "courierId": 3
}
```

**Error Response** (409 Conflict - No Couriers Available):
```json
{
  "type": "https://api.delivery.hackathon.com/errors/no-couriers-available",
  "title": "No Couriers Available",
  "status": 409,
  "detail": "No free couriers available for assignment"
}
```

## 🧪 Testing

### Test Data Seeding

The application automatically seeds the database with **10 free couriers** at random locations (0-99 on both X and Y axes) when it starts. You'll see this log message:

```
✅ Test Data Loaded: 10 couriers created
```

### Test Coverage

The project includes comprehensive unit tests for:
- **DispatchService**: Courier assignment logic and error scenarios
- **OrderService**: Order creation with courier assignment

All tests use:
- **Mockito** for mocking dependencies
- **Instancio** for generating test data
- **AssertJ** for fluent assertions

## 🏗 Architecture

The project follows a **layered architecture** with **package-by-feature** organization:

```
com.hackathon.delivery
├── order/
│   ├── controller/    # REST endpoints
│   ├── service/       # Business logic
│   ├── model/         # Entities (Order, OrderStatus)
│   ├── repository/    # Data access
│   └── dto/           # Request/Response objects
├── courier/
│   ├── model/         # Entities (Courier, CourierStatus)
│   └── repository/    # Data access
└── shared/
    ├── exception/     # Custom exceptions & handlers
    ├── config/        # Application configuration
    └── GeoPoint       # Value Object
```

### Design Principles
- **Transaction Script Pattern**: Anemic domain model + smart services
- **DTO Pattern**: Entities never leave the service layer
- **Soft References**: ID-based relationships (no direct object references)
- **Constructor Injection**: All dependencies injected via constructor

## 🧠 Business Logic

### Courier Assignment Algorithm

The system uses the **Nearest Neighbor** algorithm:

1. Fetch all couriers with status `FREE`
2. If no couriers available → throw `NoCouriersAvailableException`
3. Calculate Euclidean distance from each courier to order source:
   ```
   distance = √((x₂-x₁)² + (y₂-y₁)²)
   ```
4. Select courier with minimum distance
5. Update courier status to `BUSY`
6. Assign courier to order

### Order Lifecycle

```
NEW → ASSIGNED → DELIVERED
```

- **NEW**: Order created, awaiting courier assignment
- **ASSIGNED**: Courier assigned and on the way
- **DELIVERED**: Order completed (future implementation)

## 🗄️ Database

### H2 Console

Access the H2 database console at: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

**Connection Details:**
- JDBC URL: `jdbc:h2:mem:deliverydb`
- Username: `sa`
- Password: _(empty)_

### Schema

The database schema is auto-generated from JPA entities with `ddl-auto=create-drop`.

## 📋 Project Status

### ✅ Phase 0: Setup & Core CRUD (DONE)
- Project initialization
- Domain model (GeoPoint, Order, OrderStatus)
- Repository layer

### 🟡 Phase 1: MVP & Logic Implementation (IN PROGRESS)
- Courier domain extension
- Dispatch service with nearest neighbor algorithm
- REST API with error handling
- Comprehensive testing

## 📝 License

This project is part of a hackathon exercise.
