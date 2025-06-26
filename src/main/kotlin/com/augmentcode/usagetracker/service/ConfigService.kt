package com.augmentcode.usagetracker.service

import com.augmentcode.usagetracker.util.Constants
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger

/**
 * Configuration service for managing plugin settings persistence
 * 配置服务，用于管理插件设置的持久化
 */
@Service
class ConfigService {
    
    companion object {
        private val LOG = Logger.getInstance(ConfigService::class.java)
        
        // Configuration keys
        private const val KEY_REFRESH_INTERVAL = "augment.refreshInterval"
        private const val KEY_ENABLED = "augment.enabled"
        private const val KEY_SHOW_IN_STATUS_BAR = "augment.showInStatusBar"
        
        fun getInstance(): ConfigService {
            return ApplicationManager.getApplication().getService(ConfigService::class.java)
        }
    }
    
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()
    
    init {
        LOG.info("ConfigService initialized")
    }
    
    /**
     * Get refresh interval in seconds
     * 获取刷新间隔（秒）
     */
    fun getRefreshInterval(): Int {
        val interval = properties.getInt(KEY_REFRESH_INTERVAL, Constants.DEFAULT_REFRESH_INTERVAL)
        LOG.debug("Retrieved refresh interval: $interval seconds")
        return interval
    }
    
    /**
     * Set refresh interval in seconds
     * 设置刷新间隔（秒）
     */
    fun setRefreshInterval(seconds: Int) {
        if (seconds < 5 || seconds > 3600) { // 扩展到 3600 秒（1小时）
            LOG.warn("Invalid refresh interval: $seconds, not saving (valid range: 5-3600 seconds)")
            return
        }
        
        properties.setValue(KEY_REFRESH_INTERVAL, seconds, Constants.DEFAULT_REFRESH_INTERVAL)
        LOG.info("Refresh interval saved: $seconds seconds")
    }
    
    /**
     * Get enabled state
     * 获取启用状态
     */
    fun isEnabled(): Boolean {
        val enabled = properties.getBoolean(KEY_ENABLED, Constants.DEFAULT_ENABLED)
        LOG.debug("Retrieved enabled state: $enabled")
        return enabled
    }
    
    /**
     * Set enabled state
     * 设置启用状态
     */
    fun setEnabled(enabled: Boolean) {
        properties.setValue(KEY_ENABLED, enabled, Constants.DEFAULT_ENABLED)
        LOG.info("Enabled state saved: $enabled")
    }
    
    /**
     * Get show in status bar setting
     * 获取状态栏显示设置
     */
    fun isShowInStatusBar(): Boolean {
        val show = properties.getBoolean(KEY_SHOW_IN_STATUS_BAR, Constants.DEFAULT_SHOW_IN_STATUS_BAR)
        LOG.debug("Retrieved show in status bar: $show")
        return show
    }
    
    /**
     * Set show in status bar setting
     * 设置状态栏显示
     */
    fun setShowInStatusBar(show: Boolean) {
        properties.setValue(KEY_SHOW_IN_STATUS_BAR, show, Constants.DEFAULT_SHOW_IN_STATUS_BAR)
        LOG.info("Show in status bar saved: $show")
    }
    
    /**
     * Reset all settings to defaults
     * 重置所有设置为默认值
     */
    fun resetToDefaults() {
        LOG.info("Resetting all settings to defaults")
        
        properties.unsetValue(KEY_REFRESH_INTERVAL)
        properties.unsetValue(KEY_ENABLED)
        properties.unsetValue(KEY_SHOW_IN_STATUS_BAR)
        
        LOG.info("All settings reset to defaults")
    }
    
    /**
     * Get configuration summary for debugging
     * 获取配置摘要用于调试
     */
    fun getConfigSummary(): String {
        return buildString {
            append("=== Configuration Summary ===\n")
            append("Refresh Interval: ${getRefreshInterval()} seconds\n")
            append("Enabled: ${isEnabled()}\n")
            append("Show in Status Bar: ${isShowInStatusBar()}\n")
            append("Default Refresh Interval: ${Constants.DEFAULT_REFRESH_INTERVAL} seconds\n")
            append("Default Enabled: ${Constants.DEFAULT_ENABLED}\n")
            append("Default Show in Status Bar: ${Constants.DEFAULT_SHOW_IN_STATUS_BAR}\n")
        }
    }
    
    /**
     * Validate and fix configuration if needed
     * 验证并修复配置（如果需要）
     */
    fun validateAndFixConfig(): Boolean {
        var fixed = false
        
        // Validate refresh interval
        val currentInterval = getRefreshInterval()
        if (currentInterval < 5 || currentInterval > 3600) { // 扩展到 3600 秒（1小时）
            LOG.warn("Invalid refresh interval detected: $currentInterval, fixing to default")
            setRefreshInterval(Constants.DEFAULT_REFRESH_INTERVAL)
            fixed = true
        }
        
        if (fixed) {
            LOG.info("Configuration validation completed with fixes applied")
        } else {
            LOG.debug("Configuration validation completed, no fixes needed")
        }
        
        return fixed
    }
    
    /**
     * Export configuration for backup
     * 导出配置用于备份
     */
    fun exportConfig(): Map<String, Any> {
        return mapOf(
            "refreshInterval" to getRefreshInterval(),
            "enabled" to isEnabled(),
            "showInStatusBar" to isShowInStatusBar()
        )
    }
    
    /**
     * Import configuration from backup
     * 从备份导入配置
     */
    fun importConfig(config: Map<String, Any>): Boolean {
        return try {
            config["refreshInterval"]?.let { 
                if (it is Int) setRefreshInterval(it)
            }
            config["enabled"]?.let { 
                if (it is Boolean) setEnabled(it)
            }
            config["showInStatusBar"]?.let { 
                if (it is Boolean) setShowInStatusBar(it)
            }
            
            LOG.info("Configuration imported successfully")
            true
        } catch (e: Exception) {
            LOG.error("Error importing configuration", e)
            false
        }
    }
}
