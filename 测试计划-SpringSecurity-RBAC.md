# 高校竞赛报名管理系统 — Spring Security + JWT + RBAC 测试计划

## 目录

1. [认证测试 (Authentication)](#1-认证测试-authentication)
2. [权限测试 (Authorization) — 逐端点覆盖](#2-权限测试-authorization--逐端点覆盖)
3. [IDOR 回归测试](#3-idor-回归测试)
4. [边界情况](#4-边界情况)
5. [测试数据准备](#5-测试数据准备)

---

## 1. 认证测试 (Authentication)

### 测试用户

| 用户 | 用户名 | 密码 | 角色 | 状态 |
|------|--------|------|------|------|
| 学生A | student1 | pass123 | 0 (学生) | 正常 |
| 学生B | student2 | pass123 | 0 (学生) | 正常 |
| 管理员A | admin1 | admin123 | 1 (管理员) | 正常 |
| 冻结用户 | frozen1 | pass123 | 0 (学生) | 冻结 (status=0) |

### 测试用例

| 编号 | 测试点 | 前置条件 | 操作步骤 | 期望结果 | 实际结果 |
|------|--------|----------|----------|----------|----------|
| AUTH-01 | 正常登录 — 学生 | 学生A已注册，状态正常 | POST /api/user/login `{"username":"student1","password":"pass123"}` | 200; 返回`data.user`含用户信息、`data.token`为JWT字符串; token解析后含userId/username/role; Set-Cookie无(无状态) | |
| AUTH-02 | 正常登录 — 管理员 | 管理员A已注册，状态正常 | POST /api/user/login `{"username":"admin1","password":"admin123"}` | 200; 返回`data.token`; role=1 | |
| AUTH-03 | 错误密码 | 学生A存在 | POST /api/user/login `{"username":"student1","password":"wrong"}` | 400; `{"code":400,"message":"用户名或密码错误"}` | |
| AUTH-04 | 不存在用户 | 无此用户 | POST /api/user/login `{"username":"nobody","password":"xxx"}` | 400; `{"code":400,"message":"用户名或密码错误"}` | |
| AUTH-05 | 注册新用户 — 成功 | 用户名唯一 | POST /api/user/register `{"username":"newuser","password":"123456","name":"新人","email":"new@test.com","phone":"13800000000"}` | 200; 返回`data.user`(password为null)和`data.token`; role默认为0 | |
| AUTH-06 | 注册重复用户名 | student1已存在 | POST /api/user/register `{"username":"student1","password":"123456","name":"重复","email":"dup@test.com","phone":"13800000001"}` | 400; `{"code":400,"message":"用户名已存在"}` | |
| AUTH-07 | 无token访问受保护接口 | — | GET /api/team/leader (无Authorization头) | 401; `{"code":401,"message":"未登录"}` (或Spring Security默认401) | |
| AUTH-08 | 空Authorization头 | — | GET /api/team/leader `Authorization: ` (空值) | 401; 无token放行后过滤器链返回401 | |
| AUTH-09 | 无效token格式 | — | GET /api/team/leader `Authorization: Bearer invalid.jwt.here` | 401; JWT解析失败, 无认证信息注入, 返回401 | |
| AUTH-10 | 过期token | 生成一个过期token | 手动构造expired JWT; GET /api/team/leader `Authorization: Bearer <过期token>` | 401; JWT解析抛出ExpiredJwtException, 认证失败 | |
| AUTH-11 | token被篡改 | 截获student1的token, 修改payload | GET /api/team/leader `Authorization: Bearer <篡改后token>` | 401; 签名校验失败 | |
| AUTH-12 | 已冻结用户登录 | frozen1状态为冻结(status=0或1??) | POST /api/user/login `{"username":"frozen1","password":"pass123"}` | 400; `{"code":400,"message":"账户已被冻结"}` (需确认后端逻辑) | |
| AUTH-13 | 已冻结用户访问接口 | frozen1能登录(若后端未拦截) | 持有frozen1的token访问GET /api/user/page | 403 或业务层拒绝 | |
| AUTH-14 | 公共端点无需token | — | GET /api/contest/page (无token) | 200; 正常返回竞赛分页数据 | |
| AUTH-15 | 非Bearer格式token | — | GET /api/team/leader `Authorization: Basic xxxxxx` | 401; JwtAuthFilter不处理非Bearer头, 后续链返回401 | |

---

## 2. 权限测试 (Authorization) — 逐端点覆盖

### 约定

- **Role=0 (学生):** 期望通过 `isAuthenticated()` 校验的端点, 不能通过需要特定 `hasAuthority(...)` 的端点
- **Role=1 (管理员):** 期望通过所有端点 (包含默认全部权限的回退逻辑)
- **无Token:** 期望非白名单端点返回 401
- **权限不足时:** 期望返回 403 + `{"code":403,"message":"无权限"}` (由 GlobalExceptionHandler 处理 AccessDeniedException)

### 2.1 UserController

| 编号 | 端点 | 权限要求 | Role=0 (学生) | Role=1 (管理员) | 无Token | userId来源变化 |
|------|------|----------|--------------|----------------|---------|---------------|
| AUTH-16 | POST /api/user/login | PUBLIC | 200 → 登录成功 | 200 → 登录成功 | 200 (同左) | 无userId概念 |
| AUTH-17 | POST /api/user/register | PUBLIC | 200 → 注册成功 | 200 → 注册成功 | 200 (同左) | 无userId概念 |
| AU-01 | GET /api/user/1 | (any authenticated) | 200 → 返回用户信息 | 200 → 返回用户信息 | 401 | 路径id, 未变更 |
| AU-02 | GET /api/user/page | `user:list` | 403 | 200 → 分页用户列表 | 401 | N/A |
| AU-03 | PUT /api/user/1/profile | `isAuthenticated()` | 200 → 更新成功 | 200 → 更新成功 | 401 | **路径id; 使用SecurityUtil.getCurrentUserId()但未与路径id比对 → IDOR风险** |
| AU-04 | PUT /api/user/1/password | `isAuthenticated()` | 200 → 修改成功 | 200 → 修改成功 | 401 | **路径id; 同上 → IDOR风险** |
| AU-05 | PUT /api/user/1/freeze | `user:freeze` | 403 | 200 → 冻结成功 | 401 | 路径id, 未变更 |
| AU-06 | PUT /api/user/1/unfreeze | `user:freeze` | 403 | 200 → 解冻成功 | 401 | 路径id, 未变更 |

### 2.2 ContestController

| 编号 | 端点 | 权限要求 | Role=0 | Role=1 | 无Token | userId来源变化 |
|------|------|----------|--------|--------|---------|---------------|
| AU-07 | POST /api/contest | `contest:create` | 403 | 200 → 创建成功 | 401 | N/A |
| AU-08 | PUT /api/contest | `contest:update` | 403 | 200 → 更新成功 | 401 | N/A |
| AU-09 | DELETE /api/contest/1 | `contest:delete` | 403 | 200 → 删除成功 | 401 | N/A |
| AU-10 | PUT /api/contest/1/publish | `contest:publish` | 403 | 200 → 发布成功 | 401 | N/A |
| AU-11 | PUT /api/contest/1/unpublish | `contest:publish` | 403 | 200 → 取消发布成功 | 401 | N/A |
| AU-12 | GET /api/contest/1 | PUBLIC | 200 | 200 | 200 | N/A |
| AU-13 | GET /api/contest/page | PUBLIC | 200 | 200 | 200 | N/A |
| AU-14 | GET /api/contest/hot | PUBLIC | 200 | 200 | 200 | N/A |
| AU-15 | GET /api/contest/latest | PUBLIC | 200 | 200 | 200 | N/A |

### 2.3 TeamController

| 编号 | 端点 | 权限要求 | Role=0 | Role=1 | 无Token | userId来源变化 |
|------|------|----------|--------|--------|---------|---------------|
| AU-16 | POST /api/team | `isAuthenticated()` | 200 → 创建团队 | 200 → 创建团队 | 401 | **原client传userId → 改为SecurityUtil.getCurrentUserId()** |
| AU-17 | POST /api/team/1/invite | `isAuthenticated()` | 200 → 生成邀请码 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-18 | POST /api/team/join | `isAuthenticated()` | 200 → 加入团队 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-19 | PUT /api/team/1/members/2/approve | `isAuthenticated()` | 200 → 通过成员 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-20 | PUT /api/team/1/members/2/reject | `isAuthenticated()` | 200 → 拒绝成员 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-21 | DELETE /api/team/1/members/2 | `isAuthenticated()` | 200 → 移除成员 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-22 | PUT /api/team/1/dissolve | `isAuthenticated()` | 200 → 解散团队 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-23 | PUT /api/team/1/leave | `isAuthenticated()` | 200 → 离开团队 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-24 | PUT /api/team/1/submit | `isAuthenticated()` | 200 → 提交审核 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-25 | GET /api/team/1/members | `isAuthenticated()` | 200 → 成员列表 | 200 | 401 | N/A |
| AU-26 | GET /api/team/1/pending | `isAuthenticated()` | 200 → 待审核列表 | 200 | 401 | N/A |
| AU-27 | GET /api/team/1/detail | `isAuthenticated()` | 200 → 团队详情 | 200 | 401 | N/A |
| AU-28 | GET /api/team/leader | `isAuthenticated()` | 200 → 我的团队 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-29 | GET /api/team/page | `team:list` | 403 | 200 → 分页团队 | 401 | N/A |
| AU-30 | GET /api/team/user/1 | `isAuthenticated()` | 200 → 用户团队列表 | 200 | 401 | 路径userId, **未用SecurityUtil → IDOR风险** |
| AU-31 | PUT /api/team/1/admin-approve | `team:approve` | 403 | 200 → 管理通过 | 401 | N/A |
| AU-32 | PUT /api/team/1/admin-reject | `team:approve` | 403 | 200 → 管理驳回 | 401 | N/A |

### 2.4 RegistrationController

| 编号 | 端点 | 权限要求 | Role=0 | Role=1 | 无Token | userId来源变化 |
|------|------|----------|--------|--------|---------|---------------|
| AU-33 | POST /api/registration/personal | `isAuthenticated()` | 200 → 个人报名 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-34 | POST /api/registration/team | `isAuthenticated()` | 200 → 团队报名 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-35 | PUT /api/registration/1/approve | `registration:approve` | 403 | 200 → 审核通过 | 401 | N/A |
| AU-36 | PUT /api/registration/1/reject | `registration:approve` | 403 | 200 → 审核驳回 | 401 | N/A |
| AU-37 | PUT /api/registration/1/cancel | `isAuthenticated()` | 200 → 取消报名 | 200 | 401 | **原client传userId → 改为SecurityUtil** |
| AU-38 | GET /api/registration/user/1 | `isAuthenticated()` | 200 → 用户报名列表 | 200 | 401 | 路径userId, **未用SecurityUtil → IDOR风险** |
| AU-39 | GET /api/registration/contest/1 | `registration:list` | 403 | 200 → 竞赛报名列表 | 401 | N/A |
| AU-40 | GET /api/registration/page | `registration:list` | 403 | 200 → 全部报名分页 | 401 | N/A |
| AU-41 | GET /api/registration/1 | `isAuthenticated()` | 200 → 报名详情 | 200 | 401 | N/A |

### 2.5 NotificationController

| 编号 | 端点 | 权限要求 | Role=0 | Role=1 | 无Token | userId来源变化 |
|------|------|----------|--------|--------|---------|---------------|
| AU-42 | GET /api/notification/user/1 | `isAuthenticated()` | 200 → 用户通知列表 | 200 | 401 | 路径userId, **未用SecurityUtil → IDOR风险** |
| AU-43 | GET /api/notification/unread/1 | `isAuthenticated()` | 200 → 未读数 | 200 | 401 | 路径userId, **未用SecurityUtil → IDOR风险** |
| AU-44 | PUT /api/notification/1/read | `isAuthenticated()` | 200 → 标记已读 | 200 | 401 | **使用SecurityUtil校验所有权** |
| AU-45 | PUT /api/notification/read-all/1 | `isAuthenticated()` | 200 → 全部已读 | 200 | 401 | 路径userId, **未用SecurityUtil → IDOR风险** |
| AU-46 | POST /api/notification/send | `notification:send` | 403 | 200 → 发送成功 | 401 | N/A |
| AU-47 | POST /api/notification/broadcast | `notification:broadcast` | 403 | 200 → 广播成功 | 401 | N/A |

### 2.6 CmsContentController

| 编号 | 端点 | 权限要求 | Role=0 | Role=1 | 无Token | userId来源变化 |
|------|------|----------|--------|--------|---------|---------------|
| AU-48 | GET /api/cms/banners | PUBLIC | 200 | 200 | 200 | N/A |
| AU-49 | GET /api/cms/announcements | PUBLIC | 200 | 200 | 200 | N/A |
| AU-50 | POST /api/cms | `cms:create` | 403 | 200 → 创建CMS内容 | 401 | N/A |
| AU-51 | PUT /api/cms | `cms:update` | 403 | 200 → 更新CMS | 401 | N/A |
| AU-52 | GET /api/cms/1 | PUBLIC | 200 | 200 | 200 | N/A |
| AU-53 | DELETE /api/cms/1 | `cms:delete` | 403 | 200 → 删除CMS | 401 | N/A |

### 2.7 OperationLogController

| 编号 | 端点 | 权限要求 | Role=0 | Role=1 | 无Token | userId来源变化 |
|------|------|----------|--------|--------|---------|---------------|
| AU-54 | GET /api/log/page | `log:list` | 403 | 200 → 日志分页 | 401 | N/A |

### 2.8 PermissionController

| 编号 | 端点 | 权限要求 | Role=0 | Role=1 | 无Token | userId来源变化 |
|------|------|----------|--------|--------|---------|---------------|
| AU-55 | GET /api/permission/list | `user:list` | 403 | 200 → 权限列表 | 401 | N/A |
| AU-56 | GET /api/permission/role/0 | `user:list` | 403 | 200 → 角色权限ID列表 | 401 | N/A |
| AU-57 | PUT /api/permission/role/0 | `user:list` | 403 | 200 → 保存角色权限 | 401 | N/A |

### 2.9 UploadController

| 编号 | 端点 | 权限要求 | Role=0 | Role=1 | 无Token | userId来源变化 |
|------|------|----------|--------|--------|---------|---------------|
| AU-58 | POST /api/upload | `isAuthenticated()` | 200 → 上传文件 | 200 | 401 | N/A |
| AU-59 | GET /api/uploads/test.png | PUBLIC | 200 | 200 | 200 | N/A |

### 2.10 AiChatController

| 编号 | 端点 | 权限要求 | Role=0 | Role=1 | 无Token | userId来源变化 |
|------|------|----------|--------|--------|---------|---------------|
| AU-60 | POST /api/ai/chat | `isAuthenticated()` | 200 → SSE流式响应 | 200 | 401 | **请求头自行解析JWT → 不一致; 建议统一用SecurityUtil** |
| AU-61 | POST /api/ai/stop/1 | `isAuthenticated()` | 200 → 停止成功 | 200 | 401 | N/A |

---

## 3. IDOR 回归测试

> **背景:** 改造前部分接口由客户端传入 userId 参数; 改造后应统一从 JWT 解析。以下测试验证是否存在"用户A可操作用户B数据"的漏洞。

### 3.1 Profile 与密码 — 高危

| 编号 | 测试点 | 前置条件 | 操作步骤 | 期望结果 | 实际结果 |
|------|--------|----------|----------|----------|----------|
| IDOR-01 | 学生A修改学生B的profile | 学生A的JWT(userId=1); 学生B存在(id=2) | PUT /api/user/2/profile `Authorization: Bearer <student1_token>` `{"name":"Hacked"}` | **当前代码漏洞: 200 → 修改成功(应为403);** updateProfile未校验SecurityUtil.getCurrentUserId()是否等于路径id | |
| IDOR-02 | 学生A修改学生B的密码 | 同上 | PUT /api/user/2/password `Authorization: Bearer <student1_token>` `{"oldPassword":"...","newPassword":"..."}` | **当前代码漏洞: 200 → 修改成功(应为403);** changePassword未做所有权校验 | |
| IDOR-03 | 学生A通过body伪造userId修改自己profile(旧客户端方式) | 学生A JWT userId=1 | PUT /api/user/1/profile body中带`userId: 999` | 200 → 修改的是id=1的用户; 服务端忽略body中的userId | |

### 3.2 团队操作 — 低风险 (已改用SecurityUtil)

| 编号 | 测试点 | 前置条件 | 操作步骤 | 期望结果 | 实际结果 |
|------|--------|----------|----------|----------|----------|
| IDOR-04 | 学生A创建团队时body传他人userId | 学生A JWT | POST /api/team body: `{"teamName":"MyTeam","userId":999}` | 200 → 队长为SecurityUtil.getCurrentUserId()(即学生A); body中的userId被忽略 | |
| IDOR-05 | 学生A用B的teamId解散B的团队 | 学生A JWT; B的团队teamId=2; 学生A不是该队队长 | PUT /api/team/2/dissolve | 403 (service层判断队长权限) | |
| IDOR-06 | 学生A代替学生B加入团队 | 学生A JWT | POST /api/team/join body: `{"inviteCode":"ABC"}` | 200 → 加入者为SecurityUtil.getCurrentUserId()(即A) | |

### 3.3 报名操作 — 低风险 (已改用SecurityUtil)

| 编号 | 测试点 | 前置条件 | 操作步骤 | 期望结果 | 实际结果 |
|------|--------|----------|----------|----------|----------|
| IDOR-07 | 学生A替学生B报名 | 学生A JWT | POST /api/registration/personal body: `{"contestId":1,"userId":999}` | 200 → 报名人为SecurityUtil.getCurrentUserId()(即A) | |
| IDOR-08 | 学生A取消学生B的报名 | 学生A JWT; B的报名记录id=5 | PUT /api/registration/5/cancel | 403 (service层判断报名人是否匹配) | |

### 3.4 通知操作 — 中风险 (部分未校验)

| 编号 | 测试点 | 前置条件 | 操作步骤 | 期望结果 | 实际结果 |
|------|--------|----------|----------|----------|----------|
| IDOR-09 | 学生A查看学生B的通知 | 学生A JWT | GET /api/notification/user/2 | **潜在风险: 200 → 返回B的通知;** 未校验userId与当前用户一致 | |
| IDOR-10 | 学生A标记学生B所有通知已读 | 学生A JWT | PUT /api/notification/read-all/2 | **潜在风险: 200 → B的通知被标记已读;** 未校验所有权 | |
| IDOR-11 | 学生A查看其他用户报名记录 | 学生A JWT | GET /api/registration/user/2 | **潜在风险: 200 → 返回B的报名记录;** 未校验所有权 | |

### 3.5 用户信息查询

| 编号 | 测试点 | 前置条件 | 操作步骤 | 期望结果 | 实际结果 |
|------|--------|----------|----------|----------|----------|
| IDOR-12 | 学生A查看学生B的团队 | 学生A JWT | GET /api/team/user/2 | **潜在风险: 200 → 返回B的团队列表;** 未校验所有权 | |
| IDOR-13 | 学生A查看学生B的用户详情 | 学生A JWT | GET /api/user/2 | 200 (用户详情为公开信息, 无敏感字段, password已置null) | |

---

## 4. 边界情况

| 编号 | 测试点 | 前置条件 | 操作步骤 | 期望结果 | 实际结果 |
|------|--------|----------|----------|----------|----------|
| BND-01 | token在过期临界点 | token还有1秒过期 | 持有该token请求受保护接口 | 200 (在过期前有效) | |
| BND-02 | token刚过期 | token已过期1秒 | 持有该token请求受保护接口 | 401 | |
| BND-03 | 并发请求 — 同一用户同时发起10个请求 | 学生A token | 并发10个GET /api/team/leader | 全部200; SecurityContextHolder为线程安全, 无串号 | |
| BND-04 | 并发请求 — 不同用户同时操作 | 学生A,B各持token | 并发: A修改B的profile(若漏洞未修复) + B修改密码 | 各自操作互不影响 | |
| BND-05 | 权限变更后立即生效 | 管理员A: 为角色0新增`contest:create`权限 | PUT /api/permission/role/0; 立即用学生A token POST /api/contest | **取决于PermissionService缓存策略?** 当前无缓存, 每次请求查DB → 200 | |
| BND-06 | 权限变更后撤销 | 管理员A: 从角色0移除`user:list` | PUT /api/permission/role/0; 立即用学生A token GET /api/user/page | 403 (DB实时查询) | |
| BND-07 | role_permission表无数据(新系统) | 清空role_permission表 | 管理员A token访问所有管理员端点 | 200 (PermissionService回退策略: role=1返回全部权限) | |
| BND-08 | 管理页面未初始化角色0权限 | role_permission表无role=0数据 | 学生A token访问需权限端点如`contest:create` | 403 (PermissionService角色0回退为空Set) | |
| BND-09 | 角色值越界 | 手动在DB插入role=99的用户 | 该用户登录后获取token; 访问受保护接口 | role=99不在role_permission表中 → 按非1管理员回退为空Set; 仅通过isAuthenticated()端点 | |
| BND-10 | JWT中无role字段 | 手动构造不含role的JWT | 请求受保护接口 | JwtAuthFilter中getRole返回null → filterChain.doFilter, 无认证注入 → 401 | |
| BND-11 | Authorization头包含特殊字符 | — | `Authorization: Bearer token\n injection` | JWT解析失败 → 401 | |
| BND-12 | 连续快速登录5次 | — | 同一IP连续POST /api/user/login 5次 | 暂时无限流, 全部正常返回; **建议增加限流** | |
| BND-13 | 注册时用户名包含SQL注入 | — | POST /api/user/register `username: "' OR 1=1--"` | 400 (需确认后端有参数校验或MyBatis-Plus参数化查询) | |
| BND-14 | 数据库查询异常时权限回退 | 模拟PermissionService查询DB抛异常 | 管理员A请求受保护端点 | 200 (catch块返回allPermissions) | |
| BND-15 | 超长token | — | `Authorization: Bearer <10MB字符串>` | JWT解析抛出异常 → catch块捕获 → 401 | |

---

## 5. 测试数据准备

### 5.1 测试用户

```sql
-- 学生 (正常)
INSERT INTO `user` (id, username, password, name, role, status, email, phone, create_time, update_time)
VALUES (1, 'student1', '$2a$10$...', '张三', 0, 1, 's1@test.com', '13800000001', NOW(), NOW());

-- 学生 (正常)
INSERT INTO `user` (id, username, password, name, role, status, email, phone, create_time, update_time)
VALUES (2, 'student2', '$2a$10$...', '李四', 0, 1, 's2@test.com', '13800000002', NOW(), NOW());

-- 管理员
INSERT INTO `user` (id, username, password, name, role, status, email, phone, create_time, update_time)
VALUES (3, 'admin1', '$2a$10$...', '管理员', 1, 1, 'admin@test.com', '13800000003', NOW(), NOW());

-- 冻结用户
INSERT INTO `user` (id, username, password, name, role, status, email, phone, create_time, update_time)
VALUES (4, 'frozen1', '$2a$10$...', '冻结用户', 0, 0, 'frozen@test.com', '13800000004', NOW(), NOW());
```

### 5.2 测试竞赛

```sql
-- 已发布竞赛
INSERT INTO `contest` (id, title, description, category, status, contest_type, max_team_members, start_time, end_time, create_time, update_time)
VALUES (1, '2024全国大学生算法竞赛', '算法设计竞赛', '算法', 1, 0, 3, '2024-06-01', '2024-07-01', NOW(), NOW());

-- 未发布竞赛
INSERT INTO `contest` (id, title, description, category, status, contest_type, max_team_members, start_time, end_time, create_time, update_time)
VALUES (2, '未发布竞赛', '待发布', '其他', 0, 1, 3, '2024-08-01', '2024-09-01', NOW(), NOW());
```

### 5.3 测试团队

```sql
-- 学生1创建的团队
INSERT INTO `team` (id, team_name, leader_id, contest_id, status, invite_code, create_time, update_time)
VALUES (1, '张三的团队', 1, 1, 0, 'ABC123', NOW(), NOW());

-- 学生2作为队长(用于IDOR测试)
INSERT INTO `team` (id, team_name, leader_id, contest_id, status, invite_code, create_time, update_time)
VALUES (2, '李四的团队', 2, 1, 0, 'XYZ789', NOW(), NOW());

-- 团队成员
INSERT INTO `team_member` (id, team_id, user_id, status, create_time)
VALUES (1, 1, 1, 1, NOW());   -- 队长, 已通过
INSERT INTO `team_member` (id, team_id, user_id, status, create_time)
VALUES (2, 1, 2, 0, NOW());   -- 待审核
```

### 5.4 报名记录

```sql
-- 学生1报名竞赛1
INSERT INTO `registration` (id, user_id, contest_id, team_id, status, remark, create_time, update_time)
VALUES (1, 1, 1, NULL, 0, '个人报名', NOW(), NOW());

-- 学生2的报名(用于IDOR测试)
INSERT INTO `registration` (id, user_id, contest_id, team_id, status, remark, create_time, update_time)
VALUES (2, 2, 1, NULL, 0, '需要取消', NOW(), NOW());
```

### 5.5 通知

```sql
-- 学生1的通知
INSERT INTO `notification` (id, user_id, title, content, type, is_read, create_time)
VALUES (1, 1, '报名成功', '您已成功报名竞赛', 1, 0, NOW());

-- 学生2的通知(用于IDOR测试)
INSERT INTO `notification` (id, user_id, title, content, type, is_read, create_time)
VALUES (2, 2, '审核通知', '您的团队已通过审核', 1, 0, NOW());
```

### 5.6 权限配置 (Seed Data)

```sql
-- 权限表 (完整种子数据)
INSERT INTO `permission` (id, code, name) VALUES
(1, 'contest:create',       '创建竞赛'),
(2, 'contest:update',       '更新竞赛'),
(3, 'contest:delete',       '删除竞赛'),
(4, 'contest:publish',      '发布/取消发布竞赛'),
(5, 'user:list',            '用户列表'),
(6, 'user:freeze',          '冻结/解冻用户'),
(7, 'registration:approve', '审核报名'),
(8, 'registration:list',    '报名列表'),
(9, 'registration:cancel',  '取消报名(管理端)'),
(10, 'team:approve',        '审核团队'),
(11, 'team:list',           '团队列表'),
(12, 'notification:send',   '发送通知'),
(13, 'notification:broadcast', '广播通知'),
(14, 'cms:create',          '创建CMS内容'),
(15, 'cms:update',          '更新CMS内容'),
(16, 'cms:delete',          '删除CMS内容'),
(17, 'log:list',            '操作日志'),
(18, 'file:upload',         '文件上传');
-- 注意: file:upload 在代码中定义为常量但@PreAuthorize中实际使用未发现, 仅作为种子

-- 管理员角色权限映射 (role=1 拥有全部权限)
INSERT INTO `role_permission` (role, permission_id)
SELECT 1, id FROM `permission`;

-- 学生角色权限映射 (role=0 默认无管理权限, 仅通过isAuthenticated()访问)
-- role_permission表中无role=0数据, 依赖PermissionService回退策略
```

### 5.7 测试使用的JWT Token

测试用token可以调用登录接口获取, 也可用以下代码预生成:

```java
// 预生成测试token (Postman/单元测试前置)
JwtUtil jwtUtil = new JwtUtil();
String studentToken = jwtUtil.generateToken(1L, "student1", 0);
String adminToken   = jwtUtil.generateToken(3L, "admin1", 1);
String frozenToken  = jwtUtil.generateToken(4L, "frozen1", 0);
```

---

## 附录: 测试注意事项

1. **全局异常处理:** 所有AccessDeniedException → HTTP 403 + `{"code":403,"message":"无权限"}`。测试时注意不要用默认Spring 403页面断言。

2. **SecurityContextHolder 线程安全:** 使用`MODE_INHERITABLETHREADLOCAL`确保异步请求正确传递认证信息。当前项目未显式配置, 默认MODE_THREADLOCAL。

3. **MockMVC 测试与安全上下文:** 单元测试中使用`@WithMockUser`或手动设置SecurityContextHolder; 集成测试使用`@SpringBootTest`+真实过滤器链。

4. **已知 IDOR 漏洞清单 (需修复):**
   - `PUT /api/user/{id}/profile` — 未校验路径id与当前用户一致
   - `PUT /api/user/{id}/password` — 未校验路径id与当前用户一致
   - `GET /api/team/user/{userId}` — 未校验userId与当前用户一致
   - `GET /api/registration/user/{userId}` — 未校验userId与当前用户一致
   - `GET /api/notification/user/{userId}` — 未校验userId与当前用户一致
   - `GET /api/notification/unread/{userId}` — 未校验userId与当前用户一致
   - `PUT /api/notification/read-all/{userId}` — 未校验userId与当前用户一致
   - `POST /api/ai/chat` — 自行解析JWT而非使用SecurityUtil, 存在不一致风险

5. **AiChatController 安全不一致:**
   该控制器在`/api/ai/chat`中自行从请求头解析JWT获取userId, 而非使用`SecurityUtil.getCurrentUserId()`。需统一为SecurityUtil方式, 否则SecurityContextHolder中无认证信息, @PreAuthorize虽能拦截但存在内部userId获取方式不一致的问题。
