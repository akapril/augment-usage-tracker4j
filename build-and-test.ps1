# Build and test script for Augment Usage Tracker IDEA Plugin (PowerShell)
# 适用于 Windows PowerShell 的 Augment Usage Tracker IDEA 插件构建和测试脚本

param(
    [switch]$SkipTests,
    [switch]$OpenDir,
    [switch]$Help
)

# Color functions for better output
function Write-Success { param($Message) Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Error { param($Message) Write-Host "❌ $Message" -ForegroundColor Red }
function Write-Warning { param($Message) Write-Host "⚠️ $Message" -ForegroundColor Yellow }
function Write-Info { param($Message) Write-Host "ℹ️ $Message" -ForegroundColor Cyan }
function Write-Step { param($Message) Write-Host "🔄 $Message" -ForegroundColor Blue }

# Help function
function Show-Help {
    Write-Host @"
Augment Usage Tracker IDEA Plugin Build Script
Augment Usage Tracker IDEA 插件构建脚本

Usage / 用法:
    .\build-and-test.ps1 [options]

Options / 选项:
    -SkipTests    Skip running tests / 跳过测试
    -OpenDir      Open build directory after completion / 完成后打开构建目录
    -Help         Show this help message / 显示此帮助信息

Examples / 示例:
    .\build-and-test.ps1                    # Full build with tests / 完整构建包含测试
    .\build-and-test.ps1 -SkipTests         # Build without tests / 构建但跳过测试
    .\build-and-test.ps1 -OpenDir           # Build and open directory / 构建并打开目录
    .\build-and-test.ps1 -SkipTests -OpenDir # Fast build and open / 快速构建并打开

Requirements / 系统要求:
    - Java 17 or higher / Java 17 或更高版本
    - IntelliJ IDEA 2023.1+ / IntelliJ IDEA 2023.1+
    - Windows PowerShell 5.0+ / Windows PowerShell 5.0+
"@
}

if ($Help) {
    Show-Help
    exit 0
}

Write-Host ""
Write-Host "🚀 Building Augment Usage Tracker IDEA Plugin..." -ForegroundColor Magenta
Write-Host "🚀 正在构建 Augment Usage Tracker IDEA 插件..." -ForegroundColor Magenta
Write-Host ""

# Check PowerShell version
if ($PSVersionTable.PSVersion.Major -lt 5) {
    Write-Error "PowerShell 5.0 or higher is required"
    Write-Error "需要 PowerShell 5.0 或更高版本"
    exit 1
}

# Check if Java is available
try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java not found"
    }
    
    # Extract Java version
    $versionLine = $javaVersion | Select-String "version" | Select-Object -First 1
    $versionString = $versionLine -replace '.*"([^"]*)".*', '$1'
    $majorVersion = ($versionString -split '\.')[0]
    
    if ([int]$majorVersion -lt 17) {
        Write-Error "Java 17 or higher is required (found Java $majorVersion)"
        Write-Error "需要 Java 17 或更高版本 (当前版本: Java $majorVersion)"
        exit 1
    }
    
    Write-Success "Java $majorVersion detected"
    Write-Success "检测到 Java $majorVersion"
} catch {
    Write-Error "Java is not installed or not in PATH"
    Write-Error "Java 未安装或不在 PATH 环境变量中"
    Write-Info "Please install Java 17+ from: https://adoptium.net/"
    Write-Info "请从以下地址安装 Java 17+: https://adoptium.net/"
    exit 1
}

# Check if gradlew.bat exists
if (-not (Test-Path "gradlew.bat")) {
    Write-Error "gradlew.bat not found"
    Write-Error "未找到 gradlew.bat"
    Write-Info "Please ensure you are in the correct project directory"
    Write-Info "请确保您在正确的项目目录中"
    exit 1
}

Write-Host ""

