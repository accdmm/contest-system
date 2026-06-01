# Phase 6 Module 4: Frontend Admin Create User — Test Report

## Objective
Verify the "新建用户" (Create User) feature in `UserManage.vue` works correctly.

## Changes Made
- **`UserManage.vue`**: Added `isCreate` ref, `openCreate()` function, branched `handleSave()` to call `adminCreateUser()` (create) or `updateProfile()` (edit), added `.um-add-btn` CSS
- **`api/user.js`**: Already had `adminCreateUser()` from M1

## Test Cases (manual)

### TC1: Open create dialog
1. Admin navigates to User Management page
2. Clicks "新建用户" button
3. Dialog opens with title "新建用户"
4. Password field is visible and pre-filled with "123456"
5. Username field is editable
6. Role dropdown shows 学生/教师/管理员

### TC2: Create a new student
1. Fill: username=teststu01, password=123456, name=TestStudent, role=学生, select college/major
2. Click "创建"
3. Success message "创建成功" shown
4. User appears in the table

### TC3: Create a new teacher
1. Fill: username=testtch01, password=123456, name=TestTeacher, role=教师
2. Click "创建"
3. Success

### TC4: Create a new admin
1. Fill: username=testadm01, password=123456, name=TestAdmin, role=管理员
2. Click "创建"
3. Success

### TC5: Duplicate username
1. Try creating with an existing username
2. Error message shown ("用户名已存在")

### TC6: Edit mode still works
1. Click "编辑" on any existing user
2. Dialog opens with title "编辑用户"
3. Password field is hidden
4. Username field is disabled
5. Save updates successfully

## Result
| TC | Status |
|----|--------|
| TC1 | ✅ |
| TC2 | ✅ |
| TC3 | ✅ |
| TC4 | ✅ |
| TC5 | ✅ |
| TC6 | ✅ |

## Bugs Found
- **BUG-UM-01**: 教师角色在表格中显示为"学生" — UserManage.vue 三元表达式未处理 role=2 ✅ 已修复
- **BUG-UM-05**: 连续编辑两个用户时专业 select 不回显 — watch 异步清空 majorId ✅ 已修复
