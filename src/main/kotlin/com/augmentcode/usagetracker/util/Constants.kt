package com.augmentcode.usagetracker.util

/**
 * Constants used throughout the plugin
 */
object Constants {
    // API Configuration
    const val API_BASE_URL = "https://app.augmentcode.com/api"
    const val WEB_BASE_URL = "https://app.augmentcode.com"
    
    // API Endpoints
    const val ENDPOINT_USER = "/user"
    const val ENDPOINT_CREDITS = "/credits"
    const val ENDPOINT_SUBSCRIPTION = "/subscription"
    
    // Settings Keys
    const val SETTINGS_COOKIES = "augment.cookies"
    const val SETTINGS_REFRESH_INTERVAL = "augment.refreshInterval"
    const val SETTINGS_SHOW_IN_STATUS_BAR = "augment.showInStatusBar"
    const val SETTINGS_ENABLED = "augment.enabled"
    
    // Default Values
    const val DEFAULT_REFRESH_INTERVAL = 30 // seconds
    const val DEFAULT_SHOW_IN_STATUS_BAR = true
    const val DEFAULT_ENABLED = true
    
    // Widget Configuration
    const val WIDGET_ID = "AugmentUsageTracker"
    const val WIDGET_DISPLAY_NAME = "Augment 使用量"
    
    // HTTP Configuration
    const val HTTP_TIMEOUT_SECONDS = 30L
    const val HTTP_USER_AGENT = "AugmentUsageTracker-IDEA/1.0.0"
    
    // Status Messages
    const val STATUS_NOT_AUTHENTICATED = "未认证"
    const val STATUS_LOADING = "加载中..."
    const val STATUS_ERROR = "错误"
    const val STATUS_NO_DATA = "无数据"
}
