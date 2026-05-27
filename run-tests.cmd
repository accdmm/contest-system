@echo off
REM Test runner for contest project (Windows)
REM Usage: run-tests [module]
REM Modules: team, frontend, all

if "%1"=="" (
  set MODULE=all
) else (
  set MODULE=%1
)

if "%MODULE%"=="team-backend" (
  cd contest-system
  call mvn test -pl contest-team -am -Dtest=TeamServiceImplTest,TeamControllerTest -Dsurefire.failIfNoSpecifiedTests=false -q
  goto :eof
)

if "%MODULE%"=="register-backend" (
  cd contest-system
  call mvn test -pl contest-register -Dtest=RegistrationServiceImplTest -q
  goto :eof
)

if "%MODULE%"=="frontend" (
  cd contest-frontend
  call npx vitest run --reporter=verbose
  goto :eof
)

if "%MODULE%"=="all" (
  cd contest-system
  call mvn test -pl contest-team -am -Dtest=TeamServiceImplTest,TeamControllerTest -Dsurefire.failIfNoSpecifiedTests=false -q
  call mvn test -pl contest-register -Dtest=RegistrationServiceImplTest -q
  cd ../contest-frontend
  call npx vitest run --reporter=verbose
  goto :eof
)

echo Usage: run-tests [team-backend^|register-backend^|frontend^|all]
