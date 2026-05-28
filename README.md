# 高校学科竞赛报名管理系统

A full-stack university academic competition registration management system built with Spring Boot 3 + Vue 3 + MySQL. Supports individual and team contest registration, admin review workflow, CMS management, and AI-powered assistant.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [API Overview](#api-overview)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Usage Guide](#usage-guide)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Development](#development)

---

## Features

### Student Features

| Feature | Description |
|---------|-------------|
| **Browse Contests** | View all contests with keyword search, category filter, and pagination |
| **Contest Details** | Rich HTML descriptions, dates, location, participation type, capacity |
| **Personal Registration** | Register for individual contests with optional remark |
| **Team Management** | Create teams, generate 6-character invite codes, invite teammates |
| **Team Registration** | Submit approved teams for contest registration |
| **Track Registrations** | Monitor status of all registrations (pending / approved / rejected / cancelled) |
| **Notification Inbox** | Receive real-time notifications for review results, team invites, system announcements |
| **Profile Settings** | Edit profile, change password, configure notification preferences |
| **AI Assistant** | Chat with an AI assistant that can query contests, check registration status, and perform operations via function calling |

### Admin Features

| Feature | Description |
|---------|-------------|
| **Contest CRUD** | Create, edit, publish / unpublish, delete contests |
| **Registration Review** | Approve or reject registration applications with rejection reason |
| **Team Review** | Approve or reject teams (auto-cancels related registrations on rejection) |
| **User Management** | View all users, freeze / unfreeze accounts |
| **CMS Management** | Manage homepage banners and announcements (rich text editor) |
| **Notification Management** | Send targeted notifications or system-wide broadcasts |
| **Operation Logs** | View audit trail of all admin actions |

### AI Assistant Features

The AI assistant (powered by DashScope / glm-5.1) provides 7 calling tools:

| Tool | Description |
|------|-------------|
| `getCurrentUserInfo` | Retrieve current logged-in user profile |
| `queryContests` | Browse contest list with keyword / category filters |
| `searchContestDetail` | Search a specific contest by name |
| `registerForContest` | Register for a personal contest |
| `createTeamForContest` | Create a team for a team contest |
| `queryMyRegistrations` | Query the user's registration records |
| `queryMyTeams` | Query the user's teams |

The assistant supports SSE streaming chat with conversation history persistence. It is prompted to always return tool results truthfully and never fabricate responses.

---

## Tech Stack

### Backend

| Technology | Version |
|------------|---------|
| Spring Boot | 3.3.5 |
| Java | 17 |
| MyBatis-Plus | 3.5.16 |
| MySQL | 5.7+ / 8.0 |
| Spring AI Alibaba | 1.0.0.2 |
| JWT (jjwt) | 0.11.5 |
| Hutool | 5.8.36 |
| MinIO (optional) | 8.5.10 |
| Maven | 3.6+ |

### Frontend

| Technology | Version |
|------------|---------|
| Vue 3 | ^3.4.0 |
| Vite | ^5.0.0 |
| Element Plus | ^2.5.0 |
| Pinia | ^2.1.0 |
| Vue Router 4 | ^4.3.0 |
| Axios | ^1.6.0 |
| Vitest | ^4.0.0 |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Frontend (Vue 3 SPA)                         │
│  localhost:3000  ───  Vite Dev Server  ───  Proxy /api → :8080     │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ HTTP (REST + SSE)
┌───────────────────────────▼─────────────────────────────────────────┐
│                        Backend (Spring Boot 3)                       │
│                         localhost:8080                               │
│                                                                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │
│  │ contest- │ │ contest- │ │ contest- │ │ contest- │ │ contest- │  │
│  │   admin  │ │   user   │ │competition│ │   team   │ │ register │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐               │
│  │ contest- │ │ contest- │ │ contest- │ │ contest- │               │
│  │  message │ │  common  │ │    ai    │ │  common  │               │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘               │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────────────┐
│                       MySQL Database :3306                           │
│                     contest_system (10 tables)                       │
└─────────────────────────────────────────────────────────────────────┘
```

### Module Dependency Graph

```
contest-admin ───┬── contest-common
                 ├── contest-user
                 ├── contest-competition
                 ├── contest-team ───┬── contest-common
                 │                   ├── contest-competition
                 │                   ├── contest-user
                 │                   ├── contest-register
                 │                   └── contest-message
                 ├── contest-register ─┬── contest-common
                 │                     ├── contest-competition
                 │                     ├── contest-message
                 │                     └── contest-user
                 ├── contest-message ─── contest-common
                 └── contest-ai ───────┬── contest-common
                                       ├── contest-competition
                                       ├── contest-register
                                       ├── contest-user
                                       ├── contest-team
                                       ├── spring-ai-alibaba-starter-dashscope
                                       └── spring-boot-starter-webflux
```

---

## Database Schema

The system uses 10 tables under the `contest_system` database:

| Table | Description | Key Columns |
|-------|-------------|-------------|
| `user` | Users (students + admins) | id, username (student ID), password (BCrypt), name, role (0=student, 1=admin), college, major, class_name, status |
| `contest` | Competitions | id, name, category, level, organizer, contest_time, register_start/end_time, location, contest_type (0=personal, 1=team, 2=both), team_min/max_size, max_participants, status, current_count |
| `team` | Teams | id, leader_id, team_name, team_no (unique, T+timestamp), invite_code (6-char), status (0=forming, 1=submitted, 2=approved, 3=rejected), member_count |
| `team_member` | Team membership | id, team_id, user_id, role (0=member, 1=leader), status (0=pending, 1=approved, 2=rejected) |
| `registration` | Registration records | id, contest_id, user_id, team_id, reg_type, status (0=pending, 1=approved, 2=rejected, 3=cancelled) |
| `notification` | Notifications | id, user_id (0=broadcast), type, title, content, related_id, related_type, is_read |
| `cms_content` | CMS (banners + announcements) | id, content_type (0=banner, 1=announcement), title, content, image_url, sort_order, position, publish_time |
| `operation_log` | Admin audit log | id, user_id, action, detail, ip_address |
| `ai_conversation` | AI chat sessions | id, user_id, title |
| `ai_message` | AI chat messages | id, conversation_id, role (user/assistant), content, tokens |

---

## API Overview

All endpoints are prefixed with `/api`. Authentication uses JWT Bearer tokens stored in `localStorage`.

### Authentication & Users

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/user/login` | - | Login, returns user info + JWT token |
| POST | `/api/user/register` | - | Register student account |
| GET | `/api/user/{id}` | - | Get user by ID |
| GET | `/api/user/page` | Admin | Paginated user list |
| PUT | `/api/user/{id}/profile` | User | Update profile |
| PUT | `/api/user/{id}/password` | User | Change password |
| PUT | `/api/user/{id}/freeze` | Admin | Freeze user account |
| PUT | `/api/user/{id}/unfreeze` | Admin | Unfreeze user account |

### Contests

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/contest` | Admin | Create contest |
| PUT | `/api/contest` | Admin | Update contest |
| DELETE | `/api/contest/{id}` | Admin | Delete draft contest |
| PUT | `/api/contest/{id}/publish` | Admin | Publish contest (status → open) |
| PUT | `/api/contest/{id}/unpublish` | Admin | Unpublish contest (status → draft) |
| GET | `/api/contest/{id}` | - | Get contest detail |
| GET | `/api/contest/page` | - | Paginated list (keyword, category, status filters) |
| GET | `/api/contest/hot` | - | Top N by registered count |
| GET | `/api/contest/latest` | - | Top N by create time |

### Teams

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/team` | User | Create team (userId, teamName) |
| POST | `/api/team/{teamId}/invite` | Leader | Generate 6-char invite code |
| POST | `/api/team/join` | User | Join team by invite code |
| PUT | `/api/team/{teamId}/members/{memberId}/approve` | Leader | Approve join request |
| PUT | `/api/team/{teamId}/members/{memberId}/reject` | Leader | Reject join request |
| DELETE | `/api/team/{teamId}/members/{memberId}` | Leader | Remove member |
| PUT | `/api/team/{teamId}/dissolve` | Leader | Dissolve team |
| PUT | `/api/team/{teamId}/leave` | Member | Leave team |
| PUT | `/api/team/{teamId}/submit` | Leader | Submit for admin review |
| GET | `/api/team/{teamId}/members` | - | List approved members |
| GET | `/api/team/{teamId}/pending` | Leader | List pending join requests |
| GET | `/api/team/{id}/detail` | - | Get team detail |
| GET | `/api/team/leader` | User | Teams by leader userId |
| GET | `/api/team/user/{userId}` | - | All teams for a user |
| PUT | `/api/team/{teamId}/admin-approve` | Admin | Admin approve team |
| PUT | `/api/team/{teamId}/admin-reject` | Admin | Admin reject team |

### Registrations

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/registration/personal` | User | Register for individual contest |
| POST | `/api/registration/team` | Leader | Register team for team contest |
| PUT | `/api/registration/{id}/approve` | Admin | Approve registration |
| PUT | `/api/registration/{id}/reject` | Admin | Reject registration (reason required) |
| PUT | `/api/registration/{id}/cancel` | User | Cancel own registration |
| GET | `/api/registration/user/{userId}` | User | My registrations |
| GET | `/api/registration/contest/{contestId}` | Admin | Registrations by contest |
| GET | `/api/registration/page` | Admin | Paginated all registrations |

### Notifications & CMS

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/notification/user/{userId}` | User | Paginated notifications |
| GET | `/api/notification/unread/{userId}` | User | Unread count |
| PUT | `/api/notification/{id}/read` | User | Mark as read |
| PUT | `/api/notification/read-all/{userId}` | User | Mark all as read |
| POST | `/api/notification/send` | Admin | Send notification |
| POST | `/api/notification/broadcast` | Admin | Broadcast notification |
| GET | `/api/cms/banners` | - | List banners |
| GET | `/api/cms/announcements` | - | List announcements |
| POST | `/api/cms` | Admin | Create CMS content |
| PUT | `/api/cms` | Admin | Update CMS content |
| DELETE | `/api/cms/{id}` | Admin | Delete CMS content |

### AI Chat

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/ai/chat` | User | SSE streaming AI chat |
| POST | `/api/ai/stop/{sessionId}` | User | Stop AI response generation |

### File Upload

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/upload` | User | Upload file (multipart) |
| GET | `/api/uploads/{filename}` | - | Serve uploaded file |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Node.js 18+
- MySQL 5.7+ / 8.0
- (Optional) DashScope API key for AI assistant
- (Optional) MinIO for file storage (falls back to local filesystem)

### 1. Database Setup

```bash
mysql -u root -p < contest-system/sql/init.sql
mysql -u root -p < contest-system/sql/seed.sql
```

### 2. Backend Setup

Configure database connection in `contest-admin/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/contest_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your-password
```

Build and start:

```bash
cd contest-system
mvn clean install -DskipTests
mvn spring-boot:run -pl contest-admin -am
```

The backend starts at **http://localhost:8080**.

### 3. Frontend Setup

```bash
cd contest-frontend
npm install
npm run dev
```

The frontend starts at **http://localhost:3000** and proxies `/api` requests to the backend.

### 4. Default Credentials

| Username | Password | Role | Name |
|----------|----------|------|------|
| admin | 123456 | Admin | 系统管理员 |
| admin2 | 123456 | Admin | 教务处李老师 |
| s2021001 | 123456 | Student | 张明 |
| s2021002 | 123456 | Student | 李华 |
| s2021003 | 123456 | Student | 王芳 |
| s2021004 | 123456 | Student | 赵雷 |
| s2021005 | 123456 | Student | 陈静 |
| s2021006 | 123456 | Student | 刘洋 |
| s2021007 | 123456 | Student | 孙悦 |
| s2021008 | 123456 | Student | 周杰 |
| s2021009 | 123456 | Student | 吴婷 |
| s2021010 | 123456 | Student | 郑凯 |

---

## Configuration

### application.yml (defaults)

```yaml
server:
  port: 8080

spring:
  servlet:
    multipart:
      max-file-size: 10MB
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
  profiles:
    active: dev

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

contest:
  upload:
    dir: uploads
    max-size: 10MB
  invite-code:
    expire-days: 7
  jwt:
    expire-days: 7
  threshold:
    warn-percent: 80
  deadline:
    remind-hours: 24
```

### application-dev.yml

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/contest_system?...` | MySQL JDBC URL |
| `spring.datasource.username` | `root` | DB username |
| `spring.datasource.password` | `root` | DB password |
| `spring.ai.dashscope.api-key` | `sk-...` | DashScope API key |
| `mybatis-plus.configuration.log-impl` | `StdOutImpl` | SQL logging |
| `minio.endpoint` | `http://localhost:9000` | MinIO endpoint |
| `minio.access-key` | `minioadmin` | MinIO access key |
| `minio.secret-key` | `minioadmin` | MinIO secret key |

### application-prod.yml

Environment variables for production:

| Variable | Description |
|----------|-------------|
| `DB_HOST` | MySQL host:port (default `localhost:3306`) |
| `DB_USER` | MySQL username (default `root`) |
| `DB_PASS` | MySQL password (default `root`) |
| `DASHSCOPE_API_KEY` | DashScope API key for AI |
| `MINIO_ENDPOINT` | MinIO server endpoint |
| `MINIO_ACCESS_KEY` | MinIO access key |
| `MINIO_SECRET_KEY` | MinIO secret key |

### Frontend Vite Config

```js
// vite.config.js
server: {
  port: 3000,
  proxy: { '/api': 'http://localhost:8080' }
}
```

---

## Usage Guide

### Student Workflow

#### 1. Browse Contests
- Navigate to the **Home** page to see hot and latest contests
- Use the **Contest List** page to search by keyword or filter by category
- Click a contest card to view its full details

#### 2. Register for Individual Contest
- On the **Contest Detail** page, click **个人报名** (Personal Registration)
- Optionally add a remark, then submit
- Wait for admin review — check status in **我的报名** (My Registrations)

#### 3. Create a Team
- Go to **创建团队** (Create Team) from the navigation
- Enter a team name and submit
- Share the generated 6-character invite code with teammates

#### 4. Join a Team
- Ask the team leader for the invite code
- Go to **我的团队** (My Teams) and enter the code in the join field
- Wait for the leader to approve your request

#### 5. Register Team for Contest
- The leader creates a team, invites members, and waits for approvals
- The leader submits the team for admin review
- An admin approves the team
- The leader registers the team for the target contest on the contest detail page
- An admin reviews and approves the registration

#### 6. Track Status
- **我的报名** (My Registrations): view all registration records and their review status
- **我的团队** (My Teams): view teams, manage members, submit for review
- **通知** (Notifications): receive real-time updates on review results

### Admin Workflow

#### 1. Manage Contests
- Go to **竞赛管理** (Contest Management) to create, edit, publish, or delete contests
- Set contest type (personal / team / both), time windows, participant limits

#### 2. Review Registrations
- Go to **审核报名** (Review Registrations)
- View registration applications grouped by contest
- Approve or reject with a reason (minimum 5 characters)

#### 3. Review Teams
- Teams submitted for review appear in the team list
- Approve or reject teams (rejection auto-cancels related registrations)

#### 4. Manage Users
- Go to **用户管理** (User Management) to view all users
- Freeze / unfreeze accounts to prevent login

#### 5. CMS Management
- **Banners**: upload images for the homepage carousel
- **Announcements**: create rich-text announcements with optional scheduled publishing

#### 6. Notifications
- Send targeted notifications to individual users
- Broadcast system-wide notifications to all users

### AI Assistant

Click the floating purple bubble (bottom-right corner) to open the AI chat panel. The AI can:

- "Show me my current profile"
- "Search for the Math Contest"
- "Register me for the C Programming Contest"
- "Create a team called Alpha for the ACM Contest"
- "What are my registrations?"
- "List my teams"

---

## Testing

### Test Commands

```bash
# Run backend tests (Team module)
mvn test -pl contest-team -Dtest="TeamServiceImplTest,TeamControllerTest"

# Run backend tests (Registration module)
mvn test -pl contest-register -Dtest="RegistrationServiceImplTest"

# Run frontend tests
cd contest-frontend && npm test

# Run all tests via script
bash test-and-report.sh all "description of changes"
```

### Test Coverage

| Layer | Files | Test Cases | Pass Rate |
|-------|-------|-----------|-----------|
| Backend (Team) | 2 | 62 | 100% |
| Backend (Registration) | 1 | 22 | 100% |
| Frontend (API) | 1 | 17 | 100% |
| Frontend (Views) | 3 | 13 | 100% |
| **Total** | **7** | **114** | **100%** |

### Test Modules

**TeamServiceImplTest (41 tests)** — covers team CRUD, invite codes, member approval/rejection, dissolve, leave, submit for review, admin approval/rejection, and all query scenarios.

**TeamControllerTest (21 tests)** — covers HTTP method/path mapping, parameter validation, and response handling for all team endpoints.

**RegistrationServiceImplTest (22 tests)** — covers personal/team registration validation, duplicate detection, max participants enforcement, approve/reject lifecycle, and cancellation.

**Frontend tests (30 tests)** — covers API HTTP contracts (17 tests) and component rendering (13 tests across ContestDetail, TeamDetail, CreateTeam).

### Known Issues

| ID | Bug | Module | Status |
|----|-----|--------|--------|
| BUG-API-01 | `maxParticipants` does not count pending registrations — users can exceed the limit if all registrations are approved | contest-register | ✅ Fixed |

---

## Project Structure

```
.
├── README.md                          # This file
├── AGENTS.md                          # Agent workflow guide
├── 测试报告.md                         # Test report
├── test-and-report.sh                 # Test execution script
├── run-tests.sh / run-tests.cmd       # Runner helpers
│
├── contest-system/                    # Backend (Java Spring Boot)
│   ├── pom.xml                        # Parent POM
│   ├── contest-admin/                 # Application entry point
│   ├── contest-common/                # Shared config, constants, DTOs
│   ├── contest-user/                  # User authentication & profile
│   ├── contest-competition/           # Contest CRUD
│   ├── contest-team/                  # Team management
│   ├── contest-register/              # Registration management
│   ├── contest-message/               # Notifications, CMS, logs
│   ├── contest-ai/                    # AI assistant
│   └── sql/                           # Database scripts
│       ├── init.sql                   # Schema DDL
│       └── seed.sql                   # Seed data
│
└── contest-frontend/                  # Frontend (Vue 3 SPA)
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.js                    # App bootstrap
        ├── App.vue                    # Root component
        ├── api/                       # Axios API modules (8 files)
        ├── router/index.js            # 17 routes
        ├── stores/user.js             # Pinia store
        ├── utils/format.js            # Utility functions
        ├── components/                # Shared components (5)
        ├── views/                     # Page components (16)
        │   ├── login/
        │   ├── home/
        │   ├── contest/
        │   ├── team/
        │   ├── registration/
        │   ├── notification/
        │   ├── user/
        │   └── admin/
        └── assets/styles/main.css     # Global styles
```

### Frontend Routes

| Path | Page | Auth | Admin |
|------|------|------|-------|
| `/login` | Login | - | - |
| `/home` | Home / Dashboard | - | - |
| `/contest` | Contest List | - | - |
| `/contest/:id` | Contest Detail | - | - |
| `/profile` | Profile Settings | Yes | - |
| `/my-teams` | My Teams | Yes | - |
| `/team/create` | Create Team | Yes | - |
| `/team/:id` | Team Detail | Yes | - |
| `/my-registration` | My Registrations | Yes | - |
| `/notification` | Notifications | Yes | - |
| `/admin` | Admin Dashboard | Yes | Yes |
| `/admin/contest` | Contest Management | Yes | Yes |
| `/admin/review` | Review Registrations | Yes | Yes |
| `/admin/cms` | CMS Management | Yes | Yes |
| `/admin/notification` | Notification Management | Yes | Yes |
| `/admin/users` | User Management | Yes | Yes |
| *404* | Not Found | - | - |

---

## Development

### Coding Conventions

- **Backend**: BDD-style test method names (`registerPersonal_shouldThrowWhenContestNotFound`), AAA pattern (Arrange-Act-Assert), single assertion per test
- **Frontend**: Vue 3 Composition API with `<script setup>`, Element Plus UI components, Pinia for state management
- **API Responses**: Unified `Result<T>` wrapper with `code` (200=success, 400=error, 500=server error), `message`, `data`

### Code Style

- No comments in production code
- Type safety for critical paths
- Follow existing patterns when adding new features
- New components should reference existing implementations for conventions

### Security

- Passwords hashed with BCrypt via Hutool
- JWT tokens with 7-day expiry, stored in `localStorage`
- Role-based access control (student = 0, admin = 1)
- MyBatis-Plus parameterized queries prevent SQL injection
- Global exception handler covers validation, authentication, data integrity errors

### Maven Profiles

```bash
# Development (default)
mvn spring-boot:run -pl contest-admin -am

# Production build
mvn clean package -DskipTests -Pprod
```

---

## License

This project is developed for educational purposes as a university course design.
