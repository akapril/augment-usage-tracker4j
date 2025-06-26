package com.augmentcode.usagetracker.util

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Log collector for debugging status bar issues
 * 日志收集器，用于调试状态栏问题
 */
object LogCollector {
    
    private val LOG = Logger.getInstance(LogCollector::class.java)
    
    /**
     * Collect all relevant logs for debugging
     * 收集所有相关的调试日志
     */
    fun collectDebugInfo(): String {
        return buildString {
            append("=== Augment Usage Tracker Debug Information ===\n")
            append("Generated: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}\n\n")
            
            // System Information
            append("=== System Information ===\n")
            append(getSystemInfo())
            append("\n")
            
            // IDE Information
            append("=== IDE Information ===\n")
            append(getIDEInfo())
            append("\n")
            
            // Plugin Information
            append("=== Plugin Information ===\n")
            append(getPluginInfo())
            append("\n")
            
            // Recent Logs
            append("=== Recent Augment Logs ===\n")
            append(getRecentAugmentLogs())
            append("\n")
            
            // Status Bar Information
            append("=== Status Bar Information ===\n")
            append(getStatusBarInfo())
            append("\n")
            
            // Troubleshooting Steps
            append("=== Recommended Troubleshooting Steps ===\n")
            append(getTroubleshootingSteps())
        }
    }
    
    /**
     * Get system information
     * 获取系统信息
     */
    private fun getSystemInfo(): String {
        return buildString {
            append("OS: ${System.getProperty("os.name")} ${System.getProperty("os.version")}\n")
            append("Java Version: ${System.getProperty("java.version")}\n")
            append("Java Vendor: ${System.getProperty("java.vendor")}\n")
            append("User Home: ${System.getProperty("user.home")}\n")
            append("Working Directory: ${System.getProperty("user.dir")}\n")
        }
    }
    
    /**
     * Get IDE information
     * 获取 IDE 信息
     */
    private fun getIDEInfo(): String {
        return buildString {
            try {
                val appInfo = ApplicationInfo.getInstance()
                append("IDE Name: ${appInfo.fullApplicationName}\n")
                append("IDE Version: ${appInfo.fullVersion}\n")
                append("Build Number: ${appInfo.build.asString()}\n")
                append("Product Code: ${appInfo.build.productCode}\n")
                append("Major Version: ${appInfo.majorVersion}\n")
                append("Minor Version: ${appInfo.minorVersion}\n")
                
                // Compatibility check
                val compatibility = VersionCompatibilityChecker.checkCompatibility()
                append("Compatibility Level: ${compatibility.compatibility}\n")
                append("Requires Special Handling: ${compatibility.requiresSpecialHandling}\n")
                append("Status Bar Strategy: ${VersionCompatibilityChecker.getStatusBarStrategy()}\n")
                
            } catch (e: Exception) {
                append("Error getting IDE info: ${e.message}\n")
            }
        }
    }
    
    /**
     * Get plugin information
     * 获取插件信息
     */
    private fun getPluginInfo(): String {
        return buildString {
            append("Plugin ID: ${Constants.WIDGET_ID}\n")
            append("Plugin Display Name: ${Constants.WIDGET_DISPLAY_NAME}\n")
            append("Default Refresh Interval: ${Constants.DEFAULT_REFRESH_INTERVAL}s\n")
            append("HTTP Timeout: ${Constants.HTTP_TIMEOUT_SECONDS}s\n")
            append("User Agent: ${Constants.HTTP_USER_AGENT}\n")
        }
    }
    
    /**
     * Get recent Augment-related logs from IDE log file
     * 从 IDE 日志文件中获取最近的 Augment 相关日志
     */
    private fun getRecentAugmentLogs(): String {
        return buildString {
            try {
                val logDir = PathManager.getLogPath()
                val logFile = File(logDir, "idea.log")
                
                append("Log File Path: ${logFile.absolutePath}\n")
                append("Log File Exists: ${logFile.exists()}\n")
                
                if (logFile.exists() && logFile.canRead()) {
                    append("Log File Size: ${logFile.length()} bytes\n")
                    append("Last Modified: ${java.util.Date(logFile.lastModified())}\n\n")
                    
                    // Read last 100 lines and filter for Augment-related entries
                    val lines = logFile.readLines()
                    val augmentLines = lines.takeLast(1000)
                        .filter { line ->
                            line.contains("Augment", ignoreCase = true) ||
                            line.contains("StatusBar", ignoreCase = true) ||
                            line.contains("Widget", ignoreCase = true)
                        }
                        .takeLast(50) // Last 50 relevant lines
                    
                    if (augmentLines.isNotEmpty()) {
                        append("Recent Augment-related log entries:\n")
                        augmentLines.forEach { line ->
                            append("$line\n")
                        }
                    } else {
                        append("No recent Augment-related log entries found.\n")
                        append("This might indicate the plugin is not loading properly.\n")
                    }
                } else {
                    append("Cannot read log file. File may not exist or no read permission.\n")
                }
                
            } catch (e: Exception) {
                append("Error reading log file: ${e.message}\n")
                LOG.error("Error reading log file", e)
            }
        }
    }
    
    /**
     * Get status bar information
     * 获取状态栏信息
     */
    private fun getStatusBarInfo(): String {
        return buildString {
            try {
                // This would be called from a project context
                append("Status bar information requires project context.\n")
                append("Run diagnostics from Settings → Tools → Augment Usage Tracker → Diagnose Status Bar\n")
                
            } catch (e: Exception) {
                append("Error getting status bar info: ${e.message}\n")
            }
        }
    }
    
    /**
     * Get troubleshooting steps based on current situation
     * 根据当前情况获取故障排除步骤
     */
    private fun getTroubleshootingSteps(): String {
        return buildString {
            val compatibility = VersionCompatibilityChecker.checkCompatibility()
            
            append("Based on your IDE version (${compatibility.fullVersion}):\n\n")
            
            compatibility.recommendations.forEachIndexed { index, recommendation ->
                append("${index + 1}. $recommendation\n")
            }
            
            append("\nGeneral troubleshooting steps:\n")
            append("1. Restart IDE completely\n")
            append("2. Check Settings → Tools → Augment Usage Tracker\n")
            append("3. Ensure 'Show in Status Bar' is enabled\n")
            append("4. Click 'Diagnose Status Bar' button\n")
            append("5. Check View → Appearance → Status Bar is enabled\n")
            append("6. Try reinstalling the plugin\n")
            
            if (compatibility.requiresSpecialHandling) {
                append("\nFor IntelliJ IDEA 2025.2+ specific steps:\n")
                append("1. The plugin may need extra time to initialize\n")
                append("2. Wait 30 seconds after IDE startup\n")
                append("3. Use the diagnostic tool multiple times\n")
                append("4. Check if other status bar widgets are visible\n")
                append("5. Try creating a new project to test\n")
            }
        }
    }
    
    /**
     * Save debug info to a file
     * 将调试信息保存到文件
     */
    fun saveDebugInfoToFile(): String? {
        return try {
            val debugInfo = collectDebugInfo()
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val fileName = "augment_debug_$timestamp.txt"
            val userHome = System.getProperty("user.home")
            val file = File(userHome, fileName)
            
            file.writeText(debugInfo)
            LOG.info("Debug info saved to: ${file.absolutePath}")
            
            file.absolutePath
        } catch (e: Exception) {
            LOG.error("Failed to save debug info to file", e)
            null
        }
    }
}
