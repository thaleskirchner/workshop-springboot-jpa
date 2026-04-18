# 🛒 Course Project — Spring Boot REST API

A RESTful API built with **Spring Boot** for managing an e-commerce system, including users, products, categories, orders, and payments.

---

## 📋 About the project

This is a back-end API for an online sales system. It exposes REST endpoints for the main domain entities and follows a layered architecture (Resource → Service → Repository).

---

## 🏗️ Architecture

```
com.educandoweb.course
├── entities/           # JPA entities (User, Product, Order, etc.)
│   ├── enums/          # Enums (OrderStatus)
│   └── pk/             # Composite keys (OrderItemPK)
├── repositories/       # Spring Data JPA interfaces
├── resources/          # REST controllers
├── services/           # Business logic
│   └── exceptions/     # Custom exceptions
└── CourseApplication   # Main class
```

---

## 🗂️ Domain model

| Entity       | Description                                               |
|--------------|-----------------------------------------------------------|
| `User`       | System user / customer                                    |
| `Order`      | Order placed by a user                                    |
| `OrderItem`  | Order line item (product + quantity + price)              |
| `Product`    | Product available for sale                                |
| `Category`   | Product category (N:N relationship with Product)          |
| `Payment`    | Payment associated with an order (1:1 relationship)       |

### Relationships

- `User` → `Order`: a user can have many orders (1:N)
- `Order` → `OrderItem`: an order contains many items (1:N)
- `Product` → `OrderItem`: a product can appear in many order items (1:N)
- `Product` ↔ `Category`: many-to-many (N:N) via `tb_product_category` join table
- `Order` → `Payment`: an order has at most one payment (1:1)

---

## 🔌 API Endpoints

### Users — `/users`

| Method   | Endpoint       | Description              |
|----------|----------------|--------------------------|
| `GET`    | `/users`       | List all users           |
| `GET`    | `/users/{id}`  | Find user by ID          |
| `POST`   | `/users`       | Create a new user        |
| `PUT`    | `/users/{id}`  | Update a user            |
| `DELETE` | `/users/{id}`  | Delete a user            |

### Products — `/products`

| Method | Endpoint          | Description            |
|--------|-------------------|------------------------|
| `GET`  | `/products`       | List all products      |
| `GET`  | `/products/{id}`  | Find product by ID     |

### Categories — `/categories`

| Method | Endpoint             | Description              |
|--------|----------------------|--------------------------|
| `GET`  | `/categories`        | List all categories      |
| `GET`  | `/categories/{id}`   | Find category by ID      |

### Orders — `/orders`

| Method | Endpoint        | Description           |
|--------|-----------------|-----------------------|
| `GET`  | `/orders`       | List all orders       |
| `GET`  | `/orders/{id}`  | Find order by ID      |

---

## 🛠️ Technologies

- **Java 17+**
- **Spring Boot**
- **Spring Data JPA / Hibernate**
- **Jakarta Persistence (JPA)**
- **Jackson** (JSON serialization)
- **H2 Database** (in-memory database for development) or any configured relational database
- **Maven**

---

## 🚀 Getting started

### Prerequisites

- Java 17 or higher
- Maven 3.8+

### Steps

```bash
# Clone the repository
git clone https://github.com/your-username/course.git

# Navigate to the project folder
cd course

# Run with Maven
./mvnw spring-boot:run
```

The API will be available at: `http://localhost:8080`

---

## 🧪 Test profile (`test`)

The `TestConfig` class is activated with the `test` profile and automatically seeds the database on startup.

### Seed data

**Categories**
| ID | Name        |
|----|-------------|
| 1  | Electronics |
| 2  | Books       |
| 3  | Computers   |

**Products**
| ID | Name                  | Price     | Categories              |
|----|-----------------------|-----------|-------------------------|
| 1  | The Lord of the Rings | $90.50    | Books                   |
| 2  | Smart TV              | $2190.00  | Electronics, Computers  |
| 3  | Macbook Pro           | $1250.00  | Computers               |
| 4  | PC Gamer              | $1200.00  | Computers               |
| 5  | Rails for Dummies     | $100.99   | Books                   |

**Users**
| ID | Name        | Email           |
|----|-------------|-----------------|
| 1  | Maria Brown | maria@gmail.com |
| 2  | Alex Green  | alex@gmail.com  |

**Orders**
| ID | Client      | Status          | Placed at            |
|----|-------------|-----------------|----------------------|
| 1  | Maria Brown | PAID            | 2019-06-20T19:53:07Z |
| 2  | Alex Green  | WAITING_PAYMENT | 2019-07-21T03:42:10Z |
| 3  | Maria Brown | WAITING_PAYMENT | 2019-07-22T15:21:22Z |

**Order items**
| Order | Product               | Qty | Unit price |
|-------|-----------------------|-----|------------|
| 1     | The Lord of the Rings | 2   | $90.50     |
| 1     | Macbook Pro           | 1   | $1250.00   |
| 2     | Macbook Pro           | 2   | $1250.00   |
| 3     | Rails for Dummies     | 2   | $100.99    |

Order #1 also has a **payment** recorded at `2019-06-20T21:53:07Z`.

### Activating the test profile

In `application.properties`:

```properties
spring.profiles.active=test
```

---

## ⚙️ Database configuration

By default, the project uses **H2 (in-memory database)**. To use an external database (e.g. PostgreSQL), edit `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/coursedb
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## 📦 Payload examples

### Create user (`POST /users`)

```json
{
  "name": "Maria Silva",
  "email": "maria@email.com",
  "phone": "11999999999",
  "password": "password123"
}
```

### Response (`201 Created`)

```json
{
  "id": 1,
  "name": "Maria Silva",
  "email": "maria@email.com",
  "phone": "11999999999"
}
```

---

## 🧱 Error handling

The user service includes custom exception handling:

- `ResourceNotFoundException` — resource not found (404)
- `DataBaseException` — referential integrity violation (400/409)

---

## 📄 License

This project was developed for educational purposes.
