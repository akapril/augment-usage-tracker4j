# 🚀 发布说明 / Release Notes

## Augment Usage Tracker for IntelliJ IDEA v1.0.0

> **发布日期 / Release Date**: 2024-12-21  
> **状态 / Status**: ✅ 正式发布 / Official Release

## 📦 下载 / Download

**插件文件 / Plugin File**: `augment-usage-tracker-idea-1.0.0.zip` (3.0MB)

## 🎉 新功能 / New Features

### 📊 状态栏监控 / Status Bar Monitoring
- 在 IntelliJ IDEA 状态栏实时显示 Augment 使用量
- 格式：`用户名: 150/1000 ●`
- 支持点击查看详细信息
- 认证状态可视化指示

### 🔐 安全认证 / Secure Authentication
- 支持浏览器 Cookie 认证
- 使用 IntelliJ PasswordSafe 安全存储凭据
- 自动检测 Cookie 过期状态
- 支持认证状态实时更新

### ⚙️ 设置集成 / Settings Integration
- 完整集成到 IDEA 设置面板
- 路径：`File → Settings → Tools → Augment Usage Tracker`
- 内置连接测试功能
- 可配置刷新间隔（5-300秒）

### 🔄 自动刷新 / Auto Refresh
- 智能的后台数据刷新机制
- 可配置的刷新间隔
- 异步网络请求，不阻塞 UI
- 自动错误重试和恢复

## 🛠️ 技术特性 / Technical Features

### 🏗️ 架构设计 / Architecture
- 模块化设计，关注点分离
- 服务导向架构 (SOA)
- 依赖注入和生命周期管理
- 异步编程和线程安全

### 🔧 开发技术 / Development Stack
- **语言 / Language**: Kotlin 1.9.10
- **构建 / Build**: Gradle 8.4 + gradle-intellij-plugin
- **HTTP 客户端 / HTTP Client**: OkHttp3 4.12.0
- **JSON 处理 / JSON**: Gson 2.10.1
- **测试框架 / Testing**: JUnit 4.13.2

### 🌍 兼容性 / Compatibility
- **IntelliJ IDEA**: 2023.1+ (Build 231-241.*)
- **Java**: JDK 17 或更高版本
- **操作系统 / OS**: Windows, macOS, Linux
- **网络 / Network**: 需要访问 app.augmentcode.com

## 📋 安装指南 / Installation Guide

### 1. 系统要求检查 / System Requirements Check
```bash
# 检查 Java 版本 / Check Java version
java -version  # 需要 JDK 17+

# 检查 IDEA 版本 / Check IDEA version
# 需要 IntelliJ IDEA 2023.1 或更高版本
```

### 2. 插件安装 / Plugin Installation
1. 下载 `augment-usage-tracker-idea-1.0.0.zip`
2. 打开 IntelliJ IDEA
3. 进入 `File` → `Settings` → `Plugins`
4. 点击齿轮图标 → `Install Plugin from Disk...`
5. 选择下载的 ZIP 文件
6. 重启 IDEA

### 3. 插件配置 / Plugin Configuration
1. 进入 `File` → `Settings` → `Tools` → `Augment Usage Tracker`
2. 输入 Augment Cookie 信息：
   - 登录 [app.augmentcode.com](https://app.augmentcode.com)
   - 按 F12 打开开发者工具
   - 复制 `_session` cookie 值
3. 点击 "Test Connection" 验证设置
4. 配置刷新间隔（推荐 30 秒）

## 🎯 使用说明 / Usage Instructions

### 📊 状态栏显示 / Status Bar Display
安装配置完成后，状态栏将显示：
- **格式 / Format**: `用户名: 当前使用量/总限额 ●`
- **示例 / Example**: `john: 150/1000 ●`
- **指示器 / Indicator**: 
  - ● (实心圆) = 已认证并获取到数据
  - ○ (空心圆) = 未认证或无数据

### 🖱️ 交互操作 / Interactive Features
- **左键点击 / Left Click**: 显示详细使用统计
- **Ctrl + 左键 / Ctrl + Left Click**: 手动刷新数据
- **右键点击 / Right Click**: 显示详细信息

### 📈 详细信息 / Detailed Information
点击状态栏项目可查看：
- 用户信息（邮箱、姓名、计划类型）
- 使用统计（当前用量、限额、百分比、剩余额度）
- 最后更新时间
- 认证状态摘要

## 🔧 故障排除 / Troubleshooting

### ❌ 常见问题 / Common Issues

**1. 状态栏显示 "Not Authenticated"**
- 检查 Cookie 是否正确配置
- 确认 Cookie 未过期（有效期约20小时）
- 重新获取并设置 Cookie

**2. 连接测试失败**
- 检查网络连接
- 确认可以访问 app.augmentcode.com
- 验证 Cookie 格式是否正确

**3. 数据不更新**
- 检查刷新间隔设置
- 手动点击刷新按钮测试
- 查看 IDEA 日志文件中的错误信息

**4. 插件无法加载**
- 确认 IDEA 版本 ≥ 2023.1
- 检查 Java 版本 ≥ 17
- 重启 IDEA 后重试

### 📝 日志查看 / Log Viewing
如需查看详细日志：
1. 进入 `Help` → `Show Log in Explorer/Finder`
2. 搜索包含 "AugmentUsageTracker" 的日志条目
3. 查看错误信息和调试详情

## 🔄 更新计划 / Update Roadmap

### v1.1.0 计划功能 / Planned Features
- [ ] 插件市场发布
- [ ] 更多认证方式支持
- [ ] 使用趋势图表
- [ ] 自定义显示格式
- [ ] 多语言界面支持

### 🐛 已知问题 / Known Issues
- 构建过程中的 searchable options 警告（不影响功能）
- 某些网络环境下的连接延迟

## 📞 支持与反馈 / Support & Feedback

### 🔗 相关链接 / Links
- **源代码 / Source**: [GitHub Repository](https://github.com/akapril/augment-usage-tracker-idea)
- **问题反馈 / Issues**: [GitHub Issues](https://github.com/akapril/augment-usage-tracker-idea/issues)
- **文档 / Documentation**: [README.md](README.md)
- **更新日志 / Changelog**: [CHANGELOG.md](CHANGELOG.md)

### 👨‍💻 开发者 / Developer
**akapril**
- Email: wiq@live.com
- GitHub: [@akapril](https://github.com/akapril)

## 📄 许可证 / License

MIT License - 详见 [LICENSE](LICENSE) 文件

---

**感谢使用 Augment Usage Tracker for IntelliJ IDEA！**  
**Thank you for using Augment Usage Tracker for IntelliJ IDEA!**

如果这个插件对您有帮助，请考虑给项目一个 ⭐ Star！  
If this plugin helps you, please consider giving the project a ⭐ Star!
