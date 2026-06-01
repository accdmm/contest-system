# 阶段五：前端修复测试报告

## 修复内容

### Module 3：PermissionManage 权限管理页面

| 问题 | 修复 |
|------|------|
| 按角色分配缺少教师(role=2) | 添加 `<el-radio-button :value="2">教师</el-radio-button>` |
| 角色标签文字硬编码 1/0 | 改用 `roleLabel` computed: {0:学生, 1:管理员, 2:教师} |
| 空 catch 块静默失败 | 添加 ElMessage.error 提示 |

**文件**: `PermissionManage.vue:10-12, 16, 76, 102, 119, 149`

### Module 4：TeamDetail 指导教师显示与更换

| 问题 | 修复 |
|------|------|
| TeamDetail 不显示教师信息 | 在 team-meta 区域添加"指导教师：{{ teacherName }}"显示 |
| `setTeamTeacher` API 在前端未使用 | 导入 `setTeamTeacher` 和 `listTeachers`，添加更换对话框 |
| 无法更换已创建团队的教师 | 队长可点击"更换"按钮选择新教师 |

**文件**: `TeamDetail.vue:34-57, 272-321, 352-369`

### Module 5：其他 Bug

| 问题 | 文件 | 修复 |
|------|------|------|
| Profile.vue 重复 onMounted | `Profile.vue:130-136` | 删除第二个重复的 onMounted |
| CmsManage 调用 `listAnnouncements()` 缺少 position | `cms.js:7-9` | 函数仅在有值时发送 position 参数 |
| ContestManage 始终发送 `maxParticipants: 0` | `ContestManage.vue:174` | 从 initialForm 移除该字段 |

## 测试结果（2026-06-01）

| # | 测试用例 | 预期 | 结果 |
|---|---------|------|------|
| 1 | GET /api/user/detail/1 with auth | 200 + user data | ✅ |
| 2 | GET /api/user/detail/1 without auth | 403 | ✅ |
| 3 | PUT /user/2/profile as user 1 | 400 "无权修改" | ✅ |
| 4 | PUT /user/2/password as user 1 | 400 "无权修改" | ✅ |
| 5 | GET /notification/user/1 with auth | 200 | ✅ |
| 6 | GET /notification/user/2 as user 1 | 400 "无权查看" | ✅ |
| 7 | GET /api/user/colleges without auth | 200 | ✅ |
| 8 | PermissionManage 页面教师角色按钮 | 管理员/学生/教师 三选 | ✅ |
| 9 | TeamDetail 显示指导教师+更换按钮 | 指导教师信息可见，队长可更换 | ✅ |
| 10 | 前端构建无错误 | `vite build` 成功 | ✅ |

## 涉及文件变更

### 后端
- `contest-system/contest-user/.../UserController.java` — 3 处安全修复
- `contest-system/contest-message/.../NotificationController.java` — 3 处所有权校验

### 前端
- `contest-frontend/src/api/user.js` — getUserById 端点改为 `/user/detail/${id}`
- `contest-frontend/src/api/cms.js` — listAnnouncements 条件传参
- `contest-frontend/src/views/admin/PermissionManage.vue` — 教师角色 + catch 块
- `contest-frontend/src/views/team/TeamDetail.vue` — 教师显示与更换 + setTeamTeacher 调用
- `contest-frontend/src/views/user/Profile.vue` — 删除重复 onMounted
- `contest-frontend/src/views/admin/ContestManage.vue` — 移除 maxParticipants
