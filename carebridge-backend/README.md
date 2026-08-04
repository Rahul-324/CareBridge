# CareBridge SaaS Backend

CareBridge is a multi-tenant clinic and diagnostic centre management SaaS platform built using Java and Spring Boot.

The platform is designed to help clinics and diagnostic centres manage patients, staff, appointments, consultations, prescriptions, laboratory operations, billing, subscriptions, and reports from one centralized application.

---

## Project Vision

CareBridge allows multiple clinics and diagnostic centres to register and use the same SaaS platform.

Each clinic is treated as a separate tenant, and its data must remain isolated from all other tenants.

Example:

```text
CareBridge SaaS
├── Sharma Clinic
├── City Diagnostic Centre
└── LifeCare Clinic

## architecture

Client
   ↓
Spring Boot REST API
   ↓
PostgreSQL

com.carebridge
├── common
├── config
├── tenant
├── clinic
├── auth
├── staff
├── patient
├── appointment
├── consultation
├── prescription
├── laboratory
├── billing
└── subscription

##Technology Stack

Backend
Java 21
Spring Boot
Spring Web
Spring Data JPA
Spring Security
Hibernate
Jakarta Validation
Maven
Lombok
Database
PostgreSQL
Flyway database migrations

Testing
JUnit 5
Mockito
Spring Boot Test
Testcontainers planned

Environment Profiles

CareBridge supports the following Spring profiles:

Profile	Purpose
dev	Local development
test	Automated and integration testing
prod	Production deployment