package com.augmentcode.usagetracker.ui

import com.augmentcode.usagetracker.model.UsageData
import com.augmentcode.usagetracker.model.UserInfo
import com.augmentcode.usagetracker.service.AugmentService
import com.augmentcode.usagetracker.service.AuthManager
import com.augmentcode.usagetracker.util.Constants
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import java.time.format.DateTimeFormatter

/**
 * Status bar widget for displaying Augment usage information
 */
class AugmentStatusBarWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {
    
    companion object {
        private val LOG = Logger.getInstance(AugmentStatusBarWidget::class.java)
    }
    
    private val augmentService: AugmentService by lazy { AugmentService.getInstance() }
    private val authManager: AuthManager by lazy { AuthManager.getInstance() }
    
    private var currentText = Constants.STATUS_NOT_AUTHENTICATED
    private var currentTooltip = "Augment Usage Tracker - Click to configure"
    
    // Listeners for service updates
    private val dataChangeListener: (UsageData) -> Unit = { data -> updateDisplay(data) }
    private val userInfoChangeListener: (UserInfo?) -> Unit = { userInfo -> updateUserInfo(userInfo) }
    private val authStateChangeListener: (Boolean) -> Unit = { isAuth -> updateAuthState(isAuth) }
    
    init {
        LOG.info("AugmentStatusBarWidget initialized for project: ${project.name}")
        
        // Register listeners
        augmentService.addDataChangeListener(dataChangeListener)
        augmentService.addUserInfoChangeListener(userInfoChangeListener)
        authManager.addAuthStateChangeListener(authStateChangeListener)
        
        // Initial display update
        updateInitialDisplay()
    }
    
    override fun ID(): String = Constants.WIDGET_ID
    
    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this
    
    override fun install(statusBar: com.intellij.openapi.wm.StatusBar) {
        LOG.debug("Widget installed in status bar")
    }
    
    override fun dispose() {
        LOG.info("Disposing AugmentStatusBarWidget")
        
        // Remove listeners
        augmentService.removeDataChangeListener(dataChangeListener)
        augmentService.removeUserInfoChangeListener(userInfoChangeListener)
        authManager.removeAuthStateChangeListener(authStateChangeListener)
    }
    
    // TextPresentation implementation
    override fun getText(): String = currentText
    
    override fun getTooltipText(): String = currentTooltip
    
    override fun getClickConsumer(): Consumer<MouseEvent>? {
        return Consumer { mouseEvent ->
            handleClick(mouseEvent)
        }
    }

    override fun getAlignment(): Float = 0.5f
    
    /**
     * Update display with new usage data
     */
    private fun updateDisplay(data: UsageData) {
        ApplicationManager.getApplication().invokeLater {
            if (authManager.isAuthenticated()) {
                val userInfo = augmentService.getCurrentUserInfo()
                val displayName = userInfo?.email?.substringBefore("@") ?: "Augment"

                // Check if cookies are expired
                val isExpired = authManager.areCookiesExpired()
                val statusIndicator = if (isExpired) "⚠" else "●"

                currentText = "$displayName: ${data.totalUsage}/${data.usageLimit} $statusIndicator"
                
                currentTooltip = buildString {
                    append("Augment 使用量监控\n\n")

                    // Authentication status
                    if (isExpired) {
                        append("⚠️ 认证状态：Cookie 已过期\n")
                        append("请在设置中更新您的 Cookie\n\n")
                    } else {
                        append("✅ 认证状态：正常\n\n")
                    }

                    if (userInfo != null) {
                        append("用户信息：\n")
                        userInfo.email?.let { append("• 邮箱：$it\n") }
                        userInfo.name?.let { append("• 姓名：$it\n") }
                        userInfo.plan?.let { append("• 计划：$it\n") }
                        append("\n")
                    }

                    append("使用情况：\n")
                    append("• 当前用量：${data.totalUsage} 积分\n")
                    append("• 总限额：${data.usageLimit} 积分\n")
                    append("• 使用率：${data.getUsagePercentage()}%\n")
                    append("• 剩余额度：${data.getRemainingUsage()} 积分\n")

                    data.lastUpdate?.let {
                        append("• 最后更新：${it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n")
                    }

                    append("\n数据来源：Augment API 实时数据\n")
                    if (isExpired) {
                        append("点击打开设置更新 Cookie")
                    } else {
                        append("点击查看详细信息或刷新数据")
                    }
                }
                
                LOG.debug("Display updated: ${data.totalUsage}/${data.usageLimit}")
            } else {
                updateNotAuthenticatedDisplay()
            }

            // Update status bar display
            updateStatusBar()
        }
    }
    
    /**
     * Update user info display
     */
    private fun updateUserInfo(userInfo: UserInfo?) {
        // User info changes will trigger display update through data change listener
        LOG.debug("User info updated: ${userInfo?.email}")
    }
    
    /**
     * Update authentication state
     */
    private fun updateAuthState(isAuthenticated: Boolean) {
        ApplicationManager.getApplication().invokeLater {
            if (isAuthenticated) {
                currentText = Constants.STATUS_LOADING
                currentTooltip = "Augment 使用量监控 - 正在加载数据..."

                // Trigger data refresh
                augmentService.refreshData()
            } else {
                updateNotAuthenticatedDisplay()
            }

            // Update status bar display
            updateStatusBar()

            LOG.debug("Auth state updated: $isAuthenticated")
        }
    }
    
    /**
     * Update display for not authenticated state
     */
    private fun updateNotAuthenticatedDisplay() {
        currentText = "Augment: ${Constants.STATUS_NOT_AUTHENTICATED}"
        currentTooltip = buildString {
            append("Augment 使用量监控\n\n")
            append("状态：未认证\n")
            append("请在设置中配置您的 Augment Cookie\n\n")
            append("点击打开设置")
        }

        // Update status bar display
        updateStatusBar()
    }
    
