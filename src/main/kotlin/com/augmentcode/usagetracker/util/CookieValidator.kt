package com.augmentcode.usagetracker.util

import com.intellij.openapi.diagnostic.Logger

/**
 * Utility class for validating and processing cookies
 * Cookie 验证和处理的工具类
 */
object CookieValidator {
    
    private val LOG = Logger.getInstance(CookieValidator::class.java)
    
    // Common cookie patterns
    private val SESSION_COOKIE_PATTERN = Regex("_session=([^;\\s]+)")
    private val JWT_PATTERN = Regex("^eyJ[A-Za-z0-9+/=]+\\.[A-Za-z0-9+/=]+\\.[A-Za-z0-9+/=]*$")
    
    /**
     * Validation result for cookies
     * Cookie 验证结果
     */
    data class ValidationResult(
        val isValid: Boolean,
        val cleanedCookie: String? = null,
        val errorMessage: String? = null,
        val warnings: List<String> = emptyList()
    )
    
    /**
     * Validate and clean cookie string
     * 验证并清理 Cookie 字符串
     */
    fun validateAndCleanCookie(rawCookie: String?): ValidationResult {
        if (rawCookie.isNullOrBlank()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Cookie 不能为空"
            )
        }
        
        val trimmedCookie = rawCookie.trim()
        
        // Try to extract session cookie from full cookie string
        val sessionMatch = SESSION_COOKIE_PATTERN.find(trimmedCookie)
        val sessionValue = sessionMatch?.groupValues?.get(1)
        
        return when {
            // If we found a session cookie, validate it
            sessionValue != null -> {
                validateSessionValue(sessionValue, trimmedCookie)
            }
            
            // If the input looks like a JWT token directly
            JWT_PATTERN.matches(trimmedCookie) -> {
                ValidationResult(
                    isValid = true,
                    cleanedCookie = "_session=$trimmedCookie",
                    warnings = listOf("已自动添加 _session= 前缀")
                )
            }
            
            // If it's already in the correct format
            trimmedCookie.startsWith("_session=") -> {
                val value = trimmedCookie.substring(9)
                validateSessionValue(value, trimmedCookie)
            }
            
            else -> {
                ValidationResult(
                    isValid = false,
                    errorMessage = "无效的 Cookie 格式。请提供完整的 Cookie 字符串或 _session 值。"
                )
            }
        }
    }
    
    /**
     * Validate session value
     * 验证 session 值
     */
    private fun validateSessionValue(sessionValue: String, originalCookie: String): ValidationResult {
        val warnings = mutableListOf<String>()
        
        // Check if it looks like a JWT token
        if (!JWT_PATTERN.matches(sessionValue)) {
            warnings.add("Session 值格式可能不正确，应该是 JWT 格式")
        }
        
        // Check length (JWT tokens are typically quite long)
        if (sessionValue.length < 100) {
            warnings.add("Session 值长度较短，可能不完整")
        }
        
        // Check for common issues
        if (sessionValue.contains(" ")) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Session 值包含空格，请检查复制是否完整"
            )
        }
        
        if (sessionValue.contains("\n") || sessionValue.contains("\r")) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Session 值包含换行符，请检查复制是否正确"
            )
        }
        
        // Determine the cleaned cookie format
        val cleanedCookie = if (originalCookie.contains("_session=")) {
            // Keep the original format if it already contains _session=
            originalCookie
        } else {
            // Add _session= prefix
            "_session=$sessionValue"
        }
        
        return ValidationResult(
            isValid = true,
            cleanedCookie = cleanedCookie,
            warnings = warnings
        )
    }
    
    /**
     * Extract session value from cookie string
     * 从 Cookie 字符串中提取 session 值
     */
    fun extractSessionValue(cookie: String?): String? {
        if (cookie.isNullOrBlank()) return null
        
        val sessionMatch = SESSION_COOKIE_PATTERN.find(cookie)
        return sessionMatch?.groupValues?.get(1)
    }
    
    /**
     * Check if cookie appears to be expired based on format
     * 基于格式检查 Cookie 是否可能已过期
     */
    fun isLikelyExpired(cookie: String?): Boolean {
        if (cookie.isNullOrBlank()) return true
        
        val sessionValue = extractSessionValue(cookie) ?: return true
        
        // Very basic check - if it's too short, it might be expired/invalid
        return sessionValue.length < 50
    }
    
    /**
     * Sanitize cookie for logging (hide sensitive parts)
     * 为日志记录清理 Cookie（隐藏敏感部分）
     */
    fun sanitizeForLogging(cookie: String?): String {
        if (cookie.isNullOrBlank()) return "[empty]"
        
        val sessionValue = extractSessionValue(cookie)
        return if (sessionValue != null && sessionValue.length > 20) {
            "_session=${sessionValue.take(10)}...${sessionValue.takeLast(10)}"
        } else {
            "[invalid format]"
        }
    }
    
    /**
     * Get helpful error message for common cookie issues
     * 获取常见 Cookie 问题的有用错误消息
     */
    fun getHelpfulErrorMessage(cookie: String?): String {
        return when {
            cookie.isNullOrBlank() -> {
                "请输入 Cookie。获取方法：\n" +
                "1. 登录 app.augmentcode.com\n" +
                "2. 按 F12 打开开发者工具\n" +
                "3. 在 Application 标签页找到 Cookies\n" +
                "4. 复制 _session 的值"
            }
            
            cookie.trim().length < 20 -> {
                "Cookie 长度太短，可能复制不完整。请确保复制了完整的 _session 值。"
            }
            
            !cookie.contains("_session") && !JWT_PATTERN.matches(cookie.trim()) -> {
                "Cookie 格式不正确。请复制完整的 Cookie 字符串或 _session 的值。"
            }
            
            else -> {
                "Cookie 格式可能有问题。请重新获取并复制完整的 _session 值。"
            }
        }
    }
}
