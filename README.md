# Bank Portal - Fullstack Banking Control System

Централизиран fullstack проект за вътрешен банков портал с JWT автентикация, управление на клиенти, сметки, кредити и служители.

## Съдържание
- [1. Обща информация](#1-обща-информация)
- [2. Архитектура](#2-архитектура)
- [3. Функционалности](#3-функционалности)
- [4. Технологии](#4-технологии)
- [5. Изисквания](#5-изисквания)
- [6. Бърз старт (локално)](#6-бърз-старт-локално)
- [7. Конфигурация](#7-конфигурация)
- [8. Данни за вход и роли](#8-данни-за-вход-и-роли)
- [9. API overview](#9-api-overview)
- [10. Валидации и бизнес правила](#10-валидации-и-бизнес-правила)
- [11. Тестване и качество](#11-тестване-и-качество)
- [12. Полезни команди](#12-полезни-команди)
- [13. Troubleshooting](#13-troubleshooting)

## 1. Обща информация
Проектът е разделен на два модула:
- `bank-backend` - Spring Boot REST API + security + PostgreSQL
- `frontend` - React + TypeScript + Vite клиент

Приложението е предназначено за вътрешно ползване от банкови служители и администратори.

## 2. Архитектура

```text
Bank/
├── bank-backend/   # Java 21, Spring Boot, JPA, Security, JWT
└── frontend/       # React + TypeScript + Vite
```

Поток на заявките:
1. Потребителят се логва през `/api/auth/login`.
2. Backend връща JWT токен + роля.
3. Frontend изпраща токена като `Authorization: Bearer <token>`.
4. Backend валидира токена и разрешава достъп според ролята.

## 3. Функционалности

### Автентикация и профил
- Логин на служител/админ.
- Преглед на текущ профил (`/api/auth/me`).
- Редакция на display name.
- Смяна на парола.

### Клиенти
- Създаване на:
  - ФЛ (Име, Фамилия, ЕГН)
  - ЮЛ (Фирма, Представител, ЕИК)
- Търсене и филтриране.
- Редакция и изтриване (според бизнес ограничения).
- Показване кой служител е регистрирал клиента.

### Банкови сметки
- Откриване на сметка за клиент.
- Депозит и теглене.
- Закриване на сметка.
- Показване на валута, статус и одит информация.

### Кредити
- Кредитен калкулатор с препоръчителен максимум.
- Поддръжка на потребителски и ипотечни кредити.
- Отпускане на кредит.
- Автоматичен погасителен план.
- Маркиране на вноска като платена.
- Извличане на кредити по клиент + статус.

### Служители (Admin)
- Преглед на служители.
- Създаване на нови служители.
- Обновяване на служителски профил/роля.

## 4. Технологии

### Backend (`bank-backend`)
- Java 21
- Spring Boot 3.3.2
- Spring Web
- Spring Data JPA (Hibernate)
- Spring Security + Method Security
- JWT (`jjwt`)
- PostgreSQL
- Bean Validation (Jakarta Validation)
- Lombok
- Maven
- JaCoCo (coverage report + coverage check)

### Frontend (`frontend`)
- React 19
- TypeScript 5
- Vite 8
- ESLint

## 5. Изисквания
- Java 21
- Maven 3.9+
- Node.js 20+
- npm 10+
- PostgreSQL 14+ (или съвместима версия)

## 6. Бърз старт (локално)

### 6.1. Стартиране на backend

```bash
cd bank-backend
export JAVA_HOME="/path/to/jdk-21"
mvn spring-boot:run
```

Backend слуша на:
- `http://localhost:8080`
- API base: `http://localhost:8080/api`

### 6.2. Стартиране на frontend

В нов терминал:

```bash
cd frontend
npm install
npm run dev -- --host 127.0.0.1 --port 5173 --strictPort
```

Frontend URL:
- `http://127.0.0.1:5173`

## 7. Конфигурация

### Backend конфигурация
Основните настройки са в `bank-backend/src/main/resources/application.yml`:
- DB URL: `jdbc:postgresql://localhost:5432/bankdb`
- DB user: `bank_user`
- DB password: `bank_password`
- Port: `8080`
- Hibernate DDL: `update`
- JWT secret: `app.security.jwt-secret`

### Frontend конфигурация
API base URL е хардкоднат в `frontend/src/App.tsx`:

```ts
const API = 'http://localhost:8080/api'
```

При различен backend host/port, смени тази стойност.

## 8. Данни за вход и роли
При празна таблица `employees`, `DataInitializer` създава demo акаунти:
- Admin: `admin` / `admin123`
- Employee: `employee` / `emp123`

Роли:
- `ROLE_ADMIN`
- `ROLE_EMPLOYEE`

## 9. API overview

### Auth
- `POST /api/auth/login`
- `GET /api/auth/me`
- `PATCH /api/auth/me`
- `POST /api/auth/change-password`

### Clients
- `POST /api/clients`
- `GET /api/clients`
- `PUT /api/clients/{clientId}`
- `DELETE /api/clients/{clientId}`

### Accounts
- `POST /api/accounts`
- `GET /api/accounts/by-client/{clientId}`
- `POST /api/accounts/{accountId}/deposit`
- `POST /api/accounts/{accountId}/withdraw`
- `POST /api/accounts/{accountId}/close`

### Credits
- `POST /api/credits`
- `GET /api/credits/by-client/{clientId}`
- `GET /api/credits/{creditId}/schedule`
- `GET /api/credits/{creditId}/status`
- `GET /api/credits/suggestion`
- `POST /api/credits/installments/{installmentId}/pay`

### Employees (Admin)
- `GET /api/employees`
- `POST /api/employees`
- `PUT /api/employees/{id}`

## 10. Валидации и бизнес правила

### Клиенти
- ФЛ:
  - Име: минимум 4 символа
  - Фамилия: минимум 4 символа
  - ЕГН: точно 10 цифри
- ЮЛ:
  - Фирма: задължително
  - Представител: задължително
  - ЕИК: точно 10 цифри

### Кредити
- Вноската не може да надвишава 30% от нетния месечен доход.
- При потребителски кредит е задължителна сметка за превод.
- При ипотечен кредит:
  - Изисква се стойност на имота.
  - Самоучастието е минимум 20%.
  - Сумата на кредита = стойност на имот - самоучастие.

### Сигурност
- Всички `/api/**` (без `/api/auth/**`) са защитени с JWT.
- CORS е разрешен за `localhost` и `127.0.0.1` на произволен порт.

## 11. Тестване и качество

### Backend тестове
```bash
cd bank-backend
mvn test
```

### Покритие (JaCoCo)
- Отчет: `bank-backend/target/site/jacoco/index.html`
- Enforced threshold: **минимум 80% LINE coverage** (Maven `jacoco:check`)

### Frontend build
```bash
cd frontend
npm run build
```

## 12. Полезни команди

### Стартиране на backend като JAR
```bash
cd bank-backend
mvn -q package -DskipTests
java -jar target/bank-backend-0.0.1-SNAPSHOT.jar
```

### Проверка за свободен backend порт
```bash
lsof -i :8080
```

### Спиране на процес на backend (пример)
```bash
kill <pid>
```

## 13. Troubleshooting
- **Frontend не зарежда данни:**
  - провери дали backend работи на `localhost:8080`
  - провери `API` константата във frontend
  - hard refresh на браузъра
- **401/403 грешки:**
  - логни се отново (възможно е токенът да е изтекъл/невалиден)
  - провери роля на потребителя
- **Проблем с DB връзка:**
  - провери PostgreSQL и креденшъли в `application.yml`
- **Грешна Java версия:**
  - backend изисква Java 21

---

## Лиценз и бележки
Проектът е учебен/демо характер и следва да се адаптира преди production употреба (security hardening, secrets management, observability, migration strategy, CI/CD policy).
