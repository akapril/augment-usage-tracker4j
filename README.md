# Augment Usage Tracker for JetBrains IDEs

> **版本**: 1.0.0
> **支持**: 所有 JetBrains IDE 2023.1+
> **语言**: 简体中文

一个兼容所有 JetBrains IDE 的插件，在状态栏实时显示 Augment AI 使用统计信息。

## 🎯 支持的 IDE

### 完全兼容以下所有 JetBrains IDE：
- **IntelliJ IDEA** (Ultimate & Community)
- **PyCharm** (Professional & Community)
- **WebStorm** - JavaScript 和 TypeScript 开发
- **PhpStorm** - PHP 开发
- **CLion** - C/C++ 开发
- **GoLand** - Go 开发
- **RubyMine** - Ruby 开发
- **DataGrip** - 数据库工具
- **Rider** - .NET 开发
- **AppCode** - iOS/macOS 开发
- **MPS** - 语言工程
- **Android Studio** - Android 开发

### 版本要求
- **最低版本**: 2023.1
- **推荐版本**: 2023.2 或更高
- **最高版本**: 支持到 2025.3
- **当前测试**: GoLand 2025.1 (252.23309.35)

## ✨ 功能特性

### 🎯 核心功能
- 📊 **实时监控**: 在状态栏显示当前使用量和限额
- 🔄 **智能刷新**: 可配置的自动刷新间隔（5-300秒）
- 🍪 **Cookie 认证**: 支持浏览器 Cookie 认证方式
- ⚙️ **完全中文化**: 所有界面文本均为中文显示
- 🎯 **广泛兼容**: 支持所有 JetBrains IDE 2023.1+ 版本

### 🔐 认证与安全
- 🔒 **安全存储**: 使用 JetBrains IDE 密码保险箱安全存储认证信息
- ⚠️ **过期检测**: 自动检测并提醒 Cookie 过期状态
- 🔄 **自动恢复**: 重启后自动恢复认证状态

### 🎨 智能交互
- 💡 **状态指示**: 清晰的视觉状态提示（●正常 / ⚠过期）
- 🖱️ **智能按钮**: 根据状态显示相应的操作按钮
- 🚀 **一键操作**: 快速访问设置和数据刷新功能
- 📋 **详细统计**: 完整的使用详情和趋势分析

## 📋 系统要求

- **JetBrains IDE**: 任意 2023.1 或更高版本
- **Java**: JDK 17 或更高版本
- **操作系统**: Windows, macOS, Linux
- **网络**: 需要访问 Augment API (app.augmentcode.com)

## 🚀 安装步骤

### 方法一：从源码构建

1. **克隆项目**
   ```bash
   git clone https://github.com/akapril/augment-usage-tracker-idea.git
   cd augment-usage-tracker-idea
   ```

2. **构建插件**

   **Linux/macOS 系统:**
   ```bash
   chmod +x build-and-test.sh
   ./build-and-test.sh
   ```

   **Windows 系统 (命令提示符):**
   ```cmd
   build-and-test.bat
   ```

   **Windows 系统 (PowerShell):**
   ```powershell
   .\build-and-test.ps1
   # 或者快速构建（跳过测试）:
   .\build-and-test.ps1 -SkipTests
   ```

3. **安装插件**
   - 打开任意 JetBrains IDE
   - 进入 `File` → `Settings` → `Plugins`
   - 点击齿轮图标，选择 `Install Plugin from Disk...`
   - 选择 `build/distributions/` 目录下的 `.zip` 文件
   - 重启 IDE

### 方法二：手动构建

**Linux/macOS 系统:**
```bash
# 确保 Java 17+ 已安装
java -version

# 构建项目
./gradlew clean buildPlugin

# 查找生成的插件文件
find build/distributions -name "*.zip"
```

**Windows 系统:**
```cmd
REM 确保 Java 17+ 已安装
java -version

REM 构建项目
gradlew.bat clean buildPlugin

REM 快速构建（跳过测试）
quick-build.bat
```

## ⚙️ 配置指南

### 1. 打开设置面板

安装插件后，进入：
```
File → Settings → Tools → Augment Usage Tracker
```

### 2. 配置认证信息

#### 获取 Cookie 的方法：

