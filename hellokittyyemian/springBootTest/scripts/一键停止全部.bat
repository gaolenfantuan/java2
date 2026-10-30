@echo off
chcp 936 >nul
echo ==========================================================
echo    Ò»¼üÍ£Ö¹£ººó¶Ë(8080) + Ç°¶Ë(5173)
echo ==========================================================
echo.
call "%~dp0Í£Ö¹ºó¶Ë.bat" /quiet
set "FES=%USERPROFILE%\Desktop\hellokitty-login-vue\scripts\Í£Ö¹Ç°¶Ë.bat"
if not exist "%FES%" goto :done
call "%FES%" /quiet

:done
echo.
echo È«²¿ÒÑÍ£Ö¹¡£
ping -n 4 127.0.0.1 >nul
exit /b 0
