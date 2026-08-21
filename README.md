# Agentic AI-Based Bangalore University Admission System

Spring Boot, MySQL, OpenAI API, REST APIs, and a Bootstrap frontend for a Bangalore University postgraduate admission workflow.

## Technology Stack

- Frontend: HTML5, CSS3, JavaScript, Bootstrap
- Backend: Java, Spring Boot, Spring MVC, Spring Data JPA, REST APIs
- AI: OpenAI API-ready admission agent service
- Database: MySQL

## Features

- Natural-language student profile analysis
- Student registration and login with email/password
- Admin login with fixed authorized credentials
- Course recommendation from degree, marks, and interest area
- Bangalore University postgraduate course recommendation workflow
- Official-source-oriented university fee and course information display
- Admission form validation
- Simulated payment verification
- Admission confirmation generation
- Faculty/admin admission search dashboard

## MySQL Setup

Create a MySQL database and user, or let the application create the database if the account has permission:

```sql
CREATE DATABASE bcu_admission;
```

Default database settings are in `src/main/resources/application.properties`:

```text
spring.datasource.url=jdbc:mysql://localhost:3306/bcu_admission?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=password
```

Change `spring.datasource.password` if your MySQL root password is different.

If `OPENAI_API_KEY` is not provided, the app still runs with deterministic local admission guidance.

## Login

Students can register with an email ID and password, then use the same details to login.

Admin login:

```text
Email: babhitha@gmail.com
Password: babhitha@123
```

## Run

Import the project into IntelliJ IDEA as a Maven project, then run:

```text
AdmissionSystemApplication
```

Or from a machine with Maven installed:

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080
```

## Troubleshooting

If Hibernate shows `Unable to determine Dialect without JDBC metadata`, check:

- MySQL Server is running.
- The database `bcu_admission` exists, or the MySQL user has permission to create it.
- `spring.datasource.username` and `spring.datasource.password` match your MySQL login.
- `spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect` is present in `application.properties`.

## Important Note

The project stores selected admission records in MySQL. Course eligibility, fees, seat matrix, and deadlines must be verified against the latest official Bangalore University notifications before real admission use.
