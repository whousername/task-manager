# Task Manager (Spring Boot)

Education REST-app for managing tasks
---

##  Technology stack:

- Java 24
- Spring Boot (v3.5.6)
- Spring Web / REST
- Spring Data JPA / Hibernate
- PostgreSQL
- Docker
- SLF4J / Logback
- Bean Validation (Jakarta Validation)
- Testing: JUnit 5, Mockito, Spring Boot Test

---

## Functionality

- create, edit, delete, get by id / get all (pageable) / get by filter (pageable), get task done
- filtration `creatorId`, `assignedUserId`, `status`, `priority`  
- validation (`@NotNull`, `@Positive`, `@Future`, etc)
- logging 
- handle exceptions by `@ControllerAdvice`

---

## Structure

src/main/java/…/taskmanager
--tasks - task-feature (controller, service, repository, entities)
--web   - (main-exception-handler, error entity dto)

## Query examples:

++CREATE TASK++
POST /tasks
Content-Type: application/json

{
  "creatorId": 1,
  "assignedUserId": 2,
  "priority": "HIGH",
  "deadLineDate": "2025-11-10T12:00:00"
}

++GET ALL TASKS++
GET /tasks?page=0&size=10

++GET BY FILTER++
GET /tasks/filter?creatorId=1&assignedUserId=1&status=CREATED&priority=HIGH&pageSize=10&pageNum=0

++DELETE++
DELETE /tasks/12






