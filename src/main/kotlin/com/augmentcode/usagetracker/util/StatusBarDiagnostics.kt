package com.augmentcode.usagetracker.util

import com.augmentcode.usagetracker.ui.AugmentStatusBarWidgetFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.status.StatusBarUtil

/**
 * Utility class for diagnosing and fixing status bar widget issues
 * 状态栏组件诊断和修复工具类
 */
object StatusBarDiagnostics {
    
    private val LOG = Logger.getInstance(StatusBarDiagnostics::class.java)
    
    /**
     * Perform comprehensive status bar diagnostics
     * 执行全面的状态栏诊断
     */
    fun performDiagnostics(project: Project): DiagnosticResult {
        LOG.info("Starting status bar diagnostics for project: ${project.name}")
        
        val issues = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        
        try {
            // 1. Check if WindowManager is available
            val windowManager = WindowManager.getInstance()
            if (windowManager == null) {
                issues.add("WindowManager instance is null")
                suggestions.add("重启 IDE 并重新安装插件")
                return DiagnosticResult(false, issues, suggestions)
            }
            
            // 2. Check if status bar exists
            val statusBar = windowManager.getStatusBar(project)
            if (statusBar == null) {
                issues.add("Status bar is null for project: ${project.name}")
                suggestions.add("确保项目已完全加载，然后重启 IDE")
                return DiagnosticResult(false, issues, suggestions)
            }
            
            // 3. Check if our widget is registered
            val widgetId = Constants.WIDGET_ID
            val existingWidget = statusBar.getWidget(widgetId)
            
            if (existingWidget == null) {
                issues.add("Augment widget not found in status bar")
                suggestions.add("尝试手动启用状态栏组件")
                
                // Try to manually add the widget
                try {
                    val factory = AugmentStatusBarWidgetFactory()
                    if (factory.canBeEnabledOn(statusBar)) {
                        val widget = factory.createWidget(project)
                        statusBar.addWidget(widget, "after Encoding")
                        statusBar.updateWidget(widgetId)
                        LOG.info("Successfully manually added widget to status bar")
                        suggestions.add("已尝试手动添加组件到状态栏")
                    } else {
                        issues.add("Widget factory reports it cannot be enabled on this status bar")
                        suggestions.add("检查 IDE 版本兼容性")
                    }
                } catch (e: Exception) {
                    LOG.error("Failed to manually add widget", e)
                    issues.add("Failed to manually add widget: ${e.message}")
                    suggestions.add("查看 IDE 日志获取详细错误信息")
                }
            } else {
                LOG.info("Widget found in status bar: ${existingWidget.ID()}")
            }
            
            // 4. Check widget factory registration
            try {
                val factory = AugmentStatusBarWidgetFactory()
                val factoryId = factory.getId()
                val displayName = factory.getDisplayName()
                val isAvailable = factory.isAvailable(project)
                
                LOG.info("Widget factory check - ID: $factoryId, DisplayName: $displayName, Available: $isAvailable")
                
                if (!isAvailable) {
                    issues.add("Widget factory reports not available for this project")
                    suggestions.add("检查项目类型和 IDE 版本兼容性")
                }
            } catch (e: Exception) {
                LOG.error("Error checking widget factory", e)
                issues.add("Widget factory error: ${e.message}")
                suggestions.add("重新安装插件")
            }
            
            // 5. Check status bar widget list
            try {
                val allWidgets = statusBar.allWidgets
                if (allWidgets != null) {
                    LOG.info("All status bar widgets: ${allWidgets.map { it.ID() }}")

                    val augmentWidgets = allWidgets.filter { it.ID().contains("augment", ignoreCase = true) }
                    if (augmentWidgets.isEmpty()) {
                        issues.add("No Augment widgets found in status bar widget list")
                    } else {
                        LOG.info("Found Augment widgets: ${augmentWidgets.map { it.ID() }}")
                    }
                } else {
                    issues.add("Status bar allWidgets is null")
                }
            } catch (e: Exception) {
                LOG.error("Error checking status bar widgets", e)
                issues.add("Error checking status bar widgets: ${e.message}")
            }
            
            return DiagnosticResult(issues.isEmpty(), issues, suggestions)
            
        } catch (e: Exception) {
            LOG.error("Error during status bar diagnostics", e)
            issues.add("Diagnostic error: ${e.message}")
            suggestions.add("重启 IDE 并重新安装插件")
            return DiagnosticResult(false, issues, suggestions)
        }
    }
    
    /**
     * Force enable the status bar widget
     * 强制启用状态栏组件
     */
    fun forceEnableWidget(project: Project): Boolean {
        LOG.info("Attempting to force enable status bar widget for project: ${project.name}")
        
        try {
            val windowManager = WindowManager.getInstance() ?: return false
            val statusBar = windowManager.getStatusBar(project) ?: return false
            
            // Remove existing widget if present
            val widgetId = Constants.WIDGET_ID
            val existingWidget = statusBar.getWidget(widgetId)
            if (existingWidget != null) {
                statusBar.removeWidget(widgetId)
                LOG.info("Removed existing widget")
            }
            
            // Create and add new widget
            val factory = AugmentStatusBarWidgetFactory()
            val widget = factory.createWidget(project)
            
            statusBar.addWidget(widget, "after Encoding")
            statusBar.updateWidget(widgetId)
            
            LOG.info("Successfully force-enabled status bar widget")
            return true
            
        } catch (e: Exception) {
            LOG.error("Failed to force enable widget", e)
            return false
        }
    }
    
    /**
     * Get user-friendly diagnostic report
     * 获取用户友好的诊断报告
     */
    fun getDiagnosticReport(project: Project): String {
        val result = performDiagnostics(project)
        
        return buildString {
            append("=== Augment 状态栏组件诊断报告 ===\n\n")
            
            if (result.success) {
                append("✅ 状态栏组件状态：正常\n")
                append("所有检查都通过，组件应该正常显示。\n")
            } else {
                append("❌ 状态栏组件状态：异常\n\n")
                
                append("发现的问题：\n")
                result.issues.forEachIndexed { index, issue ->
                    append("${index + 1}. $issue\n")
                }
                
                append("\n建议的解决方案：\n")
                result.suggestions.forEachIndexed { index, suggestion ->
                    append("${index + 1}. $suggestion\n")
                }
            }
            
            append("\n=== 手动解决步骤 ===\n")
            append("1. 重启 IDE\n")
            append("2. 检查插件是否已启用：设置 → 插件 → 已安装\n")
            append("3. 查看 IDE 日志：帮助 → 在资源管理器中显示日志\n")
            append("4. 如问题持续，请重新安装插件\n")
        }
    }
    
    /**
     * Diagnostic result data class
     * 诊断结果数据类
     */
    data class DiagnosticResult(
        val success: Boolean,
        val issues: List<String>,
        val suggestions: List<String>
    )
}
