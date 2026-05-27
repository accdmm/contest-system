#!/bin/bash
# ============================================================
# test-and-report.sh — 运行测试并自动追加结果到测试报告
#
# 用法:
#   ./test-and-report.sh <module> "<变更说明>"
#
# 模块:
#   team-backend    — contest-team 全部后端测试
#   register-backend — contest-register 全部后端测试
#   frontend        — contest-frontend 全部前端测试
#   team            — contest-team（后端+前端）
#   all             — 全部测试
#
# 示例:
#   ./test-and-report.sh team-backend "修复 approveMember 空指针"
#   ./test-and-report.sh frontend "更新 ContestDetail 测试用例"
#
# 子 Agent 在每次修改代码后调用此脚本，
# 测试结果会自动追加到 测试报告.md 的「测试执行记录」章节。
# ============================================================

REPORT_FILE="测试报告.md"
TIMESTAMP=$(date "+%Y-%m-%d %H:%M:%S")

if [ $# -lt 1 ]; then
  echo "用法: $0 <module> [变更说明]"
  echo "模块: team-backend, register-backend, frontend, team, all"
  exit 1
fi

MODULE="$1"
shift
CHANGE_DESC="${*:-无}"

# ---- 临时文件 ----
TEMP_OUT=$(mktemp)
TEMP_ERR=$(mktemp)
trap 'rm -f "$TEMP_OUT" "$TEMP_ERR"' EXIT

# ---- 执行测试 ----
echo ""
echo "============================================"
echo "  Running tests: $MODULE"
echo "  Change: $CHANGE_DESC"
echo "  Started: $TIMESTAMP"
echo "============================================"

set +e  # allow test failures

case "$MODULE" in
  team-backend)
    cd contest-system
    mvn test -pl contest-team -am -Dtest=TeamServiceImplTest,TeamControllerTest \
      -Dsurefire.failIfNoSpecifiedTests=false 2>"$TEMP_ERR" | tee "$TEMP_OUT"
    EXIT_CODE=${PIPESTATUS[0]}
    cd ..
    ;;
  register-backend)
    cd contest-system
    mvn test -pl contest-register -Dtest=RegistrationServiceImplTest \
      2>"$TEMP_ERR" | tee "$TEMP_OUT"
    EXIT_CODE=${PIPESTATUS[0]}
    cd ..
    ;;
  frontend)
    cd contest-frontend
    npx vitest run --reporter=verbose 2>"$TEMP_ERR" | tee "$TEMP_OUT"
    EXIT_CODE=${PIPESTATUS[0]}
    cd ..
    ;;
  team)
    cd contest-system
    mvn test -pl contest-team -am -Dtest=TeamServiceImplTest,TeamControllerTest \
      -Dsurefire.failIfNoSpecifiedTests=false 2>"$TEMP_ERR" | tee "$TEMP_OUT"
    EXIT_CODE=${PIPESTATUS[0]}
    cd ..
    echo "" >> "$TEMP_OUT"
    cd contest-frontend
    npx vitest run --reporter=verbose 2>"$TEMP_ERR" | tee -a "$TEMP_OUT"
    EXIT_CODE=$((EXIT_CODE + ${PIPESTATUS[0]}))
    cd ..
    ;;
  all)
    cd contest-system
    mvn test -pl contest-team -am -Dtest=TeamServiceImplTest,TeamControllerTest \
      -Dsurefire.failIfNoSpecifiedTests=false 2>"$TEMP_ERR" | tee "$TEMP_OUT"
    EXIT_CODE=${PIPESTATUS[0]}
    mvn test -pl contest-register -Dtest=RegistrationServiceImplTest \
      2>"$TEMP_ERR" | tee -a "$TEMP_OUT"
    EXIT_CODE=$((EXIT_CODE + ${PIPESTATUS[0]}))
    cd ..
    echo "" >> "$TEMP_OUT"
    cd contest-frontend
    npx vitest run --reporter=verbose 2>"$TEMP_ERR" | tee -a "$TEMP_OUT"
    EXIT_CODE=$((EXIT_CODE + ${PIPESTATUS[0]}))
    cd ..
    ;;
  *)
    echo "未知模块: $MODULE"
    echo "可用模块: team-backend, register-backend, frontend, team, all"
    exit 1
    ;;
