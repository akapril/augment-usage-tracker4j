@echo off
REM Quick build script for Augment Usage Tracker IDEA Plugin (Windows)
REM Augment Usage Tracker IDEA 插件快速构建脚本 (Windows)

echo.
echo 🚀 Quick Build - Augment Usage Tracker IDEA Plugin
echo 🚀 快速构建 - Augment Usage Tracker IDEA 插件
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java not found! Please install Java 17+
    echo ❌ 未找到 Java！请安装 Java 17+
    pause
    exit /b 1
)

REM Check gradlew.bat
if not exist "gradlew.bat" (
    echo ❌ gradlew.bat not found!
    echo ❌ 未找到 gradlew.bat！
    pause
    exit /b 1
)

echo ✅ Environment check passed
echo ✅ 环境检查通过
echo.

REM Quick build (skip tests for speed)
echo 📦 Building plugin (skipping tests for speed)...
echo 📦 构建插件（跳过测试以提高速度）...
call gradlew.bat clean buildPlugin -x test

if errorlevel 1 (
    echo.
    echo ❌ Build failed!
    echo ❌ 构建失败！
    pause
    exit /b 1
)

echo.
echo ✅ Build completed!
echo ✅ 构建完成！

REM Find and display plugin file
for /f "delims=" %%i in ('dir /b /s build\distributions\*.zip 2^>nul') do (
    echo.
    echo 📁 Plugin file: %%i
    echo 📁 插件文件: %%i
    
    REM Ask if user wants to open the directory
    set /p OPEN="Open directory? (y/n) / 打开目录？(y/n): "
    if /i "!OPEN!"=="y" (
        explorer "build\distributions"
    )
    goto :end
)

echo ❌ Plugin file not found!
echo ❌ 未找到插件文件！

:end
echo.
echo Press any key to exit...
echo 按任意键退出...
pause >nul
