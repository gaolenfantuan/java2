@echo off
chcp 936 >nul
title Hello Kitty 前端 (端口 5173)
cd /d "%~dp0.."

echo ==========================================================
echo    Hello Kitty 前端启动器   Vue 3 + Vite
echo ==========================================================
echo.

rem ---------------- 1. 找 Node ----------------
echo [1/3] 检查 Node.js ...
where node >nul 2>nul
if not errorlevel 1 goto :node_ok
if not exist "C:\Program Files\nodejs\node.exe" goto :no_node
set "PATH=C:\Program Files\nodejs;%PATH%"
:node_ok
for /f "delims=" %%v in ('node -v') do echo         Node 版本 %%v

rem ---------------- 2. 准备依赖 ----------------
echo [2/3] 准备依赖（node_modules 每次开机都被删）...
if exist "node_modules\" goto :deps_ok
if not exist ".deps-cache\node_modules.zip" goto :deps_install
echo         发现离线缓存，直接还原（约 3-10 秒）...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0deps-cache.ps1" restore
if exist "node_modules\" goto :deps_ok
echo         缓存不可用或已过期，改为在线安装...
del /q ".deps-cache\node_modules.zip" >nul 2>nul

:deps_install
if not exist "package-lock.json" goto :npm_install
echo         正在安装依赖，约 1-2 分钟（有 npm 缓存会快很多）...
call npm ci --no-audit --no-fund
if errorlevel 1 goto :deps_fail
goto :deps_ok
:npm_install
echo         正在安装依赖，约 1-2 分钟...
call npm install --no-audit --no-fund
if errorlevel 1 goto :deps_fail

:deps_ok
if exist ".deps-cache\node_modules.zip" goto :deps_cached
echo         正在生成离线缓存（下次开机 3 秒还原）...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0deps-cache.ps1" save
:deps_cached
echo         依赖就绪

rem ---------------- 3. 启动 ----------------
echo [3/3] 启动开发服务器 ...
echo         页面地址  http://localhost:5173
echo         后端接口  页面里的 /api 请求会代理到 http://localhost:8080
echo         停止服务  双击 scripts\停止前端.bat
echo.
call npm run dev
echo.
echo 前端已退出。
pause
exit /b 0

:no_node
echo [错误] 没找到 Node.js（需要 18 以上版本）。
echo        请到 https://nodejs.org 下载 LTS 安装，装完重开这个脚本。
pause
exit /b 1

:deps_fail
echo [错误] 依赖安装失败，多半是网络问题。可以试试换国内镜像：
echo        npm config set registry https://registry.npmmirror.com
pause
exit /b 1
