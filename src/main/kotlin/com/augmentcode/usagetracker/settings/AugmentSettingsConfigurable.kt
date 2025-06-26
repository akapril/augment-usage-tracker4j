package com.augmentcode.usagetracker.settings

import com.augmentcode.usagetracker.service.AugmentService
import com.augmentcode.usagetracker.service.AuthManager
import com.augmentcode.usagetracker.service.ConfigService
import com.augmentcode.usagetracker.util.Constants
import com.augmentcode.usagetracker.util.LogCollector
import com.augmentcode.usagetracker.util.StatusBarDiagnostics
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

/**
 * Settings configurable for Augment Usage Tracker
 */
class AugmentSettingsConfigurable : Configurable {
    
    private val authManager: AuthManager by lazy { AuthManager.getInstance() }
    private val augmentService: AugmentService by lazy { AugmentService.getInstance() }
    private val configService: ConfigService by lazy { ConfigService.getInstance() }
    
    // UI Components
    private var mainPanel: JPanel? = null
    private var cookiesField: JBPasswordField? = null
    private var refreshIntervalField: JBTextField? = null
    private var enabledCheckBox: JBCheckBox? = null
    private var showInStatusBarCheckBox: JBCheckBox? = null
    private var statusLabel: JBLabel? = null
    
    override fun getDisplayName(): String = "Augment 使用量监控"
    
    override fun createComponent(): JComponent? {
        mainPanel = JPanel(BorderLayout())
        
        // Create form panel
        val formPanel = createFormPanel()
        mainPanel!!.add(formPanel, BorderLayout.CENTER)
        
        // Create button panel
        val buttonPanel = createButtonPanel()
        mainPanel!!.add(buttonPanel, BorderLayout.SOUTH)
        
        // Load current settings
        loadSettings()
        
        return mainPanel
    }
    
    private fun createFormPanel(): JPanel {
        cookiesField = JBPasswordField().apply {
            columns = 50
            toolTipText = "输入您的 Augment 浏览器 Cookie（例如：_session=...）"
        }

        refreshIntervalField = JBTextField().apply {
            columns = 10
            toolTipText = "刷新间隔（秒）（5-3600，推荐60-300）"

            // 保存原始背景色
            val originalBackground = background
            val errorBackground = java.awt.Color(255, 240, 240) // 淡红色背景

            // 添加实时验证
            document.addDocumentListener(object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = validateRefreshInterval()
                override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = validateRefreshInterval()
                override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = validateRefreshInterval()

                private fun validateRefreshInterval() {
                    val value = text.toIntOrNull()
                    when {
                        value == null && text.isNotEmpty() -> {
                            toolTipText = "❌ 请输入有效的数字"
                            background = errorBackground
                        }
                        value != null && (value < 5 || value > 3600) -> {
                            toolTipText = "❌ 刷新间隔必须在 5-3600 秒之间（当前：${value}秒）"
                            background = errorBackground
                        }
                        value != null && value in 5..3600 -> {
                            val recommendation = when {
                                value < 30 -> "⚠️ 过于频繁，可能影响性能"
                                value in 30..300 -> "✅ 推荐范围，平衡性能和实时性"
                                value in 301..1800 -> "✅ 省电模式，适合长时间使用"
                                else -> "✅ 最低频率，最大化省电"
                            }
                            toolTipText = "刷新间隔：${value}秒 - $recommendation"
                            background = originalBackground // 恢复原始背景色
                        }
                        else -> {
                            toolTipText = "刷新间隔（秒）（5-3600，推荐60-300）"
                            background = originalBackground // 恢复原始背景色
                        }
                    }
                    // 强制重绘组件以确保颜色更新
                    repaint()
                }
            })
        }

        enabledCheckBox = JBCheckBox("启用 Augment 使用量监控").apply {
            toolTipText = "启用或禁用使用量监控"
        }

        showInStatusBarCheckBox = JBCheckBox("在状态栏显示").apply {
            toolTipText = "在状态栏显示使用量信息"
        }

        statusLabel = JBLabel("状态：未配置").apply {
            foreground = JBUI.CurrentTheme.Label.disabledForeground()
        }
        
        return FormBuilder.createFormBuilder()
            .addComponent(JBLabel("<html><b>Augment 使用量监控设置</b></html>"))
            .addVerticalGap(10)

