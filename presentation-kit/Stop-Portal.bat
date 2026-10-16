@echo off
setlocal enabledelayedexpansion

REM ============================================================================
REM Stop-Portal.bat - Presentation Kit Demo Shutdown Script
REM ============================================================================

set "KIT_DIR=%~dp0"
set "DEMO_PORT=8088"
set "SHUTDOWN_PORT=8006"

echo ============================================================================
echo   STOPPING STUDENT COURSE REGISTRATION PORTAL (DEMO ENVIRONMENT)
echo ============================================================================
echo.

set "CATALINA_HOME=%KIT_DIR%tomcat"
set "CATALINA_BASE=%KIT_DIR%tomcat"

echo Step 1: Sending shutdown command to Demo Tomcat (Port %SHUTDOWN_PORT%)...
call "%KIT_DIR%tomcat\bin\shutdown.bat" 5 -force > nul 2>&1

echo Step 2: Verifying port %DEMO_PORT% is released...
set "PORT_FREE=0"
for /l %%i in (1, 1, 10) do (
    if !PORT_FREE! equ 0 (
        ping 127.0.0.1 -n 2 > nul
        netstat -ano | findstr /R /C:":%DEMO_PORT% " > nul 2>&1
        if !ERRORLEVEL! neq 0 (
            set "PORT_FREE=1"
        ) else (
            <nul set /p=.
        )
    )
)

echo.
if !PORT_FREE! equ 1 (
    echo.
    echo ============================================================================
    echo   DEMO APPLICATION STOPPED SUCCESSFULLY!
    echo   Port %DEMO_PORT% has been released.
    echo   (Note: Your production Tomcat on port 8080 remains completely untouched.)
    echo ============================================================================
    echo.
) else (
    echo.
    echo [NOTICE] Demo Tomcat is still releasing port %DEMO_PORT%.
    echo Terminating remaining Demo process specifically listening on %DEMO_PORT%...
    powershell -NoProfile -Command "$conn = Get-NetTCPConnection -LocalPort %DEMO_PORT% -ErrorAction SilentlyContinue; if ($conn) { Stop-Process -Id $conn.OwningProcess -Force -ErrorAction SilentlyContinue; Write-Host 'Stopped PID:' $conn.OwningProcess }"
    echo Demo Tomcat terminated.
    echo.
)

pause
