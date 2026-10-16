@echo off
setlocal enabledelayedexpansion

REM ============================================================================
REM 1-SETUP-DATABASE.bat - Non-Technical 1-Click Database Setup Wizard
REM Student Course Registration Portal (Demo Environment)
REM ============================================================================

set "KIT_DIR=%~dp0"
title Course Portal - Database Setup Wizard

echo ============================================================================
echo   ONE-TIME DATABASE SETUP WIZARD
echo   Student Course Registration Portal (Demo Environment)
echo ============================================================================
echo.
echo This wizard will automatically set up the demo database for your presentation.
echo You only need to run this ONCE.
echo.

REM ----------------------------------------------------------------------------
REM 1. Locate MySQL Client (mysql.exe)
REM ----------------------------------------------------------------------------
echo [1/3] Locating MySQL Server on this computer...
set "MYSQL_EXE="

where mysql.exe > nul 2>&1
if %ERRORLEVEL% equ 0 set "MYSQL_EXE=mysql.exe"

if "%MYSQL_EXE%"=="" if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
if "%MYSQL_EXE%"=="" if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if "%MYSQL_EXE%"=="" if exist "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe" set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe"
if "%MYSQL_EXE%"=="" if exist "C:\Program Files\MySQL\MySQL Server 8.2\bin\mysql.exe" set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.2\bin\mysql.exe"
if "%MYSQL_EXE%"=="" if exist "C:\xampp\mysql\bin\mysql.exe" set "MYSQL_EXE=C:\xampp\mysql\bin\mysql.exe"

if "%MYSQL_EXE%"=="" goto :MYSQL_NOT_FOUND

echo   [OK] Found MySQL at: "%MYSQL_EXE%"
echo.
goto :PROMPT_PASS

:MYSQL_NOT_FOUND
echo.
echo ============================================================================
echo   [!] MySQL Server was not detected on this computer.
echo ============================================================================
echo.
echo To run the portal, MySQL Server 8.0 or 8.4 must be installed.
echo.
echo Quick steps to install:
echo   1. Download the free MySQL Community Installer from:
echo      https://dev.mysql.com/downloads/installer/
echo   2. Run installer, choose default settings, and set root password to: root
echo   3. Once finished, double-click this file again!
echo.
set /p OPEN_WEB="Would you like to open the MySQL download page now? (Y/N): "
if /i "%OPEN_WEB%"=="Y" start https://dev.mysql.com/downloads/installer/
echo.
pause
exit /b 1

REM ----------------------------------------------------------------------------
REM 2. Prompt for MySQL root password
REM ----------------------------------------------------------------------------
:PROMPT_PASS
echo [2/3] MySQL Administrator Connection:
echo Please enter your MySQL 'root' password.
echo (If you just installed MySQL with password 'root', or have no password, press Enter)
set "ROOT_PASS="
set /p ROOT_PASS="MySQL root password: "

REM Test connection
echo Testing connection...
if "%ROOT_PASS%"=="" (
    "%MYSQL_EXE%" -u root -e "SELECT 1;" > nul 2>&1
    set "PASS_ARG="
) else (
    "%MYSQL_EXE%" -u root --password="%ROOT_PASS%" -e "SELECT 1;" > nul 2>&1
    set "PASS_ARG=--password=%ROOT_PASS%"
)

if %ERRORLEVEL% equ 0 goto :CONNECT_OK

echo.
echo   [X] Could not connect to MySQL with the entered password.
echo.
set /p RETRY="Would you like to try typing the password again? (Y/N): "
if /i "%RETRY%"=="Y" goto :PROMPT_PASS
echo Setup cancelled. Please check your MySQL service and password.
pause
exit /b 1

:CONNECT_OK
echo   [OK] Connection to MySQL Server verified successfully!
echo.

REM ----------------------------------------------------------------------------
REM 3. Execute Isolated Initialization & Seed Scripts
REM ----------------------------------------------------------------------------
echo [3/3] Setting up demo database and seeding R22 curriculum catalog...

echo   - Creating isolated database 'student_course_portal_demo'...
if "%PASS_ARG%"=="" (
    "%MYSQL_EXE%" -u root < "%KIT_DIR%database\01_init_demo_database.sql" > nul 2>&1
) else (
    "%MYSQL_EXE%" -u root %PASS_ARG% < "%KIT_DIR%database\01_init_demo_database.sql" > nul 2>&1
)

if %ERRORLEVEL% neq 0 (
    echo   [ERROR] Failed to execute database\01_init_demo_database.sql
    pause
    exit /b 1
)

echo   - Populating 8 application tables, demo accounts, and R22 catalog...
"%MYSQL_EXE%" -u portal_demo --password=DemoPortal#2026 student_course_portal_demo < "%KIT_DIR%database\02_seed_demo_data.sql" > nul 2>&1
if %ERRORLEVEL% neq 0 (
    if "%PASS_ARG%"=="" (
        "%MYSQL_EXE%" -u root student_course_portal_demo < "%KIT_DIR%database\02_seed_demo_data.sql" > nul 2>&1
    ) else (
        "%MYSQL_EXE%" -u root %PASS_ARG% student_course_portal_demo < "%KIT_DIR%database\02_seed_demo_data.sql" > nul 2>&1
    )
)

if %ERRORLEVEL% neq 0 (
    echo   [ERROR] Failed to seed demo tables.
    pause
    exit /b 1
)

echo.
echo ============================================================================
echo   [SUCCESS] DEMO DATABASE SETUP COMPLETE!
echo ============================================================================
echo.
echo   - Isolated Database:   student_course_portal_demo
echo   - Dedicated User:      portal_demo
echo   - Demo Accounts:       4 Students + 1 Admin (Seeded)
echo   - R22 Catalog:         92 Courses + 41 Electives (Seeded)
echo   - Registration Window: All 8 Semesters CLOSED (Initial Safe State)
echo.
echo   NEXT STEP:
echo   To launch your presentation, simply double-click:
echo       2-START-PORTAL.bat
echo ============================================================================
echo.
pause
