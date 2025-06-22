# 开发者指南 / Developer Guide

> Augment Usage Tracker for IntelliJ IDEA 开发文档

## 📋 目录

1. [项目概述](#项目概述)
2. [技术架构](#技术架构)
3. [开发环境](#开发环境)
4. [项目结构](#项目结构)
5. [核心组件](#核心组件)
6. [构建和测试](#构建和测试)
7. [发布流程](#发布流程)
8. [贡献指南](#贡献指南)

## 🎯 项目概述

### 项目目标
为 IntelliJ IDEA 用户提供一个简洁、高效的 Augment AI 使用量监控插件，支持：
- 实时使用量显示
- 智能状态管理
- 完全中文化界面
- 安全的认证存储

### 核心特性
- **状态栏集成**: 无侵入式的使用量显示
- **智能交互**: 根据状态提供相应操作
- **安全认证**: 使用 IntelliJ PasswordSafe 存储凭据
- **自动刷新**: 可配置的数据更新机制

## 🏗️ 技术架构

### 技术栈
- **语言**: Kotlin 1.9.10
- **构建工具**: Gradle 8.4 + gradle-intellij-plugin
- **HTTP 客户端**: OkHttp3 4.12.0
- **JSON 解析**: Gson 2.10.1
- **测试框架**: JUnit 4.13.2
- **平台**: IntelliJ Platform SDK

### 架构模式
- **服务导向架构 (SOA)**: 核心功能通过服务提供
- **依赖注入**: 使用 IntelliJ 的服务容器
- **观察者模式**: 状态变化通知机制
- **策略模式**: 不同状态的处理策略

### 核心设计原则
1. **单一职责**: 每个类专注于特定功能
2. **开闭原则**: 易于扩展，无需修改现有代码
3. **依赖倒置**: 依赖抽象而非具体实现
4. **接口隔离**: 小而专用的接口

## 🛠️ 开发环境

### 系统要求
- **操作系统**: Windows 10+, macOS 10.15+, Linux (Ubuntu 18.04+)
- **Java**: JDK 17 或更高版本
- **IDE**: IntelliJ IDEA 2023.1+ (推荐 Ultimate)
- **内存**: 最少 8GB RAM (推荐 16GB)

### 环境配置
```bash
# 1. 克隆项目
git clone https://github.com/akapril/augment-usage-tracker-idea.git
cd augment-usage-tracker-idea

# 2. 检查 Java 版本
java -version  # 确保是 JDK 17+

# 3. 构建项目
./gradlew build

# 4. 运行测试
./gradlew test

# 5. 启动开发环境
./gradlew runIde
```

### IDE 配置
1. 导入项目到 IntelliJ IDEA
2. 确保 Project SDK 设置为 JDK 17+
3. 启用 Kotlin 插件
4. 配置代码格式化规则

## 📁 项目结构

```
src/main/kotlin/com/augmentcode/usagetracker/
├── model/                          # 数据模型
│   ├── UsageData.kt               # 使用量数据模型
│   ├── UserInfo.kt                # 用户信息模型
│   └── ApiResponse.kt             # API 响应模型
├── service/                        # 核心服务
│   ├── AugmentService.kt          # 主要业务逻辑服务
│   ├── AuthManager.kt             # 认证管理服务
│   └── AugmentApiClient.kt        # API 客户端服务
├── ui/                            # 用户界面组件
│   ├── AugmentStatusBarWidget.kt  # 状态栏组件
│   └── AugmentStatusBarWidgetFactory.kt # 状态栏工厂
├── settings/                       # 设置相关
│   └── AugmentSettingsConfigurable.kt # 设置面板
├── util/                          # 工具类
│   └── Constants.kt               # 常量定义
└── AugmentPluginStartup.kt        # 插件启动类

src/main/resources/
├── META-INF/
│   └── plugin.xml                 # 插件配置文件
└── messages/                      # 国际化资源（预留）

src/test/kotlin/                   # 测试代码
├── service/
│   ├── AugmentServiceTest.kt
│   └── AuthManagerTest.kt
└── model/
    └── UsageDataTest.kt
```

## 🔧 核心组件

### 1. AugmentService (主服务)
**职责**: 协调各个组件，管理数据流
```kotlin
class AugmentService : Disposable {
    fun refreshData(): CompletableFuture<Boolean>
    fun getCurrentUsageData(): UsageData
    fun getCurrentUserInfo(): UserInfo?
    fun startAutoRefresh()
    fun stopAutoRefresh()
}
```

### 2. AuthManager (认证管理)
**职责**: 管理用户认证状态和凭据存储
```kotlin
class AuthManager {
    fun isAuthenticated(): Boolean
    fun areCookiesExpired(): Boolean
    fun setCookies(cookies: String)
    fun getCookies(): String?
    fun clearCredentials()
}
```

### 3. AugmentApiClient (API 客户端)
**职责**: 处理与 Augment API 的通信
```kotlin
class AugmentApiClient {
    suspend fun getUserInfo(cookies: String): ApiResponse<UserInfo>
    suspend fun getUsageData(cookies: String): ApiResponse<UsageData>
    fun testConnection(cookies: String): ApiResponse<Boolean>
}
```

### 4. AugmentStatusBarWidget (状态栏组件)
**职责**: 在状态栏显示使用量信息和处理用户交互
```kotlin
class AugmentStatusBarWidget : StatusBarWidget.TextPresentation {
    override fun getText(): String
    override fun getTooltipText(): String?
    override fun getClickConsumer(): Consumer<MouseEvent>?
}
```

## 🧪 构建和测试

### 构建命令
```bash
# 清理构建
./gradlew clean

# 编译 Kotlin 代码
./gradlew compileKotlin

# 运行测试
./gradlew test

# 构建插件
./gradlew buildPlugin

# 验证插件
./gradlew verifyPlugin

# 运行 IDEA 实例进行测试
./gradlew runIde
```

### 测试策略
1. **单元测试**: 测试各个组件的独立功能
2. **集成测试**: 测试组件间的协作
3. **UI 测试**: 测试用户界面交互
4. **性能测试**: 测试插件对 IDEA 性能的影响

### 测试覆盖率
- **目标覆盖率**: 80%+
- **核心服务**: 90%+
- **UI 组件**: 70%+
- **工具类**: 95%+

## 📦 发布流程

### 版本管理
- 遵循 [语义化版本](https://semver.org/lang/zh-CN/) 规范
- 格式: `MAJOR.MINOR.PATCH`
- 示例: `1.0.0`, `1.1.0`, `1.0.1`

### 发布步骤
1. **更新版本号**
   ```bash
   # 更新 build.gradle.kts 中的版本号
   version = "1.1.0"
   ```

2. **更新文档**
   - 更新 CHANGELOG.md
   - 更新 README.md
   - 更新 plugin.xml 描述

3. **构建和验证**
   ```bash
   ./gradlew clean buildPlugin verifyPlugin
   ```

4. **创建发布**
   - 创建 Git 标签
   - 上传到 GitHub Releases
   - 提交到 JetBrains Marketplace（可选）

### 质量检查清单
- [ ] 所有测试通过
- [ ] 插件验证通过
- [ ] 文档更新完整
- [ ] 版本号正确
- [ ] 构建产物完整

## 🤝 贡献指南

### 代码规范
1. **Kotlin 编码规范**: 遵循 [Kotlin 官方编码约定](https://kotlinlang.org/docs/coding-conventions.html)
2. **命名约定**: 
   - 类名: PascalCase
   - 函数名: camelCase
   - 常量: UPPER_SNAKE_CASE
3. **注释规范**: 使用 KDoc 格式
4. **文件组织**: 按功能模块组织文件

### 提交规范
```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型 (type)**:
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式化
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具的变动

**示例**:
```
feat(ui): 添加 Cookie 过期状态提示

- 在状态栏显示过期警告图标
- 添加过期处理对话框
- 更新工具提示文本

Closes #123
```

### 开发流程
1. **Fork 项目** 到个人账户
2. **创建功能分支** `git checkout -b feature/new-feature`
3. **开发和测试** 确保代码质量
4. **提交更改** 遵循提交规范
5. **创建 Pull Request** 详细描述更改内容
6. **代码审查** 响应审查意见
7. **合并代码** 通过审查后合并

### 问题报告
使用 GitHub Issues 报告问题，请包含：
- **环境信息**: OS, IDEA 版本, 插件版本
- **重现步骤**: 详细的操作步骤
- **期望行为**: 应该发生什么
- **实际行为**: 实际发生了什么
- **日志信息**: 相关的错误日志

## 📚 参考资料

### 官方文档
- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)
- [Kotlin 语言参考](https://kotlinlang.org/docs/)
- [Gradle 构建工具](https://gradle.org/docs/)

### 相关项目
- [gradle-intellij-plugin](https://github.com/JetBrains/gradle-intellij-plugin)
- [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)

### 社区资源
- [JetBrains 插件开发论坛](https://intellij-support.jetbrains.com/hc/en-us/community/topics/200366979-IntelliJ-IDEA-Open-API-and-Plugin-Development)
- [Kotlin 社区](https://kotlinlang.org/community/)

---

**维护者**: akapril  
**最后更新**: 2024-12-21  
**文档版本**: 1.0.0
