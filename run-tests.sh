#!/bin/bash
# Test runner for contest project
#
# 子 Agent: 请使用 test-and-report.sh <module> "<变更说明>" 替代此脚本，
#           测试结果会自动追加到 测试报告.md。
#
# Usage: ./run-tests.sh [module]
# Modules: team-backend, register-backend, frontend, team, all

set -e

case "${1:-all}" in
  team-backend)
    cd contest-system
    mvn test -pl contest-team -am -Dtest="${2:-TeamServiceImplTest,TeamControllerTest}" -Dsurefire.failIfNoSpecifiedTests=false -q
    ;;
  register-backend)
    cd contest-system
    mvn test -pl contest-register -Dtest="${2:-RegistrationServiceImplTest}" -q
    ;;
  team)
    cd contest-system
    mvn test -pl contest-team -am -Dtest="${2:-TeamServiceImplTest,TeamControllerTest}" -Dsurefire.failIfNoSpecifiedTests=false -q
    cd ../contest-frontend
    npx vitest run --reporter=verbose
    ;;
  frontend)
    cd contest-frontend
    npx vitest run --reporter=verbose
    ;;
  all)
    cd contest-system
    mvn test -pl contest-team -am -Dtest=TeamServiceImplTest,TeamControllerTest -Dsurefire.failIfNoSpecifiedTests=false -q
    mvn test -pl contest-register -Dtest=RegistrationServiceImplTest -q
    cd ../contest-frontend
    npx vitest run --reporter=verbose
    ;;
  *)
    echo "Usage: ./run-tests.sh [team-backend|register-backend|team|frontend|all]"
    exit 1
    ;;
esac
