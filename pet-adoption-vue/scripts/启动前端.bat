@echo off
chcp 936 >nul
title 宠物领养站 - 前端 (5173)
cd /d "%~dp0.."
setlocal EnableDelayedExpansion

echo ==========================================================
echo   前端启动  Vue 3 + Vite  [5173]
echo ==========================================================
echo.

for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
  taskkill /PID %%p /F >nul 2>nul
)

where npm >nul 2>nul
if errorlevel 1 goto :no_npm

rem ---- 1. 依赖：缓存优先（node_modules 每次开机都会被删） ----
echo [1/2] 检查依赖...
if exist "node_modules\vite\bin\vite.js" goto :deps_ok
if exist ".deps-cache\node_modules\vite\bin\vite.js" (
  echo       从 .deps-cache 还原 node_modules ...
  robocopy ".deps-cache\node_modules" "node_modules" /E /MT:16 /NFL /NDL /NJH /NJS /NP >nul
)
if exist "node_modules\vite\bin\vite.js" goto :deps_ok

echo       首次安装依赖 npm install ...
call npm install
if errorlevel 1 goto :npm_fail
if not exist ".deps-cache" mkdir ".deps-cache"
echo       保存依赖缓存（下次开机秒恢复）...
robocopy "node_modules" ".deps-cache\node_modules" /E /MT:16 /NFL /NDL /NJH /NJS /NP >nul

:deps_ok
echo [2/2] 启动前端...
echo       访问 http://localhost:5173
echo.
call npm run dev
echo.
echo 前端已退出。
pause
exit /b 0

:no_npm
echo [错误] 未找到 npm，请安装 Node.js。
pause
exit /b 1

:npm_fail
echo [错误] npm install 失败，请检查网络或 npm 源。
pause
exit /b 1
