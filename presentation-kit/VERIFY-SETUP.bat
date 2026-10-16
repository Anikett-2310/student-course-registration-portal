@echo off
setlocal enabledelayedexpansion

REM ============================================================================
REM VERIFY-SETUP.bat - Presentation Kit Diagnostic & Prerequisite Checker
REM ============================================================================

set "KIT_DIR=%~dp0"
set "LOG_FILE=%KIT_DIR%logs\verify-setup.log"

if not exist "%KIT_DIR%logs" mkdir "%KIT_DIR%logs"

echo ============================================================================ > "%LOG_FILE%"
echo PRESENTATION KIT SETUP VERIFICATION LOG - %DATE% %TIME% >> "%LOG_FILE%"
echo ============================================================================ >> "%LOG_FILE%"

echo ============================================================================
echo   STUDENT COURSE REGISTRATION PORTAL - SETUP VERIFICATION
echo ============================================================================
echo.

set "ERRORS=0"
set "WARNINGS=0"

REM ----------------------------------------------------------------------------
REM 1. Check Configuration File
REM ----------------------------------------------------------------------------
echo [1/6] Checking configuration files...
if exist "%KIT_DIR%config\portal-env.bat" (
    echo   [OK] config\portal-env.bat found.
    echo   [OK] config\portal-env.bat found. >> "%LOG_FILE%"
    call "%KIT_DIR%config\portal-env.bat"
) else (
    echo   [ERROR] config\portal-env.bat is MISSING!
    echo           Please copy config\portal-env.bat.example to config\portal-env.bat.
    echo   [ERROR] config\portal-env.bat is MISSING! >> "%LOG_FILE%"
    set /a ERRORS+=1
)

if "%DEMO_PORT%"=="" set DEMO_PORT=8088

REM ----------------------------------------------------------------------------
REM 2. Check Java Runtime / JDK
REM ----------------------------------------------------------------------------
echo [2/6] Checking Java runtime environment...
set "JAVA_CMD="
if exist "%KIT_DIR%jre\bin\java.exe" (
    set "JAVA_CMD=%KIT_DIR%jre\bin\java.exe"
    echo   [OK] Bundled portable Java 21 runtime detected in jre\ (No installation required!)
    echo   [OK] Bundled portable Java 21 runtime detected in jre\. >> "%LOG_FILE%"
) else (
    if not exist "%JAVA_HOME%\bin\java.exe" (
        if exist "C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe" (
            set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
            set "JRE_HOME=C:\Program Files\Java\jdk-21.0.12.1"
        )
    )
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\java.exe" (
            set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
        )
    )
    if "!JAVA_CMD!"=="" set "JAVA_CMD=java"
)

"%JAVA_CMD%" -version > nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo   [OK] Java runtime is verified and working.
    echo   [OK] Java runtime working: >> "%LOG_FILE%"
    "%JAVA_CMD%" -version >> "%LOG_FILE%" 2>&1
) else (
    echo   [ERROR] Java 21 runtime not found!
    echo           Please install Oracle JDK 21 or Eclipse Temurin 21.
    echo   [ERROR] Java 21 runtime not found! >> "%LOG_FILE%"
    set /a ERRORS+=1
)

REM ----------------------------------------------------------------------------
REM 3. Check Application WAR File
REM ----------------------------------------------------------------------------
echo [3/6] Checking application package...
if exist "%KIT_DIR%application\student-course-registration.war" (
    echo   [OK] application\student-course-registration.war found.
    echo   [OK] application\student-course-registration.war found. >> "%LOG_FILE%"
) else (
    echo   [ERROR] application\student-course-registration.war is MISSING!
    echo   [ERROR] application\student-course-registration.war is MISSING! >> "%LOG_FILE%"
    set /a ERRORS+=1
)

if exist "%KIT_DIR%application\CHECKSUM-SHA256.txt" (
    echo   [OK] application\CHECKSUM-SHA256.txt found.
    echo   [OK] application\CHECKSUM-SHA256.txt found. >> "%LOG_FILE%"
) else (
    echo   [WARNING] CHECKSUM-SHA256.txt missing.
    set /a WARNINGS+=1
)

