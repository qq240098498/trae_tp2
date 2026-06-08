@echo off
chcp 65001
cd /d "%~dp0"

:: ========== 只改这里 ==========
set COMMIT_MSG=.1523281629553499:86afa9441eca7a3f7832de37d6944700_6a266a2976c46c0350cc28cd.6a266a8876c46c0350cc28cf.6a266a886dccec5c051cb2e8:Trae CN.T(2026/6/8 15:08:56)
:: ==============================

:: 排除bat自己，不提交
git reset gitpush.bat >nul 2>&1

:: 提交代码
git add .
git reset gitpush.bat >nul 2>&1
git commit -m "%COMMIT_MSG%"

:: 获取commitId
for /f %%i in ('git rev-parse HEAD') do set COMMIT_ID=%%i

:: 推送
git pull origin main >nul 2>&1
git push origin main

:: 输出结果
echo.
echo ==========================
echo 提交备注：%COMMIT_MSG%
echo 提交ID：%COMMIT_ID%
echo ==========================
echo.
pause