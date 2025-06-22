package com.augmentcode.usagetracker.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for UsageData model
 */
class UsageDataTest {
    
    @Test
    fun testUsagePercentageCalculation() {
        val data1 = UsageData(totalUsage = 500, usageLimit = 1000)
        assertEquals("Usage percentage should be 50%", 50, data1.getUsagePercentage())
        
        val data2 = UsageData(totalUsage = 750, usageLimit = 1000)
        assertEquals("Usage percentage should be 75%", 75, data2.getUsagePercentage())
        
        val data3 = UsageData(totalUsage = 1000, usageLimit = 1000)
        assertEquals("Usage percentage should be 100%", 100, data3.getUsagePercentage())
        
        val data4 = UsageData(totalUsage = 100, usageLimit = 0)
        assertEquals("Usage percentage should be 0% when limit is 0", 0, data4.getUsagePercentage())
    }
    
    @Test
    fun testRemainingUsageCalculation() {
        val data1 = UsageData(totalUsage = 300, usageLimit = 1000)
        assertEquals("Remaining usage should be 700", 700, data1.getRemainingUsage())
        
        val data2 = UsageData(totalUsage = 1000, usageLimit = 1000)
        assertEquals("Remaining usage should be 0", 0, data2.getRemainingUsage())
        
        val data3 = UsageData(totalUsage = 1200, usageLimit = 1000)
        assertEquals("Remaining usage should be 0 when over limit", 0, data3.getRemainingUsage())
    }
    
    @Test
    fun testWarningLevels() {
        val data1 = UsageData(totalUsage = 500, usageLimit = 1000) // 50%
        assertFalse("50% usage should not be at warning level", data1.isAtWarningLevel())
        assertFalse("50% usage should not be near limit", data1.isNearLimit())
        
        val data2 = UsageData(totalUsage = 800, usageLimit = 1000) // 80%
        assertTrue("80% usage should be at warning level", data2.isAtWarningLevel())
        assertFalse("80% usage should not be near limit", data2.isNearLimit())
        
        val data3 = UsageData(totalUsage = 950, usageLimit = 1000) // 95%
        assertTrue("95% usage should be at warning level", data3.isAtWarningLevel())
        assertTrue("95% usage should be near limit", data3.isNearLimit())
    }
    
    @Test
    fun testApiResponseModel() {
        val successResponse = ApiResponse(success = true, data = "test data")
        assertTrue("Success response should be successful", successResponse.success)
        assertEquals("Success response should contain data", "test data", successResponse.data)
        assertNull("Success response should not have error", successResponse.error)
        
        val errorResponse = ApiResponse<String>(success = false, error = "test error")
        assertFalse("Error response should not be successful", errorResponse.success)
        assertNull("Error response should not contain data", errorResponse.data)
        assertEquals("Error response should contain error message", "test error", errorResponse.error)
    }
    
    @Test
    fun testUserInfoModel() {
        val userInfo = UserInfo(
            email = "test@example.com",
            name = "Test User",
            plan = "Pro"
        )
        
        assertEquals("Email should match", "test@example.com", userInfo.email)
        assertEquals("Name should match", "Test User", userInfo.name)
        assertEquals("Plan should match", "Pro", userInfo.plan)
        
        val emptyUserInfo = UserInfo()
        assertNull("Empty user info should have null email", emptyUserInfo.email)
        assertNull("Empty user info should have null name", emptyUserInfo.name)
        assertNull("Empty user info should have null plan", emptyUserInfo.plan)
    }
}