REM ----------------------------------------------------------------------------
REM 4. Check Isolated Demo Tomcat
REM ----------------------------------------------------------------------------
echo [4/6] Checking isolated Tomcat container...
if exist "%KIT_DIR%tomcat\bin\catalina.bat" (
    echo   [OK] Isolated demo Tomcat found in tomcat\bin\catalina.bat.
    echo   [OK] Isolated demo Tomcat found. >> "%LOG_FILE%"
) else (
    echo   [ERROR] tomcat\bin\catalina.bat is MISSING!
    echo   [ERROR] tomcat\bin\catalina.bat is MISSING! >> "%LOG_FILE%"
    set /a ERRORS+=1
)

if exist "%KIT_DIR%tomcat\conf\server.xml" (
    echo   [OK] tomcat\conf\server.xml found - Configured for port %DEMO_PORT%
    echo   [OK] tomcat\conf\server.xml found. >> "%LOG_FILE%"
) else (
    echo   [ERROR] tomcat\conf\server.xml is MISSING!
    echo   [ERROR] tomcat\conf\server.xml is MISSING! >> "%LOG_FILE%"
    set /a ERRORS+=1
)

REM ----------------------------------------------------------------------------
REM 5. Check Port Availability
REM ----------------------------------------------------------------------------
echo [5/6] Checking network port availability (Port %DEMO_PORT%)...
powershell -NoProfile -Command "$conn = Get-NetTCPConnection -LocalPort %DEMO_PORT% -ErrorAction SilentlyContinue; if ($conn) { exit 1 } else { exit 0 }" > nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo   [OK] Port %DEMO_PORT% is available for demo Tomcat.
    echo   [OK] Port %DEMO_PORT% is available. >> "%LOG_FILE%"
) else (
    echo   [WARNING] Port %DEMO_PORT% is currently occupied by another process!
    echo             If demo Tomcat is already running, run Stop-Portal.bat first.
    echo             Or edit DEMO_PORT in config\portal-env.bat.
    echo   [WARNING] Port %DEMO_PORT% is occupied. >> "%LOG_FILE%"
    set /a WARNINGS+=1
)

REM ----------------------------------------------------------------------------
REM 6. Check Database Scripts & MySQL Service Port 3306
REM ----------------------------------------------------------------------------
echo [6/6] Checking database setup and MySQL port (Port 3306)...
if exist "%KIT_DIR%database\01_init_demo_database.sql" (
    echo   [OK] database\01_init_demo_database.sql found.
) else (
    echo   [ERROR] database\01_init_demo_database.sql is MISSING!
    set /a ERRORS+=1
)

if exist "%KIT_DIR%database\02_seed_demo_data.sql" (
    echo   [OK] database\02_seed_demo_data.sql found.
) else (
    echo   [ERROR] database\02_seed_demo_data.sql is MISSING!
    set /a ERRORS+=1
)

powershell -NoProfile -Command "$conn = Get-NetTCPConnection -LocalPort 3306 -ErrorAction SilentlyContinue; if ($conn) { exit 0 } else { exit 1 }" > nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo   [OK] MySQL service detected listening on port 3306.
    echo   [OK] MySQL service listening on port 3306. >> "%LOG_FILE%"
) else (
    echo   [WARNING] MySQL service was not detected on port 3306.
    echo             Ensure MySQL Server 8.0/8.4 is started before launching the portal.
    echo   [WARNING] MySQL not detected on port 3306. >> "%LOG_FILE%"
    set /a WARNINGS+=1
)

echo.
echo ============================================================================
if %ERRORS% equ 0 (
    echo   VERIFICATION RESULT: PASSED [Ready for Demo]
    if %WARNINGS% gtr 0 echo   (Warnings: %WARNINGS% - see details above)
    echo   Log saved to: logs\verify-setup.log
    echo ============================================================================
    echo.
    exit /b 0
) else (
    echo   VERIFICATION RESULT: FAILED [%ERRORS% Errors Detected]
    echo   Please resolve the errors above before launching the portal.
    echo   Log saved to: logs\verify-setup.log
    echo ============================================================================
    echo.
    exit /b 1
)
