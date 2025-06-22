package com.augmentcode.usagetracker.util

import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages as IntellijMessages
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JPanel
import java.awt.BorderLayout

/**
 * Custom dialog for displaying large text content
 * 用于显示大量文本内容的自定义对话框
 */
private class LargeTextDialog(
    project: Project?,
    title: String,
    private val content: String,
    private val showRefreshButton: Boolean = false,
    private val showOpenSettingsButton: Boolean = false,
    private val showBothButtons: Boolean = false
) : DialogWrapper(project) {

    private var refreshRequested = false
    private var openSettingsRequested = false

    init {
        this.title = title
        init()
    }

    override fun createCenterPanel(): JComponent {
        val textArea = JBTextArea().apply {
            text = content
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = font.deriveFont(13f) // Slightly larger font
        }

        val scrollPane = JBScrollPane(textArea as JComponent).apply {
            preferredSize = Dimension(600, 450) // Even larger to accommodate cookie instructions
            verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        }

        val panel = JPanel(BorderLayout()).apply {
            add(scrollPane, BorderLayout.CENTER)
        }

        return panel
    }

    override fun createActions(): Array<javax.swing.Action> {
        return when {
            showBothButtons -> {
                arrayOf(
                    object : DialogWrapperAction(Messages.BUTTON_REFRESH_DATA) {
                        override fun doAction(e: java.awt.event.ActionEvent?) {
                            refreshRequested = true
                            close(OK_EXIT_CODE)
                        }
                    },
                    object : DialogWrapperAction(Messages.BUTTON_OPEN_SETTINGS) {
                        override fun doAction(e: java.awt.event.ActionEvent?) {
                            openSettingsRequested = true
                            close(OK_EXIT_CODE)
                        }
                    },
                    cancelAction
                )
            }
            showRefreshButton -> {
                arrayOf(
                    object : DialogWrapperAction(Messages.BUTTON_REFRESH_DATA) {
                        override fun doAction(e: java.awt.event.ActionEvent?) {
                            refreshRequested = true
                            close(OK_EXIT_CODE)
                        }
                    },
                    cancelAction
                )
            }
            showOpenSettingsButton -> {
                arrayOf(
                    object : DialogWrapperAction(Messages.BUTTON_OPEN_SETTINGS) {
                        override fun doAction(e: java.awt.event.ActionEvent?) {
                            openSettingsRequested = true
                            close(OK_EXIT_CODE)
                        }
                    },
                    cancelAction
                )
            }
            else -> {
                arrayOf(okAction)
            }
        }
    }

    fun isRefreshRequested(): Boolean = refreshRequested
    fun isOpenSettingsRequested(): Boolean = openSettingsRequested
}

/**
 * Utility class for common dialog operations
 * 通用对话框操作的工具类
 */
object DialogUtils {
    
    /**
     * Show configuration dialog for setting up cookies
     * 显示配置 Cookie 的对话框
     */
    fun showConfigureCookieDialog(project: Project): Boolean {
        val dialog = LargeTextDialog(
            project,
            Messages.DIALOG_TITLE_CONFIG_COOKIE,
            Messages.DIALOG_CONFIG_COOKIE_MESSAGE,
            showOpenSettingsButton = true
        )

        dialog.show()
        if (dialog.isOpenSettingsRequested()) {
            openSettingsPage(project)
            return true
        }
        return false
    }
    
    /**
     * Show expired cookie dialog
     * 显示 Cookie 过期对话框
     */
    fun showExpiredCookieDialog(project: Project): Boolean {
        val dialog = LargeTextDialog(
            project,
            Messages.DIALOG_TITLE_COOKIE_EXPIRED,
            Messages.DIALOG_COOKIE_EXPIRED_MESSAGE,
            showOpenSettingsButton = true
        )

        dialog.show()
        if (dialog.isOpenSettingsRequested()) {
            openSettingsPage(project)
            return true
        }
        return false
    }
    
    /**
     * Show usage details dialog with refresh and settings options
     * 显示使用详情对话框，包含刷新和设置选项
     */
    fun showUsageDetailsDialog(project: Project, details: String): UsageDetailsDialogResult {
        val dialog = LargeTextDialog(
            project,
            Messages.DIALOG_TITLE_DETAILS,
            details,
            showBothButtons = true
        )

        dialog.show()
        return when {
            dialog.isRefreshRequested() -> UsageDetailsDialogResult.REFRESH
            dialog.isOpenSettingsRequested() -> UsageDetailsDialogResult.OPEN_SETTINGS
            else -> UsageDetailsDialogResult.CANCEL
        }
    }

    /**
     * Result of usage details dialog interaction
     * 使用详情对话框交互结果
     */
    enum class UsageDetailsDialogResult {
        REFRESH,
        OPEN_SETTINGS,
        CANCEL
    }
    
    /**
     * Show clear credentials confirmation dialog
     * 显示清除凭据确认对话框
     */
    fun showClearCredentialsDialog(project: Project): Boolean {
        val result = IntellijMessages.showYesNoDialog(
            project,
            Messages.DIALOG_CLEAR_CREDENTIALS_CONFIRM,
            Messages.DIALOG_TITLE_CLEAR_CREDENTIALS,
            IntellijMessages.getQuestionIcon()
        )

        return result == IntellijMessages.YES
    }
    