            .addLabeledComponent("启用监控：", enabledCheckBox!!)
            .addLabeledComponent("在状态栏显示：", showInStatusBarCheckBox!!)
            .addVerticalGap(10)

            .addComponent(JBLabel("<html><b>身份认证</b></html>"))
            .addLabeledComponent("Cookie：", cookiesField!!)
            .addComponent(JBLabel("<html><i>输入您的 Augment 浏览器会话 Cookie。<br>" +
                    "格式：_session=eyJ... 或完整的 Cookie 字符串</i></html>"))
            .addVerticalGap(10)

            .addComponent(JBLabel("<html><b>刷新设置</b></html>"))
            .addLabeledComponent("刷新间隔（秒）：", refreshIntervalField!!)
            .addComponent(JBLabel("<html><i>多久刷新一次使用量数据（5-300 秒）</i></html>"))
            .addVerticalGap(10)

            .addComponent(JBLabel("<html><b>状态</b></html>"))
            .addComponent(statusLabel!!)

            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
    
    private fun createButtonPanel(): JPanel {
        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        
        val testConnectionButton = JButton("测试连接").apply {
            addActionListener { testConnection() }
            toolTipText = "测试与 Augment API 的连接"
        }

        val refreshDataButton = JButton("刷新数据").apply {
            addActionListener { refreshData() }
            toolTipText = "手动刷新使用量数据"
        }

        val clearCredentialsButton = JButton("清除凭据").apply {
            addActionListener { clearCredentials() }
            toolTipText = "清除已存储的认证凭据"
        }

        val diagnosticsButton = JButton("诊断状态栏").apply {
            addActionListener { runStatusBarDiagnostics() }
            toolTipText = "诊断并修复状态栏显示问题"
        }

        val collectLogsButton = JButton("收集调试日志").apply {
            addActionListener { collectDebugLogs() }
            toolTipText = "收集详细的调试信息用于问题排查"
        }

        buttonPanel.add(testConnectionButton)
        buttonPanel.add(refreshDataButton)
        buttonPanel.add(clearCredentialsButton)
        buttonPanel.add(diagnosticsButton)
        buttonPanel.add(collectLogsButton)
        
        return buttonPanel
    }
    
    private fun loadSettings() {
        // Load authentication status
        val isAuthenticated = authManager.isAuthenticated()
        val cookies = if (isAuthenticated) authManager.getCookies() else ""

        // Load settings from ConfigService
        cookiesField?.text = cookies ?: ""
        refreshIntervalField?.text = configService.getRefreshInterval().toString()
        enabledCheckBox?.isSelected = configService.isEnabled()
        showInStatusBarCheckBox?.isSelected = configService.isShowInStatusBar()

        updateStatusLabel()
    }
    
    private fun updateStatusLabel() {
        val isAuthenticated = authManager.isAuthenticated()
        val statusText = if (isAuthenticated) {
            val lastUpdate = authManager.getLastUpdateTime()
            val expired = authManager.areCookiesExpired()

            when {
                expired -> "状态：已认证（已过期 - 请更新 Cookie）"
                lastUpdate != null -> "状态：已认证（最后更新：$lastUpdate）"
                else -> "状态：已认证"
            }
        } else {
            "状态：未认证"
        }
        
        statusLabel?.text = statusText
        statusLabel?.foreground = if (isAuthenticated && !authManager.areCookiesExpired()) {
            JBUI.CurrentTheme.Label.foreground()
        } else {
            JBUI.CurrentTheme.Label.disabledForeground()
        }
    }
    
    private fun testConnection() {
        val cookies = cookiesField?.password?.let { String(it) }
        
        if (cookies.isNullOrBlank()) {
            Messages.showWarningDialog(
                "请先输入您的 Augment Cookie。",
                "测试连接"
            )
            return
        }
        
        // Temporarily set cookies for testing
        val originalCookies = authManager.getCookies()
        authManager.setCookies(cookies)
        
        try {
            val apiClient = com.augmentcode.usagetracker.service.AugmentApiClient()
            val result = apiClient.testConnection()
            
            if (result.success) {
                Messages.showInfoMessage(
                    mainPanel,
                    "✅ 连接成功！\n身份认证正常工作。",
                    "测试连接"
                )
                updateStatusLabel()
            } else {
                Messages.showErrorDialog(
                    mainPanel,
                    "❌ 连接失败：\n${result.error}",
                    "测试连接"
                )
                // Restore original cookies if test failed
                authManager.setCookies(originalCookies)
            }
        } catch (e: Exception) {
            Messages.showErrorDialog(
                mainPanel,
                "❌ 连接错误：\n${e.message}",
                "测试连接"
            )
            // Restore original cookies if test failed
            authManager.setCookies(originalCookies)
        }
    }
    
