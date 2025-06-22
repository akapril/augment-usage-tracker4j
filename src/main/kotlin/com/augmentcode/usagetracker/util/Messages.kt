package com.augmentcode.usagetracker.util

/**
 * Centralized message constants for the plugin
 * 插件的集中化消息常量
 */
object Messages {
    
    // Plugin Name
    const val PLUGIN_NAME = "Augment 使用量监控"
    
    // Status Bar Messages
    const val STATUS_BAR_TITLE = "Augment 使用量监控"
    const val STATUS_BAR_LOADING = "正在加载数据..."
    const val STATUS_BAR_AUTH_NORMAL = "✅ 认证状态：正常"
    const val STATUS_BAR_AUTH_EXPIRED = "⚠️ 认证状态：Cookie 已过期"
    const val STATUS_BAR_AUTH_EXPIRED_HINT = "请在设置中更新您的 Cookie"
    const val STATUS_BAR_NOT_AUTH = "状态：未认证"
    const val STATUS_BAR_NOT_AUTH_HINT = "请在设置中配置您的 Augment Cookie"
    const val STATUS_BAR_CLICK_SETTINGS = "点击打开设置"
    const val STATUS_BAR_CLICK_DETAILS = "点击查看详细信息或刷新数据"
    const val STATUS_BAR_CLICK_UPDATE_COOKIE = "点击打开设置更新 Cookie"
    const val STATUS_BAR_DATA_SOURCE = "数据来源：Augment API 实时数据"
    
    // User Info Labels
    const val USER_INFO_TITLE = "用户信息："
    const val USER_INFO_EMAIL = "• 邮箱：%s"
    const val USER_INFO_NAME = "• 姓名：%s"
    const val USER_INFO_PLAN = "• 计划：%s"
    
    // Usage Info Labels
    const val USAGE_INFO_TITLE = "使用情况："
    const val USAGE_CURRENT = "• 当前用量：%d 积分"
    const val USAGE_LIMIT = "• 总限额：%d 积分"
    const val USAGE_PERCENTAGE = "• 使用率：%d%%"
    const val USAGE_REMAINING = "• 剩余额度：%d 积分"
    const val USAGE_DAILY = "日使用量：%d 积分"
    const val USAGE_LAST_UPDATE = "• 最后更新：%s"
    
    // Auth Status Messages
    const val AUTH_STATUS_TITLE = "认证状态："
    const val AUTH_STATUS_VALID = "✅ 已认证 - Cookie 有效"
    const val AUTH_STATUS_EXPIRED = "⚠️ Cookie 已过期 - 请在设置中更新"
    
    // Dialog Titles
    const val DIALOG_TITLE_DETAILS = "Augment 使用详情"
    const val DIALOG_TITLE_CONFIG_COOKIE = "配置 Augment Cookie"
    const val DIALOG_TITLE_COOKIE_EXPIRED = "Cookie 已过期"
    const val DIALOG_TITLE_TEST_CONNECTION = "测试连接"
    const val DIALOG_TITLE_REFRESH_DATA = "刷新数据"
    const val DIALOG_TITLE_CLEAR_CREDENTIALS = "清除凭据"
    const val DIALOG_TITLE_SETTINGS = "设置"
    
    // Cookie setup instructions (common part)
    const val COOKIE_SETUP_INSTRUCTIONS = """
📋 Cookie 设置详细步骤：

1️⃣ 打开浏览器，访问 app.augmentcode.com
2️⃣ 登录您的 Augment 账户
3️⃣ 按 F12 键打开开发者工具
4️⃣ 点击 "Application" 标签页（Chrome）或 "存储" 标签页（Firefox）
5️⃣ 在左侧找到 "Cookies" → "https://app.augmentcode.com"
6️⃣ 找到名为 "_session" 的 Cookie 条目
7️⃣ 双击 "_session" 的值，全选并复制
8️⃣ 在插件设置中粘贴这个值

💡 提示：Cookie 通常很长，请确保完整复制！"""

    // Dialog Messages
    const val DIALOG_CONFIG_COOKIE_MESSAGE = """请配置您的 Augment Cookie 以开始监控使用量。

$COOKIE_SETUP_INSTRUCTIONS

是否现在打开设置页面？"""

    const val DIALOG_COOKIE_EXPIRED_MESSAGE = """⚠️ 您的 Augment Cookie 已过期！

请重新获取 Cookie 并更新设置。

$COOKIE_SETUP_INSTRUCTIONS

是否现在打开设置页面更新 Cookie？"""
    
    const val DIALOG_REFRESH_CONFIRM = "是否立即刷新数据？"
    const val DIALOG_CLEAR_CREDENTIALS_CONFIRM = "您确定要清除所有已存储的凭据吗？\n这将使您从 Augment 注销。"
    
