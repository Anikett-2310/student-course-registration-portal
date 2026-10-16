@echo off
REM ============================================================================
REM setenv.bat - Injected Tomcat Environment for Presentation Demo
REM ============================================================================

set "KIT_ROOT=%~dp0..\.."

if exist "%KIT_ROOT%\config\portal-env.bat" (
    call "%KIT_ROOT%\config\portal-env.bat"
)

REM 1. Prioritize bundled portable Java 21 runtime if present
if exist "%KIT_ROOT%\jre\bin\java.exe" (
    set "JRE_HOME=%KIT_ROOT%\jre"
    set "JAVA_HOME=%KIT_ROOT%\jre"
) else (
    REM Auto-detect system Java 21 if bundled JRE is absent
    if not exist "%JAVA_HOME%\bin\java.exe" (
        if exist "C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe" (
            set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
            set "JRE_HOME=C:\Program Files\Java\jdk-21.0.12.1"
        )
    )
)

REM 2. Pass demo database parameters to JVM system properties
set CATALINA_OPTS=%CATALINA_OPTS% "-Dcourseportal.db.url=%PORTAL_DB_URL%" "-Dcourseportal.db.user=%PORTAL_DB_USER%" "-Dcourseportal.db.password=%PORTAL_DB_PASSWORD%"
