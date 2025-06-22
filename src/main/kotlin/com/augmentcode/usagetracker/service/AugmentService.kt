package com.augmentcode.usagetracker.service

import com.augmentcode.usagetracker.model.ApiResponse
import com.augmentcode.usagetracker.model.UsageData
import com.augmentcode.usagetracker.model.UserInfo
import com.augmentcode.usagetracker.util.Constants
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Core service for managing Augment usage data
 */
@Service(Service.Level.APP)
class AugmentService {
    
    companion object {
        private val LOG = Logger.getInstance(AugmentService::class.java)
        
        fun getInstance(): AugmentService {
            return ApplicationManager.getApplication().getService(AugmentService::class.java)
        }
    }
    
    private val authManager: AuthManager by lazy { AuthManager.getInstance() }
    private val apiClient: AugmentApiClient by lazy { AugmentApiClient() }
    
    private val currentUsageData = AtomicReference<UsageData>(UsageData())
    private val currentUserInfo = AtomicReference<UserInfo?>(null)
    private val isRefreshing = AtomicBoolean(false)
    private val isEnabled = AtomicBoolean(true)
    
    private var refreshExecutor: ScheduledExecutorService? = null
    private var refreshIntervalSeconds = Constants.DEFAULT_REFRESH_INTERVAL
    
    // Listeners for data changes
    private val dataChangeListeners = mutableListOf<(UsageData) -> Unit>()
    private val userInfoChangeListeners = mutableListOf<(UserInfo?) -> Unit>()
    
    init {
        LOG.info("AugmentService initialized")
        startAutoRefresh()
    }
    
    /**
     * Get current usage data
     */
    fun getCurrentUsageData(): UsageData {
        return currentUsageData.get()
    }
    
    /**
     * Get current user information
     */
    fun getCurrentUserInfo(): UserInfo? {
        return currentUserInfo.get()
    }
    
    /**
     * Check if service is enabled
     */
    fun isEnabled(): Boolean {
        return isEnabled.get()
    }
    
    /**
     * Enable or disable the service
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled.set(enabled)
        if (enabled) {
            startAutoRefresh()
        } else {
            stopAutoRefresh()
        }
        LOG.info("AugmentService enabled: $enabled")
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return authManager.isAuthenticated()
    }
    
    /**
     * Refresh usage data manually
     */
    fun refreshData(): CompletableFuture<Boolean> {
        if (!isEnabled() || !isAuthenticated()) {
            return CompletableFuture.completedFuture(false)
        }
        
        if (isRefreshing.compareAndSet(false, true)) {
            return CompletableFuture.supplyAsync {
                try {
                    LOG.info("Starting manual data refresh")
                    val success = performDataRefresh()
                    LOG.info("Manual data refresh completed: $success")
                    success
                } catch (e: Exception) {
                    LOG.error("Error during manual data refresh", e)
                    false
                } finally {
                    isRefreshing.set(false)
                }
            }
        } else {
            LOG.debug("Data refresh already in progress, skipping")
            return CompletableFuture.completedFuture(false)
        }
    }
    
    /**
     * Set refresh interval in seconds
     */
    fun setRefreshInterval(seconds: Int) {
        if (seconds < 5 || seconds > 300) {
            LOG.warn("Invalid refresh interval: $seconds, using default")
            return
        }
        
        refreshIntervalSeconds = seconds
        LOG.info("Refresh interval set to $seconds seconds")
        
        // Restart auto refresh with new interval
        if (isEnabled()) {
            stopAutoRefresh()
            startAutoRefresh()
        }
    }
    
    /**
     * Add listener for usage data changes
     */
    fun addDataChangeListener(listener: (UsageData) -> Unit) {
        dataChangeListeners.add(listener)
    }
    
    /**
     * Add listener for user info changes
     */
    fun addUserInfoChangeListener(listener: (UserInfo?) -> Unit) {
        userInfoChangeListeners.add(listener)
    }
    