    /**
     * Show success message
     * 显示成功消息
     */
    fun showSuccessMessage(project: Project, message: String, title: String) {
        IntellijMessages.showInfoMessage(project, message, title)
    }

    /**
     * Show error message
     * 显示错误消息
     */
    fun showErrorMessage(project: Project, message: String, title: String) {
        IntellijMessages.showErrorDialog(project, message, title)
    }

    /**
     * Show warning message
     * 显示警告消息
     */
    fun showWarningMessage(project: Project, message: String, title: String) {
        IntellijMessages.showWarningDialog(project, message, title)
    }
    
    /**
     * Open plugin settings page
     * 打开插件设置页面
     */
    fun openSettingsPage(project: Project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(
            project,
            Messages.PLUGIN_NAME
        )
    }
    
    /**
     * Build user info section for display
     * 构建用户信息显示部分
     */
    fun buildUserInfoSection(userInfo: com.augmentcode.usagetracker.model.UserInfo?): String {
        return if (userInfo != null) {
            buildString {
                append(Messages.USER_INFO_TITLE).append("\n")
                userInfo.email?.let { append(Messages.format(Messages.USER_INFO_EMAIL, it)).append("\n") }
                userInfo.name?.let { append(Messages.format(Messages.USER_INFO_NAME, it)).append("\n") }
                userInfo.plan?.let { append(Messages.format(Messages.USER_INFO_PLAN, it)).append("\n") }
                append("\n")
            }
        } else ""
    }
    
    /**
     * Build usage info section for display
     * 构建使用量信息显示部分
     */
    fun buildUsageInfoSection(data: com.augmentcode.usagetracker.model.UsageData): String {
        return buildString {
            append(Messages.USAGE_INFO_TITLE).append("\n")
            append(Messages.format(Messages.USAGE_CURRENT, data.totalUsage)).append("\n")
            append(Messages.format(Messages.USAGE_LIMIT, data.usageLimit)).append("\n")
            append(Messages.format(Messages.USAGE_PERCENTAGE, data.getUsagePercentage())).append("\n")
            append(Messages.format(Messages.USAGE_REMAINING, data.getRemainingUsage())).append("\n")
            
            if (data.dailyUsage > 0) {
                append(Messages.format(Messages.USAGE_DAILY, data.dailyUsage)).append("\n")
            }
            
            data.lastUpdate?.let {
                val formattedTime = it.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                append(Messages.format(Messages.USAGE_LAST_UPDATE, formattedTime)).append("\n")
            }
        }
    }
    
    /**
     * Build auth status section for display
     * 构建认证状态显示部分
     */
    fun buildAuthStatusSection(isExpired: Boolean): String {
        return buildString {
            append("\n").append(Messages.AUTH_STATUS_TITLE).append("\n")
            if (isExpired) {
                append(Messages.AUTH_STATUS_EXPIRED)
            } else {
                append(Messages.AUTH_STATUS_VALID)
            }
        }
    }
    
    /**
     * Build complete usage details string
     * 构建完整的使用详情字符串
     */
    fun buildUsageDetails(
        data: com.augmentcode.usagetracker.model.UsageData,
        userInfo: com.augmentcode.usagetracker.model.UserInfo?,
        isExpired: Boolean
    ): String {
        return buildString {
            append(Messages.DIALOG_TITLE_DETAILS).append("\n\n")
            append(buildUserInfoSection(userInfo))
            append(buildUsageInfoSection(data))
            append(buildAuthStatusSection(isExpired))

            // Always include cookie setup instructions
            append("\n\n").append("═".repeat(50)).append("\n")
            append(Messages.COOKIE_SETUP_INSTRUCTIONS)
        }
    }
    
    /**
     * Build tooltip text for status bar
     * 构建状态栏的工具提示文本
     */
    fun buildStatusBarTooltip(
        data: com.augmentcode.usagetracker.model.UsageData,
        userInfo: com.augmentcode.usagetracker.model.UserInfo?,
        isExpired: Boolean
    ): String {
        return buildString {
            append(Messages.STATUS_BAR_TITLE).append("\n\n")

            // Authentication status
            if (isExpired) {
                append(Messages.STATUS_BAR_AUTH_EXPIRED).append("\n")
                append(Messages.STATUS_BAR_AUTH_EXPIRED_HINT).append("\n\n")
            } else {
                append(Messages.STATUS_BAR_AUTH_NORMAL).append("\n\n")
            }

            append(buildUserInfoSection(userInfo))
            append(buildUsageInfoSection(data))

            append("\n").append(Messages.STATUS_BAR_DATA_SOURCE).append("\n")
            if (isExpired) {
                append(Messages.STATUS_BAR_CLICK_UPDATE_COOKIE)
            } else {
                append(Messages.STATUS_BAR_CLICK_DETAILS)
            }

            // Always include brief cookie setup reminder
            append("\n\n").append("💡 Cookie 设置提醒：").append("\n")
            append("访问 app.augmentcode.com → F12 → Application → Cookies → 复制 _session 值")
        }
    }
}
