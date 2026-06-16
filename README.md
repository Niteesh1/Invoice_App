# Book Fee Collection App

Spring Boot and Thymeleaf app for student book issue records, partial payments, pending dues, receipts, and reports.

## Current App State

- Backend/UI: Spring Boot 3, Thymeleaf, Spring Security, Spring Data JPA
- Database: MySQL 8.4 in Docker
- Schema migration: Flyway
- App port: `8082`
- MySQL host port: `3308`
- MySQL container port: `3306`
- Docker volume: `book_fee_mysql_data`
- Main class: `com.school.fees.BookFeeCollectionApplication`

## Quick Start

```powershell
docker compose up -d mysql
$env:JAVA_HOME='C:\Program Files\Java\jdk-18.0.2'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

Open `http://localhost:8082`.

Default logins:

```text
admin / admin123
cashier / cashier123
```

The MySQL database is exposed on host port `3308` and persisted in Docker volume `book_fee_mysql_data`.

## Main Features

- Add students with fixed class dropdown from `Play` to `Class X`
- Create book issue records
- On book issue, select class first, then search/select only students from that class
- Track due amount, discount, collected amount, balance, and status
- Record partial or full payments
- Prevent payment amount from exceeding pending balance
- Keep payment history
- Generate printable receipts
- Dashboard for collection and pending status
- Reports for collection, sales, and pending dues
- Added CI Pipeline to create JAR using Gihub actions
- Added Semantic Version tag Flow
## Important Docs

- Runtime and reset steps: `docs/RUNBOOK.md`
- Original build brief: `BOOK_FEE_COLLECTION_REPO_STARTER.md`
- Separate .NET rebuild and DevOps project handoff: `docs/DOTNET_BOOK_FEE_DEVOPS_PROJECT_HANDOFF.md`
- Beginner Java/JAR/Maven/Docker/.NET flowchart: `docs/BEGINNER_BUILD_DEPLOYMENT_FLOWCHART.md`
- Complete application flow image: `docs/BOOK_FEE_COMPLETE_APPLICATION_FLOW.png`
