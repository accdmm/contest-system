# Phase 3 测试报告：指导老师角色

## 测试日期
2026-05-31

## 测试环境
- 后端：Spring Boot + MyBatis-Plus + Spring Security
- 前端：Vue 3 + Element Plus
- 数据库：MySQL (contest_system)

## 测试内容

### 1. ROLE_TEACHER 角色定义

| 变更 | 位置 |
|------|------|
| 新增 `ROLE_TEACHER = 2` | `CommonConstants.java:10` |
| 更新 user 表 role 字段注释 | `init.sql:33` |
| 更新 role_permission 表 role 字段注释 | `init.sql:206` |
| JWT 角色名映射 | `JwtAuthFilter.java:56` |

### 2. 数据表变更

- **team 表**：新增 `teacher_id BIGINT UNSIGNED DEFAULT NULL` 列
- **seed 数据**：
  - 2 个教师用户：王教授 (id=13, 计算机学院)、刘教授 (id=14, 数学学院)
  - 给 role=2 分配权限：`team:list`, `registration:list`, `notification:send`, `file:upload`
  - 部分团队预分配指导教师

### 3. 后端 API

| 端点 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/api/user/teachers` | GET | 需认证 | 获取所有教师列表 |
| `/api/team/{teamId}/teacher` | PUT | 需认证(队长) | 设置/更换指导教师 |
| `/api/team/teacher` | GET | 需认证 | 教师获取自己指导的团队 |

### 4. 前端变更

| 文件 | 变更 |
|------|------|
| `stores/user.js` | 新增 `isTeacher` computed |
| `api/user.js` | 新增 `listTeachers()` |
| `api/team.js` | 新增 `setTeamTeacher()`, `getTeacherTeams()` |
| `CreateTeam.vue` | 新增指导教师下拉选择框 |
| `TeamDetail.vue` | 显示指导教师信息，队长可选择设置教师 |
| `TeacherTeams.vue` | 新建页面：教师查看自己指导的团队 |
| `NavBar.vue` | 教师下拉菜单 + 移动端侧栏新增"我指导的团队"链接 |
| `router/index.js` | 新增 `/teacher/teams` 路由 (role=2) |

### 5. 关键业务逻辑

- 创建团队时可选填写指导教师
- 队长可随时更换指导教师
- 指导教师可见自己的团队列表
- `setTeacher` 校验：仅队长可设置、教师 role 必须为 2

### 6. 后端测试结果

```
Tests run: 62, Failures: 0, Errors: 0, Skipped: 0
```

所有 62 个单元测试通过，包括：
- TeamServiceImplTest: createTeam (含 teacherId), generateInvite, 成员管理
- TeamControllerTest: 所有 REST 端点 (create, invite, join, approve/reject, dissolve, leave, submit, admin, userTeams, page, teacher)

### 7. 注意事项

- 编译通过需注意 ChatTools.java 中的 `createTeam` 调用改为三参数
- 教师账户需由管理员创建（无法自行注册）
- 教师默认权限：查看团队列表、查看报名、发送通知、上传文件
