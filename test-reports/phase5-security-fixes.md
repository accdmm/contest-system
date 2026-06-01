# 阶段五：安全修复测试报告

## 修复内容

### Module 1：UserController 安全修复

| 问题 | 文件 | 修复 |
|------|------|------|
| `GET /api/user/{id}` 完全公开（无需认证） | `UserController.java:84` | 添加 `@PreAuthorize("isAuthenticated()")`，并将路径改为 `/detail/{id}` 避免路由冲突 |
| `PUT /api/user/{id}/profile` 无所有权校验 | `UserController.java:102-108` | 校验 `SecurityUtil.getCurrentUserId()` 是否等于路径 `{id}` |
| `PUT /api/user/{id}/password` 无所有权校验 | `UserController.java:110-116` | 同上，添加所有权校验 |

### Module 2：通知越权修复

| 问题 | 文件 | 修复 |
|------|------|------|
| `GET /api/notification/user/{userId}` 可查看任意用户通知 | `NotificationController.java:21-27` | 校验路径 userId 与当前用户匹配 |
| `GET /api/notification/unread/{userId}` 可查看任意用户未读数 | `NotificationController.java:29-33` | 同上 |
| `PUT /api/notification/read-all/{userId}` 可标记他人通知已读 | `NotificationController.java:43-48` | 同上 |

## 测试结果（2026-06-01）

| # | 测试用例 | 预期 | 结果 |
|---|---------|------|------|
| 1 | 已认证用户获取自己的详情 `GET /api/user/detail/1` | 200 返回用户数据 | ✅ |
| 2 | 已认证用户访问旧的 `/api/user/1` 路径 | 500（无 handler） | ✅（旧路径无冲突） |
| 3 | 未认证用户访问 `GET /api/user/detail/1` | 403 Forbidden | ✅ |
| 4 | 用户 admin(1) 修改用户 2 的资料 | 400 "无权修改其他用户的资料" | ✅ |
| 5 | 用户 admin(1) 修改用户 2 的密码 | 400 "无权修改其他用户的密码" | ✅ |
| 6 | 用户查看自己的通知列表 | 200 正常 | ✅ |
| 7 | 用户查看用户 2 的通知列表 | 400 "无权查看其他用户的通知" | ✅ |

## 涉及文件变更

- `contest-system/contest-user/.../UserController.java` — 3 处修改
- `contest-system/contest-message/.../NotificationController.java` — 3 处修改
- `contest-frontend/src/api/user.js` — `getUserById` 端点从 `/user/${id}` 改为 `/user/detail/${id}`
