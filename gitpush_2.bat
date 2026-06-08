@echo off
chcp 65001
cd /d "%~dp0"

:: ========== 只改这里 ==========
set COMMIT_MSG=.1523281629553499:b0315284146e8e50307061780d661c7a_6a262f9876c46c0350cc2323.6a2633ab76c46c0350cc255a.6a2633aa6dccec5c051cb2e3:Trae CN.T(2026/6/8 11:14:51)
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