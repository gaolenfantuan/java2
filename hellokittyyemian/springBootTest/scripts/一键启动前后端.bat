@echo off
chcp 936 >nul
title Hello Kitty 一键启动
cd /d "%~dp0.."

echo ==========================================================
echo    一键启动：后端(8080) + 前端(5173)
echo ==========================================================
echo.

echo [1/2] 打开后端窗口 ...
start "Kitty后端" "%~dp0启动后端.bat"

echo [2/2] 打开前端窗口 ...
set "FE=%USERPROFILE%\Desktop\hellokitty-login-vue\scripts\启动前端.bat"
if not exist "%FE%" goto :no_fe
start "Kitty前端" "%FE%"
goto :done

:no_fe
echo [提示] 没找到前端启动脚本：%FE%
echo        如果前端项目换了位置，直接双击那个目录下的 scripts\启动前端.bat 即可。

:done
echo.
echo 两个窗口已经打开。
echo   后端首次启动要编译 30 秒左右，等它打印 "Hello Kitty 后端启动成功！" 再操作页面。
echo   然后浏览器打开： http://localhost:5173
echo.
echo 停止全部服务：双击 scripts\一键停止全部.bat
ping -n 9 127.0.0.1 >nul
exit /b 0