    // Button Labels
    const val BUTTON_OPEN_SETTINGS = "打开设置"
    const val BUTTON_REFRESH_DATA = "刷新数据"
    const val BUTTON_CANCEL = "取消"
    const val BUTTON_CLOSE = "关闭"
    
    // Success Messages
    const val SUCCESS_CONNECTION = "✅ 连接成功！\n身份认证正常工作。"
    const val SUCCESS_DATA_REFRESH = "✅ 数据刷新成功！"
    const val SUCCESS_DATA_REFRESH_DETAILED = "✅ 数据刷新成功！\n\n当前用量：%d/%d 积分\n使用百分比：%d%%"
    const val SUCCESS_CREDENTIALS_CLEARED = "✅ 凭据清除成功。"
    const val SUCCESS_SETTINGS_SAVED = "✅ 设置保存成功！"
    
    // Error Messages
    const val ERROR_CONNECTION_FAILED = "❌ 连接失败：\n%s"
    const val ERROR_CONNECTION_ERROR = "❌ 连接错误：\n%s"
    const val ERROR_DATA_REFRESH_FAILED = "❌ 数据刷新失败。\n请检查您的网络连接和认证状态。"
    const val ERROR_SETTINGS_SAVE = "❌ 保存设置时出错：\n%s"
    const val ERROR_REFRESHING_DATA = "正在刷新数据..."
    
    // Warning Messages
    const val WARNING_ENTER_COOKIE = "请先输入您的 Augment Cookie。"
    const val WARNING_CONFIGURE_AUTH = "请先配置身份认证。"
    
    // Settings Panel Labels
    const val SETTINGS_TITLE = "Augment 使用量监控设置"
    const val SETTINGS_ENABLE_TRACKER = "启用监控："
    const val SETTINGS_SHOW_STATUS_BAR = "在状态栏显示："
    const val SETTINGS_AUTH_SECTION = "身份认证"
    const val SETTINGS_COOKIE_LABEL = "Cookie："
    const val SETTINGS_COOKIE_HINT = "输入您的 Augment 浏览器会话 Cookie。<br>格式：_session=eyJ... 或完整的 Cookie 字符串"
    const val SETTINGS_REFRESH_SECTION = "刷新设置"
    const val SETTINGS_REFRESH_INTERVAL = "刷新间隔（秒）："
    const val SETTINGS_REFRESH_HINT = "多久刷新一次使用量数据（5-300 秒）"
    const val SETTINGS_STATUS_SECTION = "状态"
    
    // Settings Status Messages
    const val SETTINGS_STATUS_NOT_CONFIGURED = "状态：未配置"
    const val SETTINGS_STATUS_AUTHENTICATED = "状态：已认证"
    const val SETTINGS_STATUS_AUTHENTICATED_LAST_UPDATE = "状态：已认证（最后更新：%s）"
    const val SETTINGS_STATUS_AUTHENTICATED_EXPIRED = "状态：已认证（已过期 - 请更新 Cookie）"
    const val SETTINGS_STATUS_NOT_AUTHENTICATED = "状态：未认证"
    
    // Settings Button Labels
    const val SETTINGS_BUTTON_TEST_CONNECTION = "测试连接"
    const val SETTINGS_BUTTON_REFRESH_DATA = "刷新数据"
    const val SETTINGS_BUTTON_CLEAR_CREDENTIALS = "清除凭据"
    
    // Settings Tooltips
    const val SETTINGS_TOOLTIP_COOKIE = "输入您的 Augment 浏览器 Cookie（例如：_session=...）"
    const val SETTINGS_TOOLTIP_REFRESH_INTERVAL = "刷新间隔（秒）（5-300）"
    const val SETTINGS_TOOLTIP_ENABLE = "启用或禁用使用量监控"
    const val SETTINGS_TOOLTIP_STATUS_BAR = "在状态栏显示使用量信息"
    const val SETTINGS_TOOLTIP_TEST_CONNECTION = "测试与 Augment API 的连接"
    const val SETTINGS_TOOLTIP_REFRESH_DATA = "手动刷新使用量数据"
    const val SETTINGS_TOOLTIP_CLEAR_CREDENTIALS = "清除已存储的认证凭据"
    
    // Settings Checkbox Labels
    const val SETTINGS_CHECKBOX_ENABLE = "启用 Augment 使用量监控"
    const val SETTINGS_CHECKBOX_STATUS_BAR = "在状态栏显示"
    
    /**
     * Format string with parameters
     */
    fun format(template: String, vararg args: Any): String {
        return String.format(template, *args)
    }
}