esac

set -e

# ---- 解析测试结果 ----
FULL_OUTPUT=$(cat "$TEMP_OUT")
ERR_OUTPUT=$(cat "$TEMP_ERR")

# 提取数字工具: 从 "Key: Value" 行提取 Value
extract_num() {
  local prefix="$1"
  local line
  line=$(echo "$FULL_OUTPUT" | grep "Tests run:" | grep -oE "${prefix}: [0-9]+" | head -1)
  if [ -n "$line" ]; then
    echo "$line" | sed "s/${prefix}: //"
  else
    echo "0"
  fi
}

# 提取后端测试统计 (Maven Surefire: "Tests run: N, Failures: F, ...")
# 把所有 Tests run 行的数字加起来
# 只统计单个测试类的行（包含 " -- in "），忽略模块汇总行
sum_after() {
  local label="$1"
  echo "$FULL_OUTPUT" | grep " -- in " | grep -oE "${label}: [0-9]+" | sed "s/${label}: //" | awk '{s+=$1} END {print s}'
}

BACKEND_TOTAL=$(sum_after "Tests run")
BACKEND_FAIL=$(sum_after "Failures")
BACKEND_ERR=$(sum_after "Errors")
BACKEND_SKIP=$(sum_after "Skipped")

# 提取前端测试统计
# Vitest: "Tests       N passed | M failed (K ms)"
# Vitest: "Test Files  X passed | Y failed (Z ms)"
FRONTEND_TOTAL=0
FRONTEND_PASS=0
FRONTEND_FAIL=0
FRONTEND_PASS_COUNT=0
FRONTEND_FAIL_COUNT=0

FRONTEND_LINE=$(echo "$FULL_OUTPUT" | grep -E 'Tests[[:space:]]+[0-9]+ passed' | tail -1)
FRONTEND_FILES_LINE=$(echo "$FULL_OUTPUT" | grep -E 'Test Files[[:space:]]+[0-9]+ passed' | tail -1)

if [ -n "$FRONTEND_LINE" ]; then
  # "      Tests  30 passed (30)"  -> extract 30 from right before "passed"
  FRONTEND_PASS_COUNT=$(echo "$FRONTEND_LINE" | grep -oE '[0-9]+ passed' | grep -oE '[0-9]+' | head -1)
  FRONTEND_FAIL_COUNT=$(echo "$FRONTEND_LINE" | grep -oE '[0-9]+ failed' | grep -oE '[0-9]+' | head -1)
  FRONTEND_PASS_COUNT=${FRONTEND_PASS_COUNT:-0}
  FRONTEND_FAIL_COUNT=${FRONTEND_FAIL_COUNT:-0}
  FRONTEND_TOTAL=$((FRONTEND_PASS_COUNT + FRONTEND_FAIL_COUNT))
  FRONTEND_PASS=$FRONTEND_PASS_COUNT
  FRONTEND_FAIL=$FRONTEND_FAIL_COUNT
fi

# 确定整体结果
HAS_FAILURE=0
echo "$FULL_OUTPUT" | grep -q "BUILD FAILURE" && HAS_FAILURE=1
echo "$FULL_OUTPUT" | grep -qE "Tests.*  [0-9]+ failed" && HAS_FAILURE=1
[ "$BACKEND_FAIL" -gt 0 ] && HAS_FAILURE=1
[ "$BACKEND_ERR" -gt 0 ] && HAS_FAILURE=1
[ "$FRONTEND_FAIL" -gt 0 ] && HAS_FAILURE=1

if [ "$HAS_FAILURE" -gt 0 ]; then
  RESULT_ICON="❌"
  RESULT_TEXT="失败"
else
  RESULT_ICON="✅"
  RESULT_TEXT="通过"
fi

