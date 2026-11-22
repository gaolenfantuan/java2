@echo off
chcp 936 >nul
title 停止前端 (5173)
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
  taskkill /PID %%p /F >nul 2>nul
)
echo 已停止端口 5173 上的前端进程。
