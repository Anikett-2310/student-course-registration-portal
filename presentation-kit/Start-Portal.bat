@echo off
setlocal enabledelayedexpansion

REM ============================================================================
REM Start-Portal.bat - Presentation Kit Demo Launcher
REM ============================================================================

set "KIT_DIR=%~dp0"

echo ============================================================================
echo   LAUNCHING STUDENT COURSE REGISTRATION PORTAL (DEMO ENVIRONMENT)
echo ============================================================================
echo.

REM 1. Run Setup Verification
echo Step 1: Verifying environment and prerequisites...
call "%KIT_DIR%VERIFY-SETUP.bat"
if %ERRORLEVEL% neq 0 (
    echo.
    echo [LAUNCH ABORTED] Setup verification failed. Please fix the errors above.
    pause
    exit /b 1
)

REM 2. Load Environment Variables
if exist "%KIT_DIR%config\portal-env.bat" (
    call "%KIT_DIR%config\portal-env.bat"
)
if "%DEMO_PORT%"=="" set DEMO_PORT=8088

REM Ensure JRE_HOME points to bundled JRE or valid Java installation
if exist "%KIT_DIR%jre\bin\java.exe" (
    set "JRE_HOME=%KIT_DIR%jre"
    set "JAVA_HOME=%KIT_DIR%jre"
) else (
    if not exist "%JAVA_HOME%\bin\java.exe" (
        if exist "C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe" (
            set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
            set "JRE_HOME=C:\Program Files\Java\jdk-21.0.12.1"
        )
    )
)

REM 3. Set CATALINA_BASE & CATALINA_HOME to isolated Tomcat
set "CATALINA_HOME=%KIT_DIR%tomcat"
set "CATALINA_BASE=%KIT_DIR%tomcat"

REM Ensure setenv.bat exists in tomcat\bin
if not exist "%KIT_DIR%tomcat\bin\setenv.bat" (
    copy /Y "%KIT_DIR%config\tomcat-setenv.bat" "%KIT_DIR%tomcat\bin\setenv.bat" > nul 2>&1
)

REM 4. Start isolated Demo Tomcat in a dedicated named console
echo.
echo Step 2: Starting isolated demo Apache Tomcat on port %DEMO_PORT%...
echo   Target Database: %PORTAL_DB_URL%
echo   Database User:   %PORTAL_DB_USER%
echo.

start "Demo-Portal-Tomcat" "%KIT_DIR%tomcat\bin\catalina.bat" run

REM 5. Poll for application availability
echo Step 3: Waiting for application to initialize at http://localhost:%DEMO_PORT%/student-course-registration/ ...
set "TARGET_URL=http://localhost:%DEMO_PORT%/student-course-registration/"
set "IS_READY=0"

for /l %%i in (1, 1, 30) do (
    if !IS_READY! equ 0 (
        ping 127.0.0.1 -n 2 > nul
        powershell -NoProfile -Command "try { $r = Invoke-WebRequest -Uri '%TARGET_URL%' -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" > nul 2>&1
        if !ERRORLEVEL! equ 0 (
            set "IS_READY=1"
        ) else (
            <nul set /p=.
        )
    )
)

echo.
if !IS_READY! equ 1 (
    echo.
    echo ============================================================================
    echo   DEMO APPLICATION STARTED SUCCESSFULLY!
    echo ============================================================================
    echo.
    echo   Presentation URL: http://localhost:%DEMO_PORT%/student-course-registration/
    echo.
    echo   DEMO CREDENTIALS:
    echo   ------------------------------------------------------------------------
    echo   [Faculty / Admin]:
    echo     Username: demo_admin       ^| Password: DemoAdmin#2026
    echo.
    echo   [Students (All 4 Cohorts)]:
    echo     Year 1:   26881A1201       ^| Password: DemoStudent#2026  (Aarav)
    echo     Year 2:   25881A1201       ^| Password: DemoStudent#2026  (Diya)
    echo     Year 3:   24881A1201       ^| Password: DemoStudent#2026  (Kavya)
    echo     Year 4:   23881A1201       ^| Password: DemoStudent#2026  (Rohan)
    echo   ------------------------------------------------------------------------
    echo.
    echo   Opening default web browser now...
    start http://localhost:%DEMO_PORT%/student-course-registration/
    echo.
    echo   To stop the demo when finished, run: Stop-Portal.bat
    echo ============================================================================
    echo.
) else (
    echo.
    echo [WARNING] Application did not respond within 30 seconds.
    echo Please check the "Demo-Portal-Tomcat" console window or logs\ for error messages.
    echo You may still try visiting: http://localhost:%DEMO_PORT%/student-course-registration/
    echo.
)