    private fun refreshData() {
        if (!authManager.isAuthenticated()) {
            Messages.showWarningDialog(
                "请先配置身份认证。",
                "刷新数据"
            )
            return
        }
        
        augmentService.refreshData().thenAccept { success ->
            SwingUtilities.invokeLater {
                if (success) {
                    val data = augmentService.getCurrentUsageData()
                    Messages.showInfoMessage(
                        mainPanel,
                        "✅ 数据刷新成功！\n\n" +
                                "当前用量：${data.totalUsage}/${data.usageLimit} 积分\n" +
                                "使用百分比：${data.getUsagePercentage()}%",
                        "刷新数据"
                    )
                } else {
                    Messages.showErrorDialog(
                        mainPanel,
                        "❌ 数据刷新失败。\n请检查您的网络连接和认证状态。",
                        "刷新数据"
                    )
                }
            }
        }
    }
    
    private fun clearCredentials() {
        val result = Messages.showYesNoDialog(
            "您确定要清除所有已存储的凭据吗？\n这将使您从 Augment 注销。",
            "清除凭据",
            Messages.getQuestionIcon()
        )

        if (result == Messages.YES) {
            authManager.clearCredentials()
            cookiesField?.text = ""
            updateStatusLabel()

            Messages.showInfoMessage(
                mainPanel,
                "✅ 凭据清除成功。",
                "清除凭据"
            )
        }
    }
    
    override fun isModified(): Boolean {
        val currentCookies = cookiesField?.password?.let { String(it) } ?: ""
        val storedCookies = authManager.getCookies() ?: ""

        val currentRefreshInterval = refreshIntervalField?.text?.toIntOrNull() ?: configService.getRefreshInterval()
        val currentEnabled = enabledCheckBox?.isSelected ?: configService.isEnabled()
        val currentShowInStatusBar = showInStatusBarCheckBox?.isSelected ?: configService.isShowInStatusBar()

        return currentCookies != storedCookies ||
                currentRefreshInterval != configService.getRefreshInterval() ||
                currentEnabled != configService.isEnabled() ||
                currentShowInStatusBar != configService.isShowInStatusBar()
    }
    
    override fun apply() {
        try {
            // Apply cookies
            val cookies = cookiesField?.password?.let { String(it) }
            if (!cookies.isNullOrBlank()) {
                authManager.setCookies(cookies)
            }

            // Apply refresh interval
            val refreshInterval = refreshIntervalField?.text?.toIntOrNull()
            if (refreshInterval != null && refreshInterval in 5..3600) { // 扩展到 3600 秒（1小时）
                augmentService.setRefreshInterval(refreshInterval)
                // ConfigService.setRefreshInterval is called inside AugmentService.setRefreshInterval
            } else if (refreshInterval != null) {
                // 给用户明确的错误提示
                throw IllegalArgumentException("刷新间隔必须在 5-3600 秒之间（当前值：$refreshInterval 秒）")
            }

            // Apply enabled state
            val enabled = enabledCheckBox?.isSelected ?: true
            augmentService.setEnabled(enabled)
            // ConfigService.setEnabled is called inside AugmentService.setEnabled

            // Apply show in status bar setting
            val showInStatusBar = showInStatusBarCheckBox?.isSelected ?: true
            configService.setShowInStatusBar(showInStatusBar)

            updateStatusLabel()

            Messages.showInfoMessage(
                mainPanel,
                "✅ 设置保存成功！\n\n刷新间隔: ${refreshInterval ?: "未更改"}秒\n启用状态: ${if (enabled) "已启用" else "已禁用"}\n状态栏显示: ${if (showInStatusBar) "已启用" else "已禁用"}",
                "设置"
            )
        } catch (e: Exception) {
            Messages.showErrorDialog(
                mainPanel,
                "❌ 保存设置时出错：\n${e.message}",
                "设置"
            )
        }
    }
    
