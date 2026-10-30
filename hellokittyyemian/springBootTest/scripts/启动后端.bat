@echo off
chcp 936 >nul
title Hello Kitty 后端 (端口 8080)
cd /d "%~dp0.."

echo ==========================================================
echo    Hello Kitty 后端启动器   Spring Boot 3.4.5 + H2 内存库
echo ==========================================================
echo.

rem 防止外部环境变量覆盖 application.yml 里的 server.port
set "SERVER__PORT="
set "SERVER_PORT="

rem ---------------- 1. 找 Java ----------------
echo [1/4] 检查 Java ...
where java >nul 2>nul
if not errorlevel 1 goto :java_ok
if not exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot\bin\java.exe" goto :no_java
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
set "PATH=%JAVA_HOME%\bin;%PATH%"
:java_ok
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do echo        Java 版本 %%~v

rem ---------------- 2. 源码指纹 ----------------
echo [2/4] 检查源码有没有改动 ...
set "CACHE_DIR=%CD%\.build-cache"
set "CACHE_JAR=%CACHE_DIR%\app.jar"
set "CACHE_HASH=%CACHE_DIR%\src.hash"
set "NEW_HASH="
for /f "usebackq delims=" %%h in (`powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0src-hash.ps1"`) do set "NEW_HASH=%%h"

if not defined NEW_HASH goto :need_build
if /i "%NEW_HASH%"=="ERROR" goto :need_build
echo        指纹 %NEW_HASH:~0,12%
if not exist "%CACHE_JAR%" goto :need_build
if not exist "%CACHE_HASH%" goto :need_build

set "OLD_HASH="
set /p OLD_HASH=<"%CACHE_HASH%"
if /i "%OLD_HASH%"=="%NEW_HASH%" goto :cache_hit
echo        源码有改动，需要重新编译
goto :need_build

:cache_hit
echo        源码没变，直接用上次编译好的 jar（跳过 Maven，秒开）
set "JAR=%CACHE_JAR%"
goto :run

:need_build
rem ---------------- 3. 找 Maven 并编译 ----------------
echo [3/4] 找 Maven ...
set "MVN="
where mvn >nul 2>nul
if not errorlevel 1 set "MVN=mvn"
if defined MVN goto :mvn_ok
for /d %%d in ("D:\IDEA\*") do if exist "%%d\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" set "MVN=%%d\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"
if defined MVN goto :mvn_ok
for /d %%d in ("%ProgramFiles%\JetBrains\*") do if exist "%%d\plugins\maven\lib\maven3\bin\mvn.cmd" set "MVN=%%d\plugins\maven\lib\maven3\bin\mvn.cmd"
if not defined MVN goto :no_mvn

:mvn_ok
echo        Maven: %MVN%
echo        编译中（target 每次开机都会被删，这一次要 15-30 秒，编完会缓存）...
if /i "%MVN%"=="mvn" goto :build_by_path
call "%MVN%" -B -DskipTests package
goto :build_check

:build_by_path
call mvn -B -DskipTests package

:build_check
if errorlevel 1 goto :build_fail

set "JAR="
for %%f in ("target\springBootTest-*.jar") do set "JAR=%%f"
if not defined JAR goto :build_fail

if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
copy /y "%JAR%" "%CACHE_JAR%" >nul
>"%CACHE_HASH%" echo %NEW_HASH%
echo        编译完成，jar 已缓存到 .build-cache，下次开机秒开

:run
rem ---------------- 4. 启动 ----------------
echo.
echo [4/4] 启动服务 ...
echo        登录接口   POST http://localhost:8080/api/auth/login
echo        注册接口   POST http://localhost:8080/api/auth/register
echo        H2 控制台  http://localhost:8080/h2-console  (sa / 空密码)
echo        演示账号   kitty / 1234
echo        停止服务   双击 scripts\停止后端.bat
echo.
java -jar "%JAR%" --server.port=8080
echo.
echo 后端已退出。
pause
exit /b 0

:no_java
echo [错误] 没找到 Java 17。请安装 JDK 17，或把 java.exe 加到 PATH。
pause
exit /b 1

:no_mvn
echo [错误] 没找到 Maven。已尝试：PATH、D:\IDEA\*、%ProgramFiles%\JetBrains\*
echo        办法一：装 Maven 并加入 PATH
echo        办法二：用 IDEA 打开 D:\workSpace\springBootTest，直接运行 SpringBootTestApplication
pause
exit /b 1

:build_fail
echo [错误] 编译失败，请看上方红色报错信息。
pause
exit /b 1
