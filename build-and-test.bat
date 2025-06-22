@echo off
setlocal enabledelayedexpansion

REM Build and test script for Augment Usage Tracker IDEA Plugin (Windows)
REM 适用于 Windows 系统的 Augment Usage Tracker IDEA 插件构建和测试脚本

echo.
echo 🚀 Building Augment Usage Tracker IDEA Plugin...
echo 🚀 正在构建 Augment Usage Tracker IDEA 插件...
echo.

REM Check if Java is available
REM 检查 Java 是否可用
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java is not installed or not in PATH
    echo ❌ Java 未安装或不在 PATH 环境变量中
    echo.
    echo Please install Java 17 or higher and add it to your PATH
    echo 请安装 Java 17 或更高版本并将其添加到 PATH 环境变量
    pause
    exit /b 1
)

REM Get Java version
REM 获取 Java 版本
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION_STRING=%%g
)
set JAVA_VERSION_STRING=%JAVA_VERSION_STRING:"=%
for /f "delims=." %%a in ("%JAVA_VERSION_STRING%") do set JAVA_MAJOR_VERSION=%%a

if %JAVA_MAJOR_VERSION% LSS 17 (
    echo ❌ Java 17 or higher is required (found Java %JAVA_MAJOR_VERSION%)
    echo ❌ 需要 Java 17 或更高版本 (当前版本: Java %JAVA_MAJOR_VERSION%)
    pause
    exit /b 1
)

echo ✅ Java %JAVA_MAJOR_VERSION% detected
echo ✅ 检测到 Java %JAVA_MAJOR_VERSION%
echo.

REM Check if gradlew.bat exists
REM 检查 gradlew.bat 是否存在
if not exist "gradlew.bat" (
    echo ❌ gradlew.bat not found
    echo ❌ 未找到 gradlew.bat
    echo Please ensure you are in the correct project directory
    echo 请确保您在正确的项目目录中
    pause
    exit /b 1
)

REM Clean previous builds
REM 清理之前的构建
echo 🧹 Cleaning previous builds...
echo 🧹 清理之前的构建...
call gradlew.bat clean
if errorlevel 1 (
    echo ❌ Clean failed
    echo ❌ 清理失败
    pause
    exit /b 1
)
echo ✅ Clean completed
echo ✅ 清理完成
echo.

REM Compile the project
REM 编译项目
echo 🔨 Compiling project...
echo 🔨 编译项目...
call gradlew.bat compileKotlin
if errorlevel 1 (
    echo ❌ Compilation failed
    echo ❌ 编译失败
    pause
    exit /b 1
)
echo ✅ Compilation completed
echo ✅ 编译完成
echo.

REM Run tests
REM 运行测试
echo 🧪 Running tests...
echo 🧪 运行测试...
call gradlew.bat test
if errorlevel 1 (
    echo ⚠️ Tests failed, but continuing with build
    echo ⚠️ 测试失败，但继续构建
    echo.
) else (
    echo ✅ Tests passed
    echo ✅ 测试通过
    echo.
)

REM Build the plugin
REM 构建插件
echo 📦 Building plugin...
echo 📦 构建插件...
call gradlew.bat buildPlugin
if errorlevel 1 (
    echo ❌ Plugin build failed
    echo ❌ 插件构建失败
    pause
    exit /b 1
)
echo ✅ Plugin build completed
echo ✅ 插件构建完成
echo.

REM Verify the plugin
REM 验证插件
echo 🔍 Verifying plugin...
echo 🔍 验证插件...
call gradlew.bat verifyPlugin
if errorlevel 1 (
    echo ⚠️ Plugin verification failed, but plugin was built
    echo ⚠️ 插件验证失败，但插件已构建
    echo.
) else (
    echo ✅ Plugin verification passed
    echo ✅ 插件验证通过
    echo.
)

REM Check if plugin ZIP was created
REM 检查插件 ZIP 文件是否已创建
set PLUGIN_ZIP=
for /f "delims=" %%i in ('dir /b /s build\distributions\*.zip 2^>nul') do (
    set PLUGIN_ZIP=%%i
    goto :found_zip
)

:found_zip
if defined PLUGIN_ZIP (
    echo ✅ Plugin built successfully: %PLUGIN_ZIP%
    echo ✅ 插件构建成功: %PLUGIN_ZIP%
    
    REM Get file size
    REM 获取文件大小
    for %%A in ("%PLUGIN_ZIP%") do (
        set /a SIZE_KB=%%~zA/1024
        echo 📊 Plugin size: !SIZE_KB! KB
        echo 📊 插件大小: !SIZE_KB! KB
    )
) else (
    echo ❌ Plugin ZIP not found
    echo ❌ 未找到插件 ZIP 文件
    echo Check build\distributions\ directory for build artifacts
    echo 请检查 build\distributions\ 目录中的构建产物
    pause
    exit /b 1
)

echo.
echo 🎉 Build completed successfully!
echo 🎉 构建成功完成！
echo.
echo 📋 Next steps / 下一步操作:
echo 1. Install the plugin / 安装插件: File → Settings → Plugins → Install Plugin from Disk
echo 2. Select the file / 选择文件: %PLUGIN_ZIP%
echo 3. Restart IntelliJ IDEA / 重启 IntelliJ IDEA
echo 4. Configure / 配置: File → Settings → Tools → Augment Usage Tracker
echo.
echo 🔧 Development commands / 开发命令:
echo • Run in development / 开发模式运行: gradlew.bat runIde
echo • Run tests only / 仅运行测试: gradlew.bat test
echo • Clean build / 清理构建: gradlew.bat clean build
echo • View tasks / 查看任务: gradlew.bat tasks
echo.

REM Open build directory if requested
REM 如果需要，打开构建目录
set /p OPEN_DIR="Open build directory? (y/n) / 打开构建目录？(y/n): "
if /i "%OPEN_DIR%"=="y" (
    if exist "build\distributions" (
        explorer "build\distributions"
    ) else (
        echo Directory not found / 目录未找到: build\distributions
    )
)

echo.
echo Press any key to exit / 按任意键退出...
pause >nul
exit /b 0
