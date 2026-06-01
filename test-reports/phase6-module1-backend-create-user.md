# 阶段六 Module 1：管理员创建用户 API 测试报告

## 新增

| 文件 | 类型 | 说明 |
|------|------|------|
| `AdminCreateUserRequest.java` | DTO | username/password/name/email/phone/collegeId/majorId/role + Jakarta 校验 |
| `UserService.adminCreateUser()` | 接口方法 | 新建 |
| `UserServiceImpl.adminCreateUser()` | 实现 | 密码校验、用户名唯一、college/major 查名、角色合法性验证 |
| `POST /api/user/admin/create` | Controller | `@PreAuthorize("hasAuthority('user:create')")` |
| `permission` 表 ID 20 | 权限数据 | `user:create` → 创建用户 |
| `role_permission` 表 | 权限绑定 | role=1 (管理员) → permission_id=20 |

## 测试结果

| # | 用例 | 请求 | 预期 | 结果 |
|---|------|------|------|------|
| 1 | 管理员创建教师 | role=2 | 200，返回用户 role=2 | ✅ |
| 2 | 重复用户名 | 同 username | 400 "用户名已存在" | ✅ |
| 3 | 无效角色值 | role=99 | 400 "无效的角色值" | ✅ |
| 4 | 未认证访问 | 无 token | 403 Forbidden | ✅ |