    /**
     * Remove data change listener
     */
    fun removeDataChangeListener(listener: (UsageData) -> Unit) {
        dataChangeListeners.remove(listener)
    }
    
    /**
     * Remove user info change listener
     */
    fun removeUserInfoChangeListener(listener: (UserInfo?) -> Unit) {
        userInfoChangeListeners.remove(listener)
    }
    
    /**
     * Start automatic data refresh
     */
    private fun startAutoRefresh() {
        if (!isEnabled()) return
        
        stopAutoRefresh() // Stop existing scheduler if any
        
        refreshExecutor = ScheduledThreadPoolExecutor(1).apply {
            scheduleAtFixedRate(
                { performAutoRefresh() },
                0, // Initial delay
                refreshIntervalSeconds.toLong(),
                TimeUnit.SECONDS
            )
        }
        
        LOG.info("Auto refresh started with interval: ${refreshIntervalSeconds}s")
    }
    
    /**
     * Stop automatic data refresh
     */
    private fun stopAutoRefresh() {
        refreshExecutor?.let { executor ->
            executor.shutdown()
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow()
                }
            } catch (e: InterruptedException) {
                executor.shutdownNow()
                Thread.currentThread().interrupt()
            }
        }
        refreshExecutor = null
        LOG.info("Auto refresh stopped")
    }
    
    /**
     * Perform automatic refresh (with error handling)
     */
    private fun performAutoRefresh() {
        if (!isEnabled() || !isAuthenticated() || isRefreshing.get()) {
            return
        }
        
        try {
            if (isRefreshing.compareAndSet(false, true)) {
                performDataRefresh()
            }
        } catch (e: Exception) {
            LOG.error("Error during auto refresh", e)
        } finally {
            isRefreshing.set(false)
        }
    }
    
    /**
     * Perform the actual data refresh
     */
    private fun performDataRefresh(): Boolean {
        try {
            LOG.debug("Fetching usage data from API")
            
            // Fetch usage data and user info in parallel
            val usageResponse = apiClient.getUsageData()
            val userResponse = apiClient.getUserInfo()
            
            var success = false
            
            // Process usage data
            if (usageResponse.success && usageResponse.data != null) {
                val newUsageData = usageResponse.data.copy(lastUpdate = LocalDateTime.now())
                currentUsageData.set(newUsageData)
                notifyDataChangeListeners(newUsageData)
                success = true
                LOG.debug("Usage data updated: ${newUsageData.totalUsage}/${newUsageData.usageLimit}")
            } else {
                LOG.warn("Failed to fetch usage data: ${usageResponse.error}")
            }
            
            // Process user info
            if (userResponse.success && userResponse.data != null) {
                currentUserInfo.set(userResponse.data)
                notifyUserInfoChangeListeners(userResponse.data)
                LOG.debug("User info updated: ${userResponse.data.email}")
            } else {
                LOG.warn("Failed to fetch user info: ${userResponse.error}")
            }
            
            return success
        } catch (e: Exception) {
            LOG.error("Error performing data refresh", e)
            return false
        }
    }
    
    /**
     * Notify all data change listeners
     */
    private fun notifyDataChangeListeners(data: UsageData) {
        ApplicationManager.getApplication().invokeLater {
            dataChangeListeners.forEach { listener ->
                try {
                    listener(data)
                } catch (e: Exception) {
                    LOG.error("Error in data change listener", e)
                }
            }
        }
    }
    
    /**
     * Notify all user info change listeners
     */
    private fun notifyUserInfoChangeListeners(userInfo: UserInfo?) {
        ApplicationManager.getApplication().invokeLater {
            userInfoChangeListeners.forEach { listener ->
                try {
                    listener(userInfo)
                } catch (e: Exception) {
                    LOG.error("Error in user info change listener", e)
                }
            }
        }
    }
    
    /**
     * Dispose resources
     */
    fun dispose() {
        LOG.info("Disposing AugmentService")
        stopAutoRefresh()
        dataChangeListeners.clear()
        userInfoChangeListeners.clear()
    }
}
