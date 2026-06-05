# 高校学科竞赛报名管理系统 — 实验指导书要求检查清单

基于《2526B 企业级应用开发综合实践》指导书，本文件帮助 Claude 检查项目是否满足实验要求。

---

## 一、技术架构要求

| 要求 | 状态 | 说明 |
|------|------|------|
| 前后端分离架构 | ✅ | Vue 3 SPA（:3000）+ Spring Boot（:8080），HTTP/HTTPS 通信 |
| RESTful API 设计 | ✅ | 统一 `/api` 前缀，GET/POST/PUT/DELETE，统一 `Result<T>` 响应体 |
| 分层架构 | ✅ | Controller → Service → DAO/Repository(MyBatis-Plus) |
| 数据库设计 3NF | ✅ | 10 张表，外键+索引+事务保证数据完整性 |
| 版本控制（Git） | ✅ | 完整 commit 历史 |
| 密码 BCrypt 加密 | ✅ | Hutool BCrypt |
| JWT Token 认证 | ✅ | jjwt 0.11.5，7 天过期，密钥可配置 |

**验证方式**：检查 `application.yml` JWT 密钥是否为配置注入形式（非硬编码）；检查 Controller 是否有 JWT 拦截/注解；检查 `@RequestMapping` 是否按 REST 风格命名。

---

## 二、功能模块要求（核心）

### 2.1 用户管理

| 子功能 | 要求 | 实现 | 验证点 |
|--------|------|------|--------|
| 注册/登录 | 学号/邮箱注册，密码加密存储 | ✅ | `UserController.java` — `login`/`register` 接口 |
| 个人信息管理 | 查看/修改资料、上传头像、修改密码 | ✅ | `Profile.vue` + `UserController` PUT `/profile`、PUT `/password` |
| 头像上传 | 支持 | ✅ | MinIO + 本地文件系统双模式 |
| 三种角色 | 学生(0)、管理员(1)、教师(2) | ✅ | `seed.sql` 含三种角色数据，前端导航栏动态展示 |

**检查要点**：
- [x] 注册时密码是否 BCrypt 加密（`UserServiceImpl.register()`）
- [x] 登录时是否校验密码（`login()` 用 `BCrypt.checkPassword`）
- [x] 手机号/邮箱是否有唯一性校验
- [x] 学号(username)是否唯一
- [x] 密码长度是否 8-20 位前后端一致

### 2.2 竞赛管理（后台）

| 子功能 | 要求 | 实现 | 验证点 |
|--------|------|------|--------|
| 竞赛发布 | 填写名称、类别、级别、时间、附件、封面图 | ✅ | `ContestManage.vue` |
| 类型设置 | 个人/团队/两者皆可，团队人数范围 | ✅ | `contestType` 字段 + `teamMinSize`/`teamMaxSize` |
| 状态管理 | 上下架、修改（受报名状态限制） | ✅ | publish/unpublish/delete 接口 + 前置校验 |

**检查要点**：
- [x] `createContest` 是否有时间顺序校验（registerStart < registerEnd < contestTime）
- [x] 下架时是否检查 `currentCount > 0`（有已通过报名不可下架）
- [x] 删除时是否检查只有草稿可删
- [x] 竞赛时间是否不能早于当前时间

### 2.3 竞赛浏览（前台）

| 子功能 | 要求 | 实现 | 验证点 |
|--------|------|------|--------|
| 首页展示 | 轮播图、热门竞赛、最新竞赛 | ✅ | `Home.vue` — Banner + ContestCard |
| 搜索筛选 | 按类别、状态、关键词搜索 | ✅ | `ContestList.vue` — 下拉选择 + 防抖搜索 |
| 详情页 | 竞赛详情、截止时间、已报名人数 | ✅ | `ContestDetail.vue` |
| 状态标签 | 报名中/已截止 | ✅ | `ContestCard.vue` 兜底逻辑（后端状态 + 前端时间判断） |

**检查要点**：
- [x] 首页是否无登录也可访问
- [x] 筛选/搜索是否向后端发请求（非纯前端过滤）
- [x] 草稿竞赛是否对普通用户不可见
- [x] 报名截止时间过后的竞赛是否自动显示"已截止"

### 2.4 竞赛报名

| 子功能 | 要求 | 实现 | 验证点 |
|--------|------|------|--------|
| 个人赛报名 | 一键报名，状态"待审核/已通过/已驳回/已取消" | ✅ | `RegistrationServiceImpl.registerPersonal()` |
| 团队赛创建 | 队长创建团队，生成邀请码 | ✅ | `TeamServiceImpl.createTeam()` — 6 位邀请码 |
| 团队赛加入 | 输入邀请码申请，队长审核 | ✅ | `join()` → `approveMember()`/`rejectMember()` |
| 团队报名 | 人数达标后队长提交 | ✅ | `submitForReview()` → `registerTeam()` |

**检查要点**：
- [x] 重复报名同一竞赛是否被拦截
- [x] 每人同时最多 3 个非取消报名是否校验
- [x] 团队报名是否必须队长操作
- [x] 邀请码是否 6 位唯一
- [x] 审批时是否有状态前置校验（防止重复审批）
- [x] `currentCount` 在审批通过/驳回时是否正确增减
- [x] 驳回团队时是否同步取消关联报名

---

## 三、扩展功能

