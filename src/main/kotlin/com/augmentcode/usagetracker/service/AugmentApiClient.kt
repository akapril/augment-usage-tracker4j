package com.augmentcode.usagetracker.service

import com.augmentcode.usagetracker.model.ApiResponse
import com.augmentcode.usagetracker.model.UsageData
import com.augmentcode.usagetracker.model.UserInfo
import com.augmentcode.usagetracker.util.Constants
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Data class to hold usage values
 */
private data class UsageValues(
    val totalUsage: Int,
    val usageLimit: Int,
    val dailyUsage: Int,
    val monthlyUsage: Int
)

/**
 * API client for communicating with Augment services
 */
class AugmentApiClient {

    companion object {
        private val LOG = Logger.getInstance(AugmentApiClient::class.java)
    }

    private val authManager: AuthManager by lazy { AuthManager.getInstance() }
    private val gson = Gson()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(Constants.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(Constants.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(Constants.HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    /**
     * Get usage data from Augment API
     */
    fun getUsageData(): ApiResponse<UsageData> {
        return try {
            val response = makeRequest(Constants.ENDPOINT_CREDITS)
            if (response.success && response.data != null) {
                val usageData = parseUsageResponse(response.data)
                ApiResponse(true, usageData)
            } else {
                ApiResponse(false, error = response.error ?: "Failed to get usage data")
            }
        } catch (e: Exception) {
            LOG.error("Error getting usage data", e)
            ApiResponse(false, error = "Network error: ${e.message}")
        }
    }

    /**
     * Get user information from Augment API
     */
    fun getUserInfo(): ApiResponse<UserInfo> {
        return try {
            val response = makeRequest(Constants.ENDPOINT_USER)
            if (response.success && response.data != null) {
                val userInfo = parseUserResponse(response.data)
                ApiResponse(true, userInfo)
            } else {
                ApiResponse(false, error = response.error ?: "Failed to get user info")
            }
        } catch (e: Exception) {
            LOG.error("Error getting user info", e)
            ApiResponse(false, error = "Network error: ${e.message}")
        }
    }

    /**
     * Test API connection
     */
    fun testConnection(): ApiResponse<Boolean> {
        return try {
            val response = makeRequest(Constants.ENDPOINT_USER)
            if (response.success) {
                ApiResponse(true, true)
            } else {
                ApiResponse(false, false, response.error ?: "Connection test failed")
            }
        } catch (e: Exception) {
            LOG.error("Error testing connection", e)
            ApiResponse(false, false, "Connection error: ${e.message}")
        }
    }

    /**
     * Make HTTP request to Augment API
     */
    private fun makeRequest(endpoint: String): ApiResponse<String> {
        val cookies = authManager.getCookies()
        if (cookies.isNullOrBlank()) {
            return ApiResponse(false, error = "No authentication cookies available")
        }

        val url = "${Constants.API_BASE_URL}$endpoint"

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", Constants.HTTP_USER_AGENT)
            .addHeader("Referer", Constants.WEB_BASE_URL)
            .addHeader("Origin", Constants.WEB_BASE_URL)
            .addHeader("Cookie", cookies)
            .build()

        LOG.debug("Making API request to: $url")

        return try {
            val startTime = System.currentTimeMillis()
            httpClient.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - startTime

                LOG.debug("API response: ${response.code} in ${duration}ms")

                when (response.code) {
                    200 -> {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            ApiResponse(true, responseBody)
                        } else {
                            ApiResponse(false, error = "Empty response body")
                        }
                    }

                    401 -> {
                        LOG.warn("Authentication failed - cookies may be expired")
                        ApiResponse(false, error = "Authentication required. Please update your cookies.")
                    }

                    403 -> {
                        ApiResponse(false, error = "Access forbidden. Please check your permissions.")
                    }

                    404 -> {
                        ApiResponse(false, error = "API endpoint not found.")
                    }

                    429 -> {
                        ApiResponse(false, error = "Rate limit exceeded. Please try again later.")
                    }

                    in 500..599 -> {
                        ApiResponse(false, error = "Server error (${response.code}). Please try again later.")
                    }

                    else -> {
                        ApiResponse(false, error = "Unexpected response code: ${response.code}")
                    }
                }
            }
        } catch (e: IOException) {
            LOG.error("Network error during API request", e)
            ApiResponse(false, error = "Network error: ${e.message}")
        } catch (e: Exception) {
            LOG.error("Unexpected error during API request", e)
            ApiResponse(false, error = "Request failed: ${e.message}")
        }
    }

    /**
     * Parse usage data from API response
     */
    private fun parseUsageResponse(responseBody: String): UsageData? {
        return try {
            LOG.info("Raw API response for usage data: $responseBody")
            val jsonObject = JsonParser.parseString(responseBody).asJsonObject
            LOG.info("Parsed JSON object: $jsonObject")

            // Extract usage information based on actual Augment API response format
            val (totalUsage, usageLimit, dailyUsage, monthlyUsage) = when {
                // Check for Credits API response format (primary format)
                jsonObject.has("usageUnitsUsedThisBillingCycle") -> {
                    val used = extractIntValue(jsonObject, listOf("usageUnitsUsedThisBillingCycle"))
                    val available = extractIntValue(jsonObject, listOf("usageUnitsAvailable"))
                    val limit = used + available
                    LOG.info("Using Credits API format - used: $used, available: $available, total: $limit")
                    UsageValues(used, limit, used, used)
                }
                // Check for subscription API response format
                jsonObject.has("creditsRenewingEachBillingCycle") -> {
                    val included = extractIntValue(jsonObject, listOf("creditsIncludedThisBillingCycle"))
                    val renewing = extractIntValue(jsonObject, listOf("creditsRenewingEachBillingCycle"))
                    val used = included - renewing
                    LOG.info("Using Subscription API format - included: $included, renewing: $renewing, used: $used")
                    UsageValues(used, included, 0, used)
                }
                // Fallback to generic formats
                else -> {
                    val used = extractIntValue(jsonObject, listOf("totalUsage", "usage", "credits_used", "used"))
                    val limit = extractIntValue(jsonObject, listOf("usageLimit", "limit", "credits_limit", "total"))
                    val daily = extractIntValue(jsonObject, listOf("dailyUsage", "daily", "today"))
                    val monthly = extractIntValue(jsonObject, listOf("monthlyUsage", "monthly", "month"))
                    LOG.info("Using fallback format - totalUsage: $used, usageLimit: $limit")
                    UsageValues(used, limit, daily, monthly)
                }
            }

            LOG.info("Final extracted values - totalUsage: $totalUsage, usageLimit: $usageLimit, dailyUsage: $dailyUsage, monthlyUsage: $monthlyUsage")

            val subscriptionType = extractStringValue(jsonObject, listOf("subscriptionType", "plan", "subscription"))
            val renewalDate = extractStringValue(jsonObject, listOf("renewalDate", "renewal", "expires"))

            UsageData(
                totalUsage = totalUsage,
                usageLimit = usageLimit,
                dailyUsage = dailyUsage,
                monthlyUsage = monthlyUsage,
                lastUpdate = LocalDateTime.now(),
                subscriptionType = subscriptionType,
                renewalDate = renewalDate
            )
        } catch (e: Exception) {
            LOG.error("Error parsing usage response", e)
            null
        }
    }

    /**
     * Parse user information from API response
     */
    private fun parseUserResponse(responseBody: String): UserInfo? {
        return try {
            val jsonObject = JsonParser.parseString(responseBody).asJsonObject

            val email = extractStringValue(jsonObject, listOf("email", "user_email", "userEmail"))
            val name = extractStringValue(jsonObject, listOf("name", "username", "user_name", "displayName"))
            val plan = extractStringValue(jsonObject, listOf("plan", "subscription", "subscriptionType"))

            UserInfo(
                email = email,
                name = name,
                plan = plan
            )
        } catch (e: Exception) {
            LOG.error("Error parsing user response", e)
            null
        }
    }

    /**
     * Extract integer value from JSON object using multiple possible keys
     */
    private fun extractIntValue(jsonObject: JsonObject, possibleKeys: List<String>): Int {
        for (key in possibleKeys) {
            try {
                if (jsonObject.has(key)) {
                    val element = jsonObject.get(key)
                    if (element.isJsonPrimitive) {
                        return element.asInt
                    }
                }
            } catch (e: Exception) {
                // Continue to next key
            }
        }
        return 0
    }

    /**
     * Extract string value from JSON object using multiple possible keys
     */
    private fun extractStringValue(jsonObject: JsonObject, possibleKeys: List<String>): String? {
        for (key in possibleKeys) {
            try {
                if (jsonObject.has(key)) {
                    val element = jsonObject.get(key)
                    if (element.isJsonPrimitive && !element.isJsonNull) {
                        return element.asString
                    }
                }
            } catch (e: Exception) {
                // Continue to next key
            }
        }
        return null
    }

    /**
     * Dispose resources
     */
    fun dispose() {
        try {
            httpClient.dispatcher.executorService.shutdown()
            httpClient.connectionPool.evictAll()
        } catch (e: Exception) {
            LOG.warn("Error disposing HTTP client", e)
        }
    }
}