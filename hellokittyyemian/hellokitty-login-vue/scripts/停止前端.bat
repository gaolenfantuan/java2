@echo off
chcp 936 >nul
echo 正在查找占用 5173 端口的前端进程 ...
set "FOUND="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do call :killit %%p
if not defined FOUND echo        没有发现运行中的前端服务。
echo.
if /i "%~1"=="/quiet" exit /b 0
pause
exit /b 0

:killit
taskkill /F /PID %1 >nul 2>nul
if not errorlevel 1 echo        已结束进程 PID %1
set "FOUND=1"
exit /b 0
