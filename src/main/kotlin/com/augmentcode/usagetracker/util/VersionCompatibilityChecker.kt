package com.augmentcode.usagetracker.util

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.Logger

/**
 * Version compatibility checker for different IntelliJ Platform versions
 * 版本兼容性检查器，用于不同的 IntelliJ 平台版本
 */
object VersionCompatibilityChecker {
    
    private val LOG = Logger.getInstance(VersionCompatibilityChecker::class.java)
    
    /**
     * Check if current IDE version is compatible and get compatibility info
     * 检查当前 IDE 版本是否兼容并获取兼容性信息
     */
    fun checkCompatibility(): CompatibilityInfo {
        val appInfo = ApplicationInfo.getInstance()
        val fullVersion = appInfo.fullVersion
        val buildNumber = appInfo.build.asString()
        val majorVersion = appInfo.majorVersion
        val minorVersion = appInfo.minorVersion
        
        LOG.info("=== IDE Version Compatibility Check ===")
        LOG.info("Full Version: $fullVersion")
        LOG.info("Build Number: $buildNumber")
        LOG.info("Major Version: $majorVersion")
        LOG.info("Minor Version: $minorVersion")
        LOG.info("Product Code: ${appInfo.build.productCode}")
        
        // Extract build number for compatibility check
        val buildNumberInt = try {
            buildNumber.split('.').firstOrNull()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            LOG.warn("Failed to parse build number: $buildNumber", e)
            0
        }
        
        LOG.info("Parsed Build Number: $buildNumberInt")
        
        val compatibility = when {
            buildNumberInt >= 262 -> CompatibilityLevel.FUTURE_VERSION
            buildNumberInt >= 253 -> CompatibilityLevel.LATEST_SUPPORTED
            buildNumberInt >= 241 -> CompatibilityLevel.FULLY_SUPPORTED
            buildNumberInt >= 231 -> CompatibilityLevel.MINIMUM_SUPPORTED
            else -> CompatibilityLevel.UNSUPPORTED
        }
        
        val requiresSpecialHandling = buildNumberInt >= 252 // 2025.2+
        
        LOG.info("Compatibility Level: $compatibility")
        LOG.info("Requires Special Handling: $requiresSpecialHandling")
        
        return CompatibilityInfo(
            fullVersion = fullVersion,
            buildNumber = buildNumber,
            buildNumberInt = buildNumberInt,
            compatibility = compatibility,
            requiresSpecialHandling = requiresSpecialHandling,
            recommendations = getRecommendations(compatibility, buildNumberInt)
        )
    }
    
    /**
     * Get recommendations based on compatibility level
     * 根据兼容性级别获取建议
     */
    private fun getRecommendations(compatibility: CompatibilityLevel, buildNumber: Int): List<String> {
        return when (compatibility) {
            CompatibilityLevel.UNSUPPORTED -> listOf(
                "当前 IDE 版本过旧，不支持此插件",
                "请升级到 2023.1 或更高版本",
                "建议使用 2023.2 或更高版本以获得最佳体验"
            )
            CompatibilityLevel.MINIMUM_SUPPORTED -> listOf(
                "当前版本为最低支持版本",
                "建议升级到更新版本以获得更好的稳定性",
                "某些新功能可能不可用"
            )
            CompatibilityLevel.FULLY_SUPPORTED -> listOf(
                "当前版本完全支持",
                "所有功能都应正常工作",
                "如有问题请检查插件配置"
            )
            CompatibilityLevel.LATEST_SUPPORTED -> listOf(
                "当前版本为最新支持版本",
                "所有功能都应正常工作",
                "如有问题可能需要特殊处理"
            )
            CompatibilityLevel.FUTURE_VERSION -> listOf(
                "当前版本为未来版本，可能存在兼容性问题",
                "插件可能需要更新以完全支持此版本",
                "建议使用诊断工具检查状态栏组件",
                "如有问题请尝试强制启用状态栏组件"
            )
        }
    }
    
    /**
     * Check if status bar widget needs special handling for current version
     * 检查当前版本的状态栏组件是否需要特殊处理
     */
    fun needsStatusBarSpecialHandling(): Boolean {
        val compatibility = checkCompatibility()
        return compatibility.requiresSpecialHandling
    }
    
    /**
     * Get status bar widget implementation strategy for current version
     * 获取当前版本的状态栏组件实现策略
     */
    fun getStatusBarStrategy(): StatusBarStrategy {
        val compatibility = checkCompatibility()
        
        return when {
            compatibility.buildNumberInt >= 252 -> StatusBarStrategy.NEW_API_WITH_FALLBACK
            compatibility.buildNumberInt >= 241 -> StatusBarStrategy.STANDARD_API
            else -> StatusBarStrategy.LEGACY_API
        }
    }
    
    /**
     * Log detailed compatibility information
     * 记录详细的兼容性信息
     */
    fun logDetailedInfo() {
        val compatibility = checkCompatibility()
        
        LOG.info("=== Detailed Compatibility Information ===")
        LOG.info("IDE Version: ${compatibility.fullVersion}")
        LOG.info("Build Number: ${compatibility.buildNumber}")
        LOG.info("Compatibility Level: ${compatibility.compatibility}")
        LOG.info("Requires Special Handling: ${compatibility.requiresSpecialHandling}")
        LOG.info("Status Bar Strategy: ${getStatusBarStrategy()}")
        
        LOG.info("Recommendations:")
        compatibility.recommendations.forEachIndexed { index, recommendation ->
            LOG.info("  ${index + 1}. $recommendation")
        }
        
        LOG.info("=== End Compatibility Information ===")
    }
    
    /**
     * Compatibility information data class
     * 兼容性信息数据类
     */
    data class CompatibilityInfo(
        val fullVersion: String,
        val buildNumber: String,
        val buildNumberInt: Int,
        val compatibility: CompatibilityLevel,
        val requiresSpecialHandling: Boolean,
        val recommendations: List<String>
    )
    
    /**
     * Compatibility levels
     * 兼容性级别
     */
    enum class CompatibilityLevel {
        UNSUPPORTED,        // < 231 (< 2023.1)
        MINIMUM_SUPPORTED,  // 231-240 (2023.1-2024.0)
        FULLY_SUPPORTED,    // 241-252 (2024.1-2025.2)
        LATEST_SUPPORTED,   // 253-261 (2025.3-2026.1)
        FUTURE_VERSION      // >= 262 (>= 2026.2)
    }
    
    /**
     * Status bar implementation strategies
     * 状态栏实现策略
     */
    enum class StatusBarStrategy {
        LEGACY_API,             // Old API for older versions
        STANDARD_API,           // Standard API for most versions
        NEW_API_WITH_FALLBACK   // New API with fallback for latest versions
    }
}
