package com.augmentcode.usagetracker.model

import java.time.LocalDateTime

/**
 * Data class representing Augment usage information
 */
data class UsageData(
    val totalUsage: Int = 0,
    val usageLimit: Int = 0,
    val dailyUsage: Int = 0,
    val monthlyUsage: Int = 0,
    val lastUpdate: LocalDateTime? = null,
    val subscriptionType: String? = null,
    val renewalDate: String? = null
) {
    /**
     * Calculate usage percentage
     */
    fun getUsagePercentage(): Int {
        return if (usageLimit > 0) {
            ((totalUsage.toDouble() / usageLimit.toDouble()) * 100).toInt()
        } else {
            0
        }
    }
    
    /**
     * Get remaining usage
     */
    fun getRemainingUsage(): Int {
        return maxOf(0, usageLimit - totalUsage)
    }
    
    /**
     * Check if usage is near limit (>= 90%)
     */
    fun isNearLimit(): Boolean {
        return getUsagePercentage() >= 90
    }
    
    /**
     * Check if usage is at warning level (>= 75%)
     */
    fun isAtWarningLevel(): Boolean {
        return getUsagePercentage() >= 75
    }
}

/**
 * Data class for API response
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

/**
 * User information from Augment API
 */
data class UserInfo(
    val email: String? = null,
    val name: String? = null,
    val plan: String? = null
)