    override fun reset() {
        loadSettings()
    }
    
    override fun disposeUIResources() {
        mainPanel = null
        cookiesField = null
        refreshIntervalField = null
        enabledCheckBox = null
        showInStatusBarCheckBox = null
        statusLabel = null
    }

    /**
     * Run status bar diagnostics
     * 运行状态栏诊断
     */
    private fun runStatusBarDiagnostics() {
        try {
            // Get current project
            val projectManager = com.intellij.openapi.project.ProjectManager.getInstance()
            val openProjects = projectManager.openProjects

            if (openProjects.isEmpty()) {
                Messages.showWarningDialog(
                    "没有打开的项目。请先打开一个项目再运行诊断。",
                    "状态栏诊断"
                )
                return
            }

            val project = openProjects.first() // Use the first open project

            // Run diagnostics
            val diagnosticReport = StatusBarDiagnostics.getDiagnosticReport(project)

            // Try to force enable the widget
            val forceEnableSuccess = StatusBarDiagnostics.forceEnableWidget(project)

            val message = buildString {
                append(diagnosticReport)
                append("\n\n=== 修复尝试结果 ===\n")
                if (forceEnableSuccess) {
                    append("✅ 已尝试强制启用状态栏组件\n")
                    append("请检查状态栏是否现在显示了使用量信息。\n")
                    append("如果仍未显示，请重启 IDE。")
                } else {
                    append("❌ 无法自动修复状态栏组件\n")
                    append("请按照上述建议手动解决问题。")
                }
            }

            // Show diagnostic results in a large dialog
            val dialog = object : com.intellij.openapi.ui.DialogWrapper(project) {
                init {
                    title = "状态栏诊断报告"
                    init()
                }

                override fun createCenterPanel(): javax.swing.JComponent {
                    val textArea = javax.swing.JTextArea(message).apply {
                        isEditable = false
                        lineWrap = true
                        wrapStyleWord = true
                        font = font.deriveFont(12f)
                        rows = 20
                        columns = 60
                    }

                    return javax.swing.JScrollPane(textArea).apply {
                        preferredSize = java.awt.Dimension(700, 500)
                    }
                }
            }

            dialog.show()

        } catch (e: Exception) {
            Messages.showErrorDialog(
                "运行状态栏诊断时发生错误：${e.message}",
                "诊断失败"
            )
        }
    }

    /**
     * Collect debug logs for troubleshooting
     * 收集调试日志用于故障排除
     */
    private fun collectDebugLogs() {
        try {
            val debugInfo = LogCollector.collectDebugInfo()

            // Show debug info in a dialog
            val dialog = object : com.intellij.openapi.ui.DialogWrapper(null) {
                init {
                    title = "调试信息收集"
                    init()
                }

                override fun createCenterPanel(): javax.swing.JComponent {
                    val textArea = javax.swing.JTextArea(debugInfo).apply {
                        isEditable = false
                        lineWrap = true
                        wrapStyleWord = true
                        font = font.deriveFont(11f)
                        rows = 25
                        columns = 80
                    }

                    return javax.swing.JScrollPane(textArea).apply {
                        preferredSize = java.awt.Dimension(800, 600)
                    }
                }

                override fun createActions(): Array<javax.swing.Action> {
                    return arrayOf(
                        object : com.intellij.openapi.ui.DialogWrapper.DialogWrapperAction("保存到文件") {
                            override fun doAction(e: java.awt.event.ActionEvent?) {
                                val filePath = LogCollector.saveDebugInfoToFile()
                                if (filePath != null) {
                                    Messages.showInfoMessage(
                                        "调试信息已保存到：\n$filePath\n\n请将此文件发送给技术支持。",
                                        "保存成功"
                                    )
                                } else {
                                    Messages.showErrorDialog(
                                        "保存调试信息失败。",
                                        "保存失败"
                                    )
                                }
                            }
                        },
                        cancelAction
                    )
                }
            }

            dialog.show()

        } catch (e: Exception) {
            Messages.showErrorDialog(
                "收集调试日志时发生错误：${e.message}",
                "收集失败"
            )
        }
    }
}
