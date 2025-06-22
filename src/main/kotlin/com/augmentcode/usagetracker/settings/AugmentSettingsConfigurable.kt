package com.augmentcode.usagetracker.settings

import com.augmentcode.usagetracker.service.AugmentService
import com.augmentcode.usagetracker.service.AuthManager
import com.augmentcode.usagetracker.util.Constants
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
            toolTipText = "刷新间隔（秒）（5-300）"
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
        
        buttonPanel.add(testConnectionButton)
        buttonPanel.add(refreshDataButton)
        buttonPanel.add(clearCredentialsButton)
        
        return buttonPanel
    }
    
    private fun loadSettings() {
        // Load authentication status
        val isAuthenticated = authManager.isAuthenticated()
        val cookies = if (isAuthenticated) authManager.getCookies() else ""
        
        cookiesField?.text = cookies ?: ""
        refreshIntervalField?.text = Constants.DEFAULT_REFRESH_INTERVAL.toString()
        enabledCheckBox?.isSelected = augmentService.isEnabled()
        showInStatusBarCheckBox?.isSelected = true // Default to true
        
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
        
        val currentRefreshInterval = refreshIntervalField?.text?.toIntOrNull() ?: Constants.DEFAULT_REFRESH_INTERVAL
        val currentEnabled = enabledCheckBox?.isSelected ?: true
        
        return currentCookies != storedCookies ||
                currentRefreshInterval != Constants.DEFAULT_REFRESH_INTERVAL ||
                currentEnabled != augmentService.isEnabled()
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
            if (refreshInterval != null && refreshInterval in 5..300) {
                augmentService.setRefreshInterval(refreshInterval)
            }
            
            // Apply enabled state
            val enabled = enabledCheckBox?.isSelected ?: true
            augmentService.setEnabled(enabled)
            
            updateStatusLabel()
            
            Messages.showInfoMessage(
                mainPanel,
                "✅ 设置保存成功！",
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
}