try {
    # Clean previous builds
    Write-Step "Cleaning previous builds... / 清理之前的构建..."
    & .\gradlew.bat clean
    if ($LASTEXITCODE -ne 0) { throw "Clean failed / 清理失败" }
    Write-Success "Clean completed / 清理完成"
    Write-Host ""

    # Compile the project
    Write-Step "Compiling project... / 编译项目..."
    & .\gradlew.bat compileKotlin
    if ($LASTEXITCODE -ne 0) { throw "Compilation failed / 编译失败" }
    Write-Success "Compilation completed / 编译完成"
    Write-Host ""

    # Run tests (unless skipped)
    if (-not $SkipTests) {
        Write-Step "Running tests... / 运行测试..."
        & .\gradlew.bat test
        if ($LASTEXITCODE -ne 0) {
            Write-Warning "Tests failed, but continuing with build / 测试失败，但继续构建"
        } else {
            Write-Success "Tests passed / 测试通过"
        }
        Write-Host ""
    } else {
        Write-Info "Skipping tests as requested / 按要求跳过测试"
        Write-Host ""
    }

    # Build the plugin
    Write-Step "Building plugin... / 构建插件..."
    & .\gradlew.bat buildPlugin
    if ($LASTEXITCODE -ne 0) { throw "Plugin build failed / 插件构建失败" }
    Write-Success "Plugin build completed / 插件构建完成"
    Write-Host ""

    # Verify the plugin
    Write-Step "Verifying plugin... / 验证插件..."
    & .\gradlew.bat verifyPlugin
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "Plugin verification failed, but plugin was built / 插件验证失败，但插件已构建"
    } else {
        Write-Success "Plugin verification passed / 插件验证通过"
    }
    Write-Host ""

    # Check if plugin ZIP was created
    $pluginZip = Get-ChildItem -Path "build\distributions" -Filter "*.zip" -ErrorAction SilentlyContinue | Select-Object -First 1
    
    if ($pluginZip) {
        $sizeKB = [math]::Round($pluginZip.Length / 1KB, 2)
        Write-Success "Plugin built successfully: $($pluginZip.FullName)"
        Write-Success "插件构建成功: $($pluginZip.FullName)"
        Write-Info "Plugin size / 插件大小: $sizeKB KB"
    } else {
        Write-Error "Plugin ZIP not found / 未找到插件 ZIP 文件"
        Write-Info "Check build\distributions\ directory for build artifacts"
        Write-Info "请检查 build\distributions\ 目录中的构建产物"
        exit 1
    }

    Write-Host ""
    Write-Host "🎉 Build completed successfully! / 构建成功完成！" -ForegroundColor Green
    Write-Host ""
    
    # Display next steps
    Write-Host "📋 Next steps / 下一步操作:" -ForegroundColor Yellow
    Write-Host "1. Install the plugin / 安装插件: File → Settings → Plugins → Install Plugin from Disk"
    Write-Host "2. Select the file / 选择文件: $($pluginZip.FullName)"
    Write-Host "3. Restart IntelliJ IDEA / 重启 IntelliJ IDEA"
    Write-Host "4. Configure / 配置: File → Settings → Tools → Augment Usage Tracker"
    Write-Host ""
    
    Write-Host "🔧 Development commands / 开发命令:" -ForegroundColor Cyan
    Write-Host "• Run in development / 开发模式运行: .\gradlew.bat runIde"
    Write-Host "• Run tests only / 仅运行测试: .\gradlew.bat test"
    Write-Host "• Clean build / 清理构建: .\gradlew.bat clean build"
    Write-Host "• View tasks / 查看任务: .\gradlew.bat tasks"
    Write-Host ""

    # Open build directory if requested
    if ($OpenDir -or $pluginZip) {
        if ($OpenDir) {
            Write-Info "Opening build directory... / 打开构建目录..."
            if (Test-Path "build\distributions") {
                Invoke-Item "build\distributions"
            } else {
                Write-Warning "Directory not found / 目录未找到: build\distributions"
            }
        }
    }

} catch {
    Write-Error $_.Exception.Message
    Write-Host ""
    Write-Host "Build failed! Check the error messages above." -ForegroundColor Red
    Write-Host "构建失败！请检查上面的错误信息。" -ForegroundColor Red
    exit 1
}

Write-Host "Press any key to exit... / 按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