| 功能 | 要求 | 实现 | 说明 |
|------|------|------|------|
| 智能竞赛咨询机器人 | 扩展 | ✅ | AI 助手（DashScope glm-5.1），SSE 流式对话，7 个工具调用 |
| 消息通知系统 | 扩展 | ✅ | 审核通知、入队通知、系统公告、未读角标 |
| 竞赛作品在线提交 | 扩展 | ❌ | **未实现** — 需扩展 `registration` 表增加作品字段 + 上传功能 |
| 数据可视化大屏 | 扩展 | ✅ | ECharts 6 图表（类别/级别/报名趋势/状态/用户增长/热门竞赛）+ 4 统计卡片 |

**检查要点**：
- [ ] AI 助手是否真实调用 API（非 mock），是否 SSE 流式输出
- [ ] 通知点击是否做跳转（当前 `relatedType` 处理：`contest`→详情页, `team`→团队详情；`registration` 类型未处理，seed 数据中如有则点击无效）

---

## 四、教师角色

| 功能 | 实现 | 说明 |
|------|------|------|
| 指导教师选择 | ✅ | 创建团队、团队详情页可设置指导教师 |
| 教师查看指导团队 | ✅ | `/teacher/teams` 路由 + `TeacherTeams.vue` |
| 教师审批权限 | ✅ | 权限管理可配 `team:approve`、`registration:approve` |

---

## 五、安全要求

| 要求 | 状态 | 验证点 |
|------|------|--------|
| XSS 防护 | ✅ | `sanitizeHtml()` 过滤富文本中的 `<script>`/`<iframe>`/事件处理器 |
| SQL 注入防护 | ✅ | MyBatis-Plus 参数化查询 |
| 数据权限校验 | ✅ | `/api/registration/user/{userId}` 校验归属 |
| 角色越权防护 | ✅ | 路由 `meta.role` + 后端注解/Security 拦截 |
| 管理员创建用户角色校验 | ✅ | 非管理员不能创建管理员账号 |

---

## 六、测试要求

| 层级 | 用例数 | 通过率 |
|------|--------|--------|
| 后端单元测试 | 84（3 个 Test 类） | 100% |
| 前端测试 | 30（4 个 spec 文件） | 100% |

**验证方式**：
- 后端：`mvn test -pl contest-team -Dtest="TeamServiceImplTest,TeamControllerTest"` 和 `mvn test -pl contest-register -Dtest="RegistrationServiceImplTest"`
- 前端：`cd contest-frontend && npm test`

---

## 七、已知缺陷 / 待修复项

| 缺陷 | 模块 | 状态 |
|------|------|------|
| 通知 `relatedType === 'registration'` 点击不跳转 | 前端 NotificationList.vue:136-139 | ✅ 已修复（跳转到 `/my-registration`） |
| 作品在线提交 | 全系统 | 未实现（扩展功能） |
| 数据可视化大屏 | 全系统 | ✅ 已实现（ECharts + 7 个聚合 API + Dashboard.vue） |
| README 中团队审核(`/admin/team-review`)、操作日志(`/admin/log`)入口不存在 | 文档 | ✅ 已修正（更新为实际可用路径） |

---

## 八、提交物检查

| 要求 | 状态 |
|------|------|
| 可编译运行源码 | ✅ `mvn compile` + `vite build` 通过 |
| 汇总报告 | ❓ 需要检查是否按模板编制 |
| 系统演示录屏 | ❓ 需要确认 |
| Git 提交记录 | ✅ 完整历史（不允许抄袭/代写检查） |
| 代码重复率合规 | ❓ 需工具检查 |

---

---

## 九、前端 AI 会话管理（待实现）

| 模块 | 说明 | 已存在？ | 注意点 |
|------|------|---------|--------|
| **后端接口：会话列表** `GET /api/ai/conversations` | 返回当前用户所有会话 `id, title, updateTime`，按时间倒序 | ❌ 需新增 | 只返回元数据，不返回消息体；校验 userId 归属 |
| **后端接口：删除会话** `DELETE /api/ai/conversations/{id}` | 删除会话及其所有消息 | ❌ 需新增 | 校验归属；手动删除关联 `ai_message` |
| **已有接口复用** `POST /api/ai/chat` | 传入 `conversationId=null` 自动创建新会话，传入已有 id 继续对话 | ✅ 已实现 | 前端无需改动 |
| **前端：会话侧边栏** | 浮动面板左侧展开，显示会话标题列表 + 新建按钮 | ❌ 需新增 | 小屏可折叠；保持 `AiBubble.vue` 原有的右下角气泡入口 |
| **前端：切换会话** | 点击会话条目加载历史消息 | ❌ 需新增 | 切换时清空消息列表后重新请求后端加载；避免内存泄漏 |
| **前端：删除会话** | 删除按钮 + 二次确认 | ❌ 需新增 | 先调用后端删除接口，再从列表中移除 |
| **前端：新建会话** | 新建按钮 → 创建空会话 → 用户可立即发消息 | ❌ 需新增 | 传 `conversationId=null` 给后端即可 |

**边界约束**：
- 会话标题由后端 `updateTitle()` 自动提取用户首条提问生成，前端不做任何标题处理
- 不跨会话共享上下文（每次对话独立）
- 不限制会话数量（课程设计无需分页，但列表不宜超过 50 条）
- `AiBubble.vue` 保持现有入口不动，只在展开的面板内部增加侧边栏

---

> 本文件由 Claude 读取，用于检查实验指导书要求是否完成。每次评估时请对上述检查列表逐项核实，并报告通过/不通过及原因。