**Chrome/Edge 浏览器**：
1. 登录 [Augment 网站](https://app.augmentcode.com)
2. 按 `F12` 打开开发者工具
3. 切换到 `Application` 标签页
4. 在左侧选择 `Storage` → `Cookies` → `https://app.augmentcode.com`
5. 找到 `_session` cookie，复制其值
6. 将完整的 cookie 字符串粘贴到插件设置中

**支持的 Cookie 格式**：
```
# 完整 Cookie 字符串
_session=eyJhbGciOiJIUzI1NiJ9...; other=value

# 仅 session 值
eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoxMjM0NX0.abc123
```

### 3. 其他设置

- **启用追踪器**: 开启/关闭插件功能
- **显示在状态栏**: 控制状态栏显示
- **刷新间隔**: 设置数据刷新频率（5-300秒）

### 4. 测试连接

配置完成后，点击 `Test Connection` 按钮验证设置是否正确。

## 📖 使用说明

### 状态栏显示

插件会在 IDE 底部状态栏显示使用信息：

**正常状态**:
```
用户名: 101/600 ●
```

**过期状态**:
```
用户名: 101/600 ⚠
```

**未认证状态**:
```
Augment: 未认证
```

- **用户名**: 您的邮箱前缀
- **101/600**: 当前使用量/总限额
- **●**: 实心圆点表示认证正常
- **⚠**: 警告图标表示 Cookie 已过期
- **未认证**: 提示需要配置 Cookie

### 智能交互操作

#### 未认证状态
- **点击状态栏** → 弹出配置对话框
- **[打开设置]** 按钮 → 直接跳转到插件设置页面

#### Cookie 过期状态
- **点击状态栏** → 弹出过期提醒对话框
- **[打开设置]** 按钮 → 直接跳转到设置页面更新 Cookie

#### 正常状态
- **左键点击** → 显示详细使用统计（含 [刷新数据] 按钮）
- **Ctrl + 左键** → 直接手动刷新数据
- **右键点击** → 显示详细信息

### 详细信息包含

- 用户信息（邮箱、姓名、计划类型）
- 使用统计（当前用量、限额、百分比、剩余额度）
- 最后更新时间
- 认证状态

## 🔧 故障排除

### 常见问题

**1. 状态栏显示 "未认证"**
- 检查 Cookie 是否正确配置
- 点击状态栏，使用 [打开设置] 按钮快速配置
- 确认 Cookie 格式正确

**2. 连接测试失败**
- 检查网络连接
- 确认可以访问 app.augmentcode.com
- 验证 Cookie 格式是否正确

**3. 数据不更新**
- 检查刷新间隔设置
- 手动点击刷新按钮测试
- 查看 IDE 日志文件中的错误信息

**4. 插件无法加载**
- 确认 IDE 版本 ≥ 2023.1
- 检查 Java 版本 ≥ 17
- 重启 IDE 后重试

### 日志查看

如需查看详细日志：
```
Help → Show Log in Explorer/Finder
```
搜索包含 "AugmentUsageTracker" 的日志条目。

### Cookie 过期处理

Cookie 通常在20小时后过期，插件会自动检测并提示：
1. 重新登录 Augment 网站
2. 获取新的 Cookie
3. 在设置中更新 Cookie

## 🛠️ 开发信息

### 技术栈

- **语言**: Kotlin
- **构建工具**: Gradle + gradle-intellij-plugin
- **HTTP 客户端**: OkHttp3
- **JSON 解析**: Gson
- **平台**: IntelliJ Platform SDK

### 项目结构

```
src/main/kotlin/com/augmentcode/usagetracker/
├── model/              # 数据模型
├── service/            # 核心服务
├── ui/                 # 用户界面
├── settings/           # 设置面板
└── util/              # 工具类
```

### 开发命令

```bash
# 开发模式运行
./gradlew runIde

# 运行测试
./gradlew test

# 构建插件
./gradlew buildPlugin

# 验证插件
./gradlew verifyPlugin
```

## 📝 版本历史

### v1.0.0 (2024-12-21)
- ✨ 初始版本发布
- ✅ 状态栏实时显示功能
- ✅ Cookie 认证支持
- ✅ 智能刷新机制（5-300秒可配置）
- ✅ 完全中文化界面
- ✅ Cookie 过期检测和提醒
- ✅ 智能交互按钮（一键打开设置/刷新数据）
- ✅ 设置面板集成
- ✅ 支持所有 JetBrains IDE 2023.1+
- ✅ 安全的凭据存储

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 开发环境设置

1. 克隆项目
2. 使用任意 JetBrains IDE 打开
3. 确保 JDK 17+ 已配置
4. 运行 `./gradlew runIde` 启动开发环境

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 🔗 相关链接

- [GitHub 仓库](https://github.com/akapril/augment-usage-tracker-idea)
- [问题反馈](https://github.com/akapril/augment-usage-tracker-idea/issues)
- [Augment 官网](https://app.augmentcode.com)
- [VSCode 版本](https://github.com/akapril/augment-usage-tracker)

## 👨‍💻 作者

**akapril**
- Email: wiq@live.com
- GitHub: [@akapril](https://github.com/akapril)

---

如果这个插件对您有帮助，请考虑给项目一个 ⭐ Star！