    /**
     * Update initial display
     */
    private fun updateInitialDisplay() {
        if (authManager.isAuthenticated()) {
            val currentData = augmentService.getCurrentUsageData()
            if (currentData.totalUsage > 0 || currentData.usageLimit > 0) {
                updateDisplay(currentData)
            } else {
                currentText = Constants.STATUS_LOADING
                currentTooltip = "Augment 使用量监控 - 正在加载数据..."
                augmentService.refreshData()
            }
        } else {
            updateNotAuthenticatedDisplay()
        }
    }

    /**
     * Update status bar display
     */
    private fun updateStatusBar() {
        try {
            val statusBar = WindowManager.getInstance().getStatusBar(project)
            statusBar?.updateWidget(Constants.WIDGET_ID)
        } catch (e: Exception) {
            LOG.error("Error updating status bar", e)
        }
    }

    /**
     * Handle widget click
     */
    private fun handleClick(mouseEvent: MouseEvent) {
        try {
            if (!authManager.isAuthenticated()) {
                // Open settings if not authenticated
                showSettingsDialog()
            } else if (authManager.areCookiesExpired()) {
                // Open settings if cookies are expired
                showExpiredCookieDialog()
            } else {
                // Show detailed information or refresh data
                when (mouseEvent.button) {
                    MouseEvent.BUTTON1 -> { // Left click
                        if (mouseEvent.isControlDown()) {
                            // Ctrl+Click: Manual refresh
                            performManualRefresh()
                        } else {
                            // Regular click: Show details
                            showUsageDetails()
                        }
                    }
                    MouseEvent.BUTTON3 -> { // Right click
                        showUsageDetails()
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error("Error handling widget click", e)
        }
    }
    
    /**
     * Show settings dialog
     */
    private fun showSettingsDialog() {
        val result = Messages.showYesNoDialog(
            project,
            "请配置您的 Augment Cookie 以开始监控使用量。\n\n" +
                    "获取 Cookie 方法：\n" +
                    "1. 登录 app.augmentcode.com\n" +
                    "2. 按 F12 打开开发者工具\n" +
                    "3. 复制 _session Cookie 值\n\n" +
                    "是否现在打开设置页面？",
            "配置 Augment Cookie",
            "打开设置",
            "取消",
            Messages.getQuestionIcon()
        )

        if (result == Messages.YES) {
            openSettingsPage()
        }
    }

    /**
     * Show expired cookie dialog
     */
    private fun showExpiredCookieDialog() {
        val result = Messages.showYesNoDialog(
            project,
            "⚠️ 您的 Augment Cookie 已过期！\n\n" +
                    "请重新获取 Cookie 并更新设置：\n\n" +
                    "获取方法：\n" +
                    "1. 登录 app.augmentcode.com\n" +
                    "2. 按 F12 打开开发者工具\n" +
                    "3. 复制 _session Cookie 值\n\n" +
                    "是否现在打开设置页面更新 Cookie？",
            "Cookie 已过期",
            "打开设置",
            "取消",
            Messages.getWarningIcon()
        )

        if (result == Messages.YES) {
            openSettingsPage()
        }
    }
    
    /**
     * Show usage details
     */
    private fun showUsageDetails() {
        val data = augmentService.getCurrentUsageData()
        val userInfo = augmentService.getCurrentUserInfo()
        
        val details = buildString {
            append("Augment 使用详情\n\n")

            if (userInfo != null) {
                append("用户信息：\n")
                userInfo.email?.let { append("邮箱：$it\n") }
                userInfo.name?.let { append("姓名：$it\n") }
                userInfo.plan?.let { append("计划：$it\n") }
                append("\n")
            }

            append("使用情况：\n")
            append("当前用量：${data.totalUsage} 积分\n")
            append("使用限额：${data.usageLimit} 积分\n")
            append("使用百分比：${data.getUsagePercentage()}%\n")
            append("剩余积分：${data.getRemainingUsage()}\n")

            if (data.dailyUsage > 0) {
                append("日使用量：${data.dailyUsage} 积分\n")
            }

            data.lastUpdate?.let {
                append("最后更新：${it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}\n")
            }

            append("\n认证状态：\n")
            if (authManager.areCookiesExpired()) {
                append("⚠️ Cookie 已过期 - 请在设置中更新")
            } else {
                append("✅ 已认证 - Cookie 有效")
            }
        }
        
        // Show dialog with refresh button for normal state
        val result = Messages.showYesNoDialog(
            project,
            details + "\n\n是否立即刷新数据？",
            "Augment 使用详情",
            "刷新数据",
            "关闭",
            Messages.getInformationIcon()
        )

        if (result == Messages.YES) {
            performManualRefresh()
        }
    }
    
    /**
     * Perform manual refresh
     */
    private fun performManualRefresh() {
        currentText = Constants.STATUS_LOADING
        currentTooltip = "正在刷新数据..."

        augmentService.refreshData().thenAccept { success ->
            ApplicationManager.getApplication().invokeLater {
                if (success) {
                    Messages.showInfoMessage(project, "数据刷新成功！", "Augment 使用量监控")
                } else {
                    Messages.showErrorDialog(project, "数据刷新失败。请检查您的网络连接和认证状态。", "Augment 使用量监控")
                }
            }
        }
    }

    /**
     * Open settings page
     */
    private fun openSettingsPage() {
        ShowSettingsUtil.getInstance().showSettingsDialog(
            project,
            "Augment 使用量监控"
        )
    }
}
