# 高校学科竞赛报名管理系统

基于 Spring Boot 3 + Vue 3 + MySQL 的全栈高校竞赛报名管理系统。支持个人/团队竞赛报名、管理员审核流程、内容管理（CMS）以及 AI 智能助手。

---

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [数据库设计](#数据库设计)
- [API 接口总览](#api-接口总览)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [使用指南](#使用指南)
- [测试](#测试)
- [项目结构](#项目结构)
- [开发指南](#开发指南)

---

## 功能特性

### 学生功能

| 功能 | 说明 |
|------|------|
| **浏览竞赛** | 查看所有竞赛，支持关键字搜索、类别筛选、分页 |
| **竞赛详情** | 富文本描述、时间地点、参赛形式、人数限制 |
| **个人报名** | 报名个人赛，可附加备注 |
| **团队管理** | 创建团队、生成6位邀请码、邀请队友 |
| **团队报名** | 队长提交通过审核的团队进行竞赛报名 |
| **报名追踪** | 查看报名记录及审核状态（待审核/已通过/已驳回/已取消） |
| **通知收件箱** | 实时接收审核结果、入队申请、系统公告等通知 |
| **个人设置** | 编辑资料、修改密码、配置通知偏好 |
| **AI 助手** | 与 AI 对话，可查询竞赛、检查报名状态、执行报名操作 |

### 管理员功能

| 功能 | 说明 |
|------|------|
| **竞赛管理** | 创建、编辑、发布/下架、删除竞赛 |
| **报名审核** | 通过或驳回报名申请（需填写驳回原因） |
| **团队审核** | 通过或驳回团队（驳回时自动取消关联的报名） |
| **用户管理** | 查看所有用户、冻结/解冻账号 |
| **内容管理** | 管理首页轮播图和公告（富文本编辑器） |
| **通知管理** | 发送定向通知或系统广播 |
| **操作日志** | 查看管理员操作审计记录 |

### AI 助手功能

AI 助手（基于 DashScope glm-5.1 模型）提供 7 个调用工具：

| 工具 | 说明 |
|------|------|
| `getCurrentUserInfo` | 获取当前登录用户信息 |
| `queryContests` | 浏览竞赛列表（关键字/类别筛选） |
| `searchContestDetail` | 按名称搜索特定竞赛 |
| `registerForContest` | 报名个人竞赛 |
| `createTeamForContest` | 创建团队参加竞赛 |
| `queryMyRegistrations` | 查询本人的报名记录 |
| `queryMyTeams` | 查询本人创建或加入的团队 |

AI 助手支持 SSE 流式对话、对话历史持久化。系统提示词强制助手如实返回工具调用结果，禁止编造答案。

---

## 技术栈

### 后端

| 技术 | 版本 |
|------|------|
| Spring Boot | 3.3.5 |
| Java | 17 |
| MyBatis-Plus | 3.5.16 |
| MySQL | 5.7+ / 8.0 |
| Spring AI Alibaba | 1.0.0.2 |
| JWT (jjwt) | 0.11.5 |
| Hutool | 5.8.36 |
| MinIO（可选） | 8.5.10 |
| Maven | 3.6+ |

### 前端

| 技术 | 版本 |
|------|------|
| Vue 3 | ^3.4.0 |
| Vite | ^5.0.0 |
| Element Plus | ^2.5.0 |
| Pinia | ^2.1.0 |
| Vue Router 4 | ^4.3.0 |
| Axios | ^1.6.0 |
| Vitest | ^4.0.0 |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3 SPA)                              │
│  localhost:3000  ───  Vite Dev Server  ───  Proxy /api → :8080     │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ HTTP (REST + SSE)
┌───────────────────────────▼─────────────────────────────────────────┐
│                      后端 (Spring Boot 3)                            │
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
│                       MySQL 数据库 :3306                              │
│                     contest_system (10 张表)                         │
└─────────────────────────────────────────────────────────────────────┘
```

### 模块依赖关系

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

## 数据库设计

系统使用 `contest_system` 数据库，共 10 张表：

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `user` | 用户表（学生 + 管理员） | id, username（学号）, password（BCrypt）, name, role（0=学生, 1=管理员）, college, major, class_name, status |
| `contest` | 竞赛表 | id, name, category, level, organizer, contest_time, register_start/end_time, location, contest_type（0=个人, 1=团队, 2=两者皆可）, team_min/max_size, max_participants, status, current_count |
| `team` | 团队表 | id, leader_id, team_name, team_no（唯一编号）, invite_code（6位）, status（0=组建中, 1=已提交, 2=已通过, 3=已驳回）, member_count |
| `team_member` | 团队成员关系表 | id, team_id, user_id, role（0=成员, 1=队长）, status（0=待审核, 1=已通过, 2=已拒绝） |
| `registration` | 报名记录表 | id, contest_id, user_id, team_id, reg_type, status（0=待审核, 1=已通过, 2=已驳回, 3=已取消） |
| `notification` | 消息通知表 | id, user_id（0=广播）, type, title, content, related_id, related_type, is_read |
| `cms_content` | 内容管理表 | id, content_type（0=轮播图, 1=公告）, title, content, image_url, sort_order, position, publish_time |
| `operation_log` | 操作日志表 | id, user_id, action, detail, ip_address |
| `ai_conversation` | AI 对话会话表 | id, user_id, title |
| `ai_message` | AI 对话消息表 | id, conversation_id, role（user/assistant）, content, tokens |

---

## API 接口总览

所有接口以 `/api` 为前缀，认证使用 JWT Bearer Token（存储在 localStorage）。

### 认证与用户

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/user/login` | 公开 | 登录，返回用户信息 + JWT Token |
| POST | `/api/user/register` | 公开 | 注册学生账号 |
| GET | `/api/user/{id}` | 公开 | 获取用户信息 |
| GET | `/api/user/page` | 管理员 | 分页用户列表 |
| PUT | `/api/user/{id}/profile` | 用户本人 | 更新个人资料 |
| PUT | `/api/user/{id}/password` | 用户本人 | 修改密码 |
| PUT | `/api/user/{id}/freeze` | 管理员 | 冻结账号 |
| PUT | `/api/user/{id}/unfreeze` | 管理员 | 解冻账号 |

### 竞赛

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/contest` | 管理员 | 创建竞赛 |
| PUT | `/api/contest` | 管理员 | 更新竞赛 |
| DELETE | `/api/contest/{id}` | 管理员 | 删除草稿竞赛 |
| PUT | `/api/contest/{id}/publish` | 管理员 | 发布竞赛 |
| PUT | `/api/contest/{id}/unpublish` | 管理员 | 下架竞赛 |
| GET | `/api/contest/{id}` | 公开 | 获取竞赛详情 |
| GET | `/api/contest/page` | 公开 | 分页列表（关键字、类别、状态筛选） |
| GET | `/api/contest/hot` | 公开 | 热门竞赛 Top N |
| GET | `/api/contest/latest` | 公开 | 最新竞赛 Top N |

### 团队

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/team` | 用户 | 创建团队 |
| POST | `/api/team/{teamId}/invite` | 队长 | 生成6位邀请码 |
| POST | `/api/team/join` | 用户 | 通过邀请码加入团队 |
| PUT | `/api/team/{teamId}/members/{memberId}/approve` | 队长 | 批准入队申请 |
| PUT | `/api/team/{teamId}/members/{memberId}/reject` | 队长 | 拒绝入队申请 |
| DELETE | `/api/team/{teamId}/members/{memberId}` | 队长 | 移除成员 |
| PUT | `/api/team/{teamId}/dissolve` | 队长 | 解散团队 |
| PUT | `/api/team/{teamId}/leave` | 成员 | 退出团队 |
| PUT | `/api/team/{teamId}/submit` | 队长 | 提交团队待管理员审核 |
| GET | `/api/team/{teamId}/members` | 公开 | 获取已通过成员列表 |
| GET | `/api/team/{teamId}/pending` | 队长 | 获取待审核入队申请 |
| GET | `/api/team/{id}/detail` | 公开 | 获取团队详情 |
| GET | `/api/team/leader` | 用户 | 获取本人创建的团队 |
| GET | `/api/team/user/{userId}` | 公开 | 获取用户的所有团队 |
| PUT | `/api/team/{teamId}/admin-approve` | 管理员 | 管理员通过团队 |
| PUT | `/api/team/{teamId}/admin-reject` | 管理员 | 管理员驳回团队 |

### 报名

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/registration/personal` | 用户 | 个人报名 |
| POST | `/api/registration/team` | 队长 | 团队报名 |
| PUT | `/api/registration/{id}/approve` | 管理员 | 通过报名 |
| PUT | `/api/registration/{id}/reject` | 管理员 | 驳回报名（需填写原因） |
| PUT | `/api/registration/{id}/cancel` | 用户 | 取消本人的报名 |
| GET | `/api/registration/user/{userId}` | 用户 | 我的报名记录 |
| GET | `/api/registration/contest/{contestId}` | 管理员 | 按竞赛查看报名 |
| GET | `/api/registration/page` | 管理员 | 分页全部报名记录 |

### 通知与内容管理

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/notification/user/{userId}` | 用户 | 分页通知列表 |
| GET | `/api/notification/unread/{userId}` | 用户 | 未读通知数 |
| PUT | `/api/notification/{id}/read` | 用户 | 标记已读 |
| PUT | `/api/notification/read-all/{userId}` | 用户 | 全部标记已读 |
| POST | `/api/notification/send` | 管理员 | 发送通知 |
| POST | `/api/notification/broadcast` | 管理员 | 广播通知 |
| GET | `/api/cms/banners` | 公开 | 获取轮播图列表 |
| GET | `/api/cms/announcements` | 公开 | 获取公告列表 |
| POST | `/api/cms` | 管理员 | 创建内容 |
| PUT | `/api/cms` | 管理员 | 更新内容 |
| DELETE | `/api/cms/{id}` | 管理员 | 删除内容 |

### AI 对话

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/ai/chat` | 用户 | SSE 流式 AI 对话 |
| POST | `/api/ai/stop/{sessionId}` | 用户 | 停止 AI 回复生成 |

### 文件上传

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/api/upload` | 用户 | 上传文件（multipart） |
| GET | `/api/uploads/{filename}` | 公开 | 获取上传的文件 |

---

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- Node.js 18+
- MySQL 5.7+ / 8.0
- （可选）DashScope API 密钥（用于 AI 助手）
- （可选）MinIO（用于文件存储，默认使用本地文件系统）

### 1. 初始化数据库

```bash
mysql -u root -p < contest-system/sql/init.sql
mysql -u root -p < contest-system/sql/seed.sql
```

### 2. 启动后端

修改 `contest-admin/src/main/resources/application-dev.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/contest_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: your-password
```

编译并启动：

```bash
cd contest-system
mvn clean install -DskipTests
mvn spring-boot:run -pl contest-admin -am
```

后端启动后访问 **http://localhost:8080**。

### 3. 启动前端

```bash
cd contest-frontend
npm install
npm run dev
```

前端启动后访问 **http://localhost:3000**，开发服务器会自动将 `/api` 请求代理到后端。

### 4. 默认账号

| 用户名 | 密码 | 角色 | 姓名 |
|--------|------|------|------|
| admin | 123456 | 管理员 | 系统管理员 |
| admin2 | 123456 | 管理员 | 教务处李老师 |
| s2021001 | 123456 | 学生 | 张明 |
| s2021002 | 123456 | 学生 | 李华 |
| s2021003 | 123456 | 学生 | 王芳 |
| s2021004 | 123456 | 学生 | 赵雷 |
| s2021005 | 123456 | 学生 | 陈静 |
| s2021006 | 123456 | 学生 | 刘洋 |
| s2021007 | 123456 | 学生 | 孙悦 |
| s2021008 | 123456 | 学生 | 周杰 |
| s2021009 | 123456 | 学生 | 吴婷 |
| s2021010 | 123456 | 学生 | 郑凯 |

---

## 配置说明

### application.yml（默认配置）

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

### application-dev.yml（开发环境）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/contest_system?...` | MySQL 连接地址 |
| `spring.datasource.username` | `root` | 数据库用户名 |
| `spring.datasource.password` | `root` | 数据库密码 |
| `spring.ai.dashscope.api-key` | `sk-...` | DashScope API 密钥 |
| `mybatis-plus.configuration.log-impl` | `StdOutImpl` | SQL 日志输出 |
| `minio.endpoint` | `http://localhost:9000` | MinIO 服务地址 |
| `minio.access-key` | `minioadmin` | MinIO 访问密钥 |
| `minio.secret-key` | `minioadmin` | MinIO 秘密密钥 |

### application-prod.yml（生产环境）

生产环境通过环境变量配置：

| 变量 | 说明 |
|------|------|
| `DB_HOST` | MySQL 地址:端口（默认 `localhost:3306`） |
| `DB_USER` | MySQL 用户名（默认 `root`） |
| `DB_PASS` | MySQL 密码（默认 `root`） |
| `DASHSCOPE_API_KEY` | DashScope API 密钥 |
| `MINIO_ENDPOINT` | MinIO 服务地址 |
| `MINIO_ACCESS_KEY` | MinIO 访问密钥 |
| `MINIO_SECRET_KEY` | MinIO 秘密密钥 |

### 前端 Vite 配置

```js
// vite.config.js
server: {
  port: 3000,
  proxy: { '/api': 'http://localhost:8080' }
}
```

---

## 使用指南

### 学生操作流程

#### 1. 浏览竞赛
- 进入**首页**查看热门和最新竞赛
- 进入**竞赛列表**按关键字搜索或按类别筛选
- 点击竞赛卡片查看完整详情

#### 2. 个人报名
- 在**竞赛详情**页点击**个人报名**按钮
- 可选添加备注，提交报名
- 等待管理员审核 — 在**我的报名**中查看状态

#### 3. 创建团队
- 从导航栏进入**创建团队**
- 输入团队名称并提交
- 将生成的6位邀请码分享给队友

#### 4. 加入团队
- 向队长索要邀请码
- 前往**我的团队**，在加入表单中输入邀请码
- 等待队长审批

#### 5. 团队报名流程
- 队长创建团队 → 邀请成员 → 队长批准入队申请
- 队长提交团队待管理员审核
- 管理员通过团队
- 队长在竞赛详情页为团队报名
- 管理员审核报名

#### 6. 状态追踪
- **我的报名**：查看所有报名记录及审核状态
- **我的团队**：查看团队、管理成员、提交审核
- **通知**：实时接收审核结果更新

### 管理员操作流程

#### 1. 竞赛管理
- 进入**竞赛管理**创建、编辑、发布或删除竞赛
- 设置参赛形式（个人/团队/两者皆可）、时间窗口、人数上限

#### 2. 报名审核
- 进入**审核报名**
- 按竞赛查看报名申请列表
- 通过或驳回（驳回原因不少于5个字符）

#### 3. 团队审核
- 查看已提交审核的团队列表
- 通过或驳回（驳回自动取消关联的报名记录）

#### 4. 用户管理
- 进入**用户管理**查看所有用户
- 冻结/解冻账号（冻结后无法登录）

#### 5. 内容管理
- **轮播图**：上传首页轮播图片
- **公告**：创建富文本公告，支持定时发布

#### 6. 通知管理
- 发送定向通知给指定用户
- 发送系统广播通知给所有用户

### AI 助手

点击右下角悬浮紫色气泡打开 AI 对话面板。可以尝试以下对话：

- "查看我的个人信息"
- "搜索数学建模竞赛"
- "帮我报名C语言编程竞赛"
- "创建一个名为Alpha的团队参加ACM竞赛"
- "查看我的报名记录"
- "列出我的团队"

---

## 测试

### 测试命令

```bash
# 运行后端团队模块测试
mvn test -pl contest-team -Dtest="TeamServiceImplTest,TeamControllerTest"

# 运行后端报名模块测试
mvn test -pl contest-register -Dtest="RegistrationServiceImplTest"

# 运行前端测试
cd contest-frontend && npm test

# 一键运行全部测试
bash test-and-report.sh all "变更说明"
```

### 测试覆盖

| 层级 | 文件数 | 用例数 | 通过率 |
|------|--------|--------|--------|
| 后端（团队） | 2 | 62 | 100% |
| 后端（报名） | 1 | 22 | 100% |
| 前端（API） | 1 | 17 | 100% |
| 前端（视图） | 3 | 13 | 100% |
| **总计** | **7** | **114** | **100%** |

### 测试模块说明

**TeamServiceImplTest（41 用例）** — 覆盖团队 CRUD、邀请码、成员审批/拒绝、解散、退出、提交审核、管理员审批/驳回、全部查询场景。

**TeamControllerTest（21 用例）** — 覆盖 HTTP 方法/路径映射、参数校验、所有团队接口的响应处理。

**RegistrationServiceImplTest（22 用例）** — 覆盖个人/团队报名校验、重复检测、人数上限、审批/拒绝生命周期、取消报名。

**前端测试（30 用例）** — 覆盖 API 层 HTTP 契约（17 用例）和组件渲染（13 用例，涉及 ContestDetail、TeamDetail、CreateTeam）。

### 已知问题

| ID | 缺陷 | 模块 | 状态 |
|----|------|------|------|
| BUG-API-01 | `maxParticipants` 未统计 pending 状态的报名数 — 若全部通过审核将超额 | contest-register | ✅ 已修复 |

---

## 项目结构

```
.
├── README.md                          # 本文件
├── AGENTS.md                          # Agent 工作流指南
├── 测试报告.md                         # 测试报告
├── test-and-report.sh                 # 测试执行脚本
├── run-tests.sh / run-tests.cmd       # 运行辅助脚本
│
├── contest-system/                    # 后端（Java Spring Boot）
│   ├── pom.xml                        # 父 POM
│   ├── contest-admin/                 # 应用入口
│   ├── contest-common/                # 公共配置、常量、DTO
│   ├── contest-user/                  # 用户认证与资料
│   ├── contest-competition/           # 竞赛管理
│   ├── contest-team/                  # 团队管理
│   ├── contest-register/              # 报名管理
│   ├── contest-message/               # 通知、CMS、日志
│   ├── contest-ai/                    # AI 助手
│   └── sql/                           # 数据库脚本
│       ├── init.sql                   # 表结构 DDL
│       └── seed.sql                   # 种子数据
│
└── contest-frontend/                  # 前端（Vue 3 SPA）
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.js                    # 应用入口
        ├── App.vue                    # 根组件
        ├── api/                       # Axios API 模块（8 个文件）
        ├── router/index.js            # 17 条路由
        ├── stores/user.js             # Pinia 状态管理
        ├── utils/format.js            # 工具函数
        ├── components/                # 共享组件（5 个）
        ├── views/                     # 页面组件（16 个）
        │   ├── login/
        │   ├── home/
        │   ├── contest/
        │   ├── team/
        │   ├── registration/
        │   ├── notification/
        │   ├── user/
        │   └── admin/
        └── assets/styles/main.css     # 全局样式
```

### 前端路由

| 路径 | 页面 | 需登录 | 需管理员 |
|------|------|--------|----------|
| `/login` | 登录 | - | - |
| `/home` | 首页 | - | - |
| `/contest` | 竞赛列表 | - | - |
| `/contest/:id` | 竞赛详情 | - | - |
| `/profile` | 个人设置 | 是 | - |
| `/my-teams` | 我的团队 | 是 | - |
| `/team/create` | 创建团队 | 是 | - |
| `/team/:id` | 团队详情 | 是 | - |
| `/my-registration` | 我的报名 | 是 | - |
| `/notification` | 通知列表 | 是 | - |
| `/admin` | 管理后台首页 | 是 | 是 |
| `/admin/contest` | 竞赛管理 | 是 | 是 |
| `/admin/review` | 审核报名 | 是 | 是 |
| `/admin/cms` | 内容管理 | 是 | 是 |
| `/admin/notification` | 通知管理 | 是 | 是 |
| `/admin/users` | 用户管理 | 是 | 是 |
| *404* | 页面未找到 | - | - |

---

## 开发指南

### 编码规范

- **后端**：BDD 风格测试方法命名（如 `registerPersonal_shouldThrowWhenContestNotFound`），AAA 模式（Arrange-Act-Assert），单用例单断言
- **前端**：Vue 3 Composition API + `<script setup>`，Element Plus 组件，Pinia 状态管理
- **API 响应**：统一 `Result<T>` 封装，包含 `code`（200=成功, 400=业务错误, 500=服务端错误）、`message`、`data`

### 代码风格

- 生产代码不加注释
- 关键路径保证类型安全
- 新增功能遵循现有模式
- 新组件参考已有实现了解约定

### 安全机制

- 密码使用 Hutool BCrypt 加密存储
- JWT Token 7 天过期，存储在 localStorage
- 基于角色的访问控制（学生=0, 管理员=1）
- MyBatis-Plus 参数化查询防止 SQL 注入
- 全局异常处理器覆盖参数校验、认证鉴权、数据完整性错误

### Maven 构建

```bash
# 开发模式（默认）
mvn spring-boot:run -pl contest-admin -am

# 生产打包
mvn clean package -DskipTests -Pprod
```

---

## 许可证

本项目为课程设计，仅供学习参考。
