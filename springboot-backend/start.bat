@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"
echo ========================================
echo   NetConfig SpringBoot Backend
echo   端口: 8080
echo   数据库: netconfig.db
echo ========================================
echo.
echo 前置条件:
echo   - JDK 17+
echo   - Maven 3.6+
echo   - 已安装 Maven (mvn --version)
echo.
if not exist "pom.xml" (
    echo [错误] 未找到 pom.xml，请确认在 springboot-backend 目录下运行
    pause
    exit /b 1
)
mvn spring-boot:run
pause
