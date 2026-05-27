# Agent Workflow Guide

## 修改代码后自动测试并记录

每次修改代码后，子 Agent 必须调用 `test-and-report.sh` 运行相关测试，
测试结果会自动追加到 `测试报告.md` 的「测试执行记录」章节。

### 命令格式

```bash
bash test-and-report.sh <module> "<变更说明>"
```

### 可用模块

| 模块 | 测试范围 | 典型场景 |
|------|---------|---------|
| `team-backend` | TeamServiceImplTest + TeamControllerTest | 修改 team 模块 |
| `register-backend` | RegistrationServiceImplTest | 修改 registration 模块 |
| `frontend` | 全部 4 个前端测试文件 | 修改前端 |
| `team` | team-backend + frontend | 同时修改 team 前后端 |
| `all` | 所有后端 + 前端测试 | 全面回归 |

### 示例

```bash
# 修改 team 后端后
bash test-and-report.sh team-backend "修复 approveMember 空指针异常"

# 修改前端后
bash test-and-report.sh frontend "更新 ContestDetail 依赖项"

# 全面回归
bash test-and-report.sh all "版本发布前回归验证"
```

### 变更说明规范

- 使用中文
- 简要描述修改内容（20 字以内）
- 聚焦「做了什么」，而非「为什么做」