# ---- 提取失败详情 ----
FAILURE_DETAILS=""
if [ "$HAS_FAILURE" -gt 0 ]; then
  FAILURE_DETAILS=$(echo "$FULL_OUTPUT" | grep -A 3 "FAIL\|ERROR" | grep -v "^--$" | head -20)
fi

# ---- 格式化统计摘要 ----
STATS=""
if [ -n "${BACKEND_TOTAL:-}" ] && [ "$BACKEND_TOTAL" -gt 0 ]; then
  STATS="后端: ${BACKEND_TOTAL} 用例, 失败=${BACKEND_FAIL}, 错误=${BACKEND_ERR}, 跳过=${BACKEND_SKIP}"
fi
if [ "$FRONTEND_TOTAL" -gt 0 ]; then
  [ -n "$STATS" ] && STATS="${STATS} |"
  STATS="${STATS} 前端: ${FRONTEND_TOTAL} 用例, 通过=${FRONTEND_PASS}, 失败=${FRONTEND_FAIL}"
fi
if [ -z "$STATS" ]; then
  TOTAL=$(echo "$FULL_OUTPUT" | grep -cE '(Tests run:|✓|×)' || true)
  STATS="总计约 ${TOTAL} 条断言"
fi

# ---- 追加到测试报告 ----
ENTRY=""
ENTRY="${ENTRY}### ${TIMESTAMP} — ${RESULT_ICON} ${MODULE}\n\n"
ENTRY="${ENTRY}| 项目 | 内容 |\n"
ENTRY="${ENTRY}|------|------|\n"
ENTRY="${ENTRY}| 变更说明 | ${CHANGE_DESC} |\n"
ENTRY="${ENTRY}| 执行结果 | ${RESULT_TEXT} |\n"
ENTRY="${ENTRY}| 统计摘要 | ${STATS} |\n"
ENTRY="${ENTRY}| 退出码 | ${EXIT_CODE} |\n"

# 后端详情 (Maven surefire lines)
BACKEND_LINES=$(echo "$FULL_OUTPUT" | grep -E 'Tests run:.*in --' || true)
if [ -n "$BACKEND_LINES" ]; then
  ENTRY="${ENTRY}| 后端详情 | |\n"
  while IFS= read -r line; do
    ENTRY="${ENTRY}| | \`${line}\` |\n"
  done <<< "$BACKEND_LINES"
fi

# 前端详情
if [ -n "$FRONTEND_FILES_LINE" ]; then
  ENTRY="${ENTRY}| 前端详情 | \`${FRONTEND_FILES_LINE}\` |\n"
fi
if [ -n "$FRONTEND_LINE" ]; then
  ENTRY="${ENTRY}| | \`${FRONTEND_LINE}\` |\n"
fi

if [ "$HAS_FAILURE" -gt 0 ] && [ -n "$FAILURE_DETAILS" ]; then
  ENTRY="${ENTRY}| 失败详情 | |\n"
  while IFS= read -r line; do
    SAFE_LINE=$(echo "$line" | sed 's/|/\//g')
    ENTRY="${ENTRY}| | \`${SAFE_LINE}\` |\n"
  done <<< "$FAILURE_DETAILS"
fi

ENTRY="${ENTRY}\n"

# ---- 写入报告 ----
if grep -q "<!-- TEST_LOG_ANCHOR -->" "$REPORT_FILE"; then
  sed -i "/<!-- TEST_LOG_ANCHOR -->/i\\$ENTRY" "$REPORT_FILE"
  echo "✅ 结果已追加到 $REPORT_FILE"
else
  echo -e "\n$ENTRY" >> "$REPORT_FILE"
  echo "⚠️ 未找到锚点标记，已追加到文件末尾"
fi

# ---- 打印摘要 ----
echo ""
echo "============================================"
echo "  Result: ${RESULT_ICON} ${RESULT_TEXT}"
echo "  ${STATS}"
echo "  Logged to: ${REPORT_FILE}"
echo "============================================"

exit $EXIT_CODE
