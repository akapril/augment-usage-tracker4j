package com.augmentcode.usagetracker.service

import com.augmentcode.usagetracker.util.Constants
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Authentication manager for handling Augment credentials
 */
@Service(Service.Level.APP)
class AuthManager {
    
    companion object {
        private val LOG = Logger.getInstance(AuthManager::class.java)
        private const val SERVICE_NAME = "AugmentUsageTracker"
        private const val COOKIES_KEY = "augment_cookies"
        private const val LAST_UPDATE_KEY = "last_update"
        
        fun getInstance(): AuthManager {
            return ApplicationManager.getApplication().getService(AuthManager::class.java)
        }
    }
    
    private val isAuthenticated = AtomicBoolean(false)
    private val currentCookies = AtomicReference<String?>(null)
    private val lastUpdateTime = AtomicReference<LocalDateTime?>(null)
    
    // Listeners for authentication state changes
    private val authStateChangeListeners = mutableListOf<(Boolean) -> Unit>()
    
    init {
        LOG.info("AuthManager initialized")
        loadStoredCredentials()
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return isAuthenticated.get() && !currentCookies.get().isNullOrBlank()
    }
    
    /**
     * Get current cookies
     */
    fun getCookies(): String? {
        return currentCookies.get()
    }
    
    /**
     * Set authentication cookies
     */
    fun setCookies(cookies: String?): Boolean {
        return try {
            if (cookies.isNullOrBlank()) {
                clearCredentials()
                return true
            }
            
            val cleanedCookies = cleanCookies(cookies)
            if (validateCookies(cleanedCookies)) {
                currentCookies.set(cleanedCookies)
                lastUpdateTime.set(LocalDateTime.now())
                
                // Store credentials securely
                storeCredentials(cleanedCookies)
                
                isAuthenticated.set(true)
                notifyAuthStateChange(true)
                
                LOG.info("Authentication cookies set successfully")
                true
            } else {
                LOG.warn("Invalid cookies format provided")
                false
            }
        } catch (e: Exception) {
            LOG.error("Error setting authentication cookies", e)
            false
        }
    }
    
    /**
     * Clear authentication credentials
     */
    fun clearCredentials() {
        try {
            currentCookies.set(null)
            lastUpdateTime.set(null)
            isAuthenticated.set(false)
            
            // Clear stored credentials
            val credentialAttributes = createCredentialAttributes(COOKIES_KEY)
            PasswordSafe.instance.set(credentialAttributes, null)
            
            val updateAttributes = createCredentialAttributes(LAST_UPDATE_KEY)
            PasswordSafe.instance.set(updateAttributes, null)
            
            notifyAuthStateChange(false)
            LOG.info("Authentication credentials cleared")
        } catch (e: Exception) {
            LOG.error("Error clearing credentials", e)
        }
    }
    
    /**
     * Get last update time
     */
    fun getLastUpdateTime(): LocalDateTime? {
        return lastUpdateTime.get()
    }
    
    /**
     * Check if cookies are expired (older than 20 hours)
     */
    fun areCookiesExpired(): Boolean {
        val lastUpdate = lastUpdateTime.get() ?: return true
        val now = LocalDateTime.now()
        val hoursSinceUpdate = java.time.Duration.between(lastUpdate, now).toHours()
        return hoursSinceUpdate >= 20
    }
    
    /**
     * Add listener for authentication state changes
     */
    fun addAuthStateChangeListener(listener: (Boolean) -> Unit) {
        authStateChangeListeners.add(listener)
    }
    
    /**
     * Remove authentication state change listener
     */
    fun removeAuthStateChangeListener(listener: (Boolean) -> Unit) {
        authStateChangeListeners.remove(listener)
    }
    
    /**
     * Load stored credentials from secure storage
     */
    private fun loadStoredCredentials() {
        try {
            val credentialAttributes = createCredentialAttributes(COOKIES_KEY)
            val credentials = PasswordSafe.instance.get(credentialAttributes)
            
            if (credentials?.password != null) {
                val storedCookies = credentials.password!!.toString()
                if (validateCookies(storedCookies)) {
                    currentCookies.set(storedCookies)
                    isAuthenticated.set(true)
                    
                    // Load last update time
                    loadLastUpdateTime()
                    
                    LOG.info("Stored authentication credentials loaded successfully")
                } else {
                    LOG.warn("Stored cookies are invalid, clearing them")
                    clearCredentials()
                }
            } else {
                LOG.info("No stored authentication credentials found")
            }
        } catch (e: Exception) {
            LOG.error("Error loading stored credentials", e)
        }
    }
    
    /**
     * Load last update time from storage
     */
    private fun loadLastUpdateTime() {
        try {
            val updateAttributes = createCredentialAttributes(LAST_UPDATE_KEY)
            val updateCredentials = PasswordSafe.instance.get(updateAttributes)
            
            if (updateCredentials?.password != null) {
                val timeString = updateCredentials.password!!.toString()
                val dateTime = LocalDateTime.parse(timeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                lastUpdateTime.set(dateTime)
                LOG.debug("Last update time loaded: $dateTime")
            }
        } catch (e: Exception) {
            LOG.warn("Error loading last update time", e)
            lastUpdateTime.set(LocalDateTime.now())
        }
    }
    
    /**
     * Store credentials securely
     */
    private fun storeCredentials(cookies: String) {
        try {
            val credentialAttributes = createCredentialAttributes(COOKIES_KEY)
            val credentials = Credentials(COOKIES_KEY, cookies)
            PasswordSafe.instance.set(credentialAttributes, credentials)
            
            // Store last update time
            val updateTime = LocalDateTime.now()
            val updateAttributes = createCredentialAttributes(LAST_UPDATE_KEY)
            val updateCredentials = Credentials(LAST_UPDATE_KEY, updateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            PasswordSafe.instance.set(updateAttributes, updateCredentials)
            
            LOG.debug("Credentials stored securely")
        } catch (e: Exception) {
            LOG.error("Error storing credentials", e)
        }
    }
    
    /**
     * Create credential attributes for secure storage
     */
    private fun createCredentialAttributes(key: String): CredentialAttributes {
        return CredentialAttributes(generateServiceName(SERVICE_NAME, key))
    }
    
    /**
     * Clean and normalize cookies string
     */
    private fun cleanCookies(cookies: String): String {
        return cookies.trim()
            .replace("\n", "")
            .replace("\r", "")
            .replace("\\s+".toRegex(), " ")
    }
    
    /**
     * Validate cookies format
     */
    private fun validateCookies(cookies: String): Boolean {
        if (cookies.isBlank()) return false
        
        // Check for _session cookie (main requirement)
        val hasSessionCookie = cookies.contains("_session=") || 
                              cookies.startsWith("eyJ") // JWT token format
        
        if (!hasSessionCookie) {
            LOG.warn("Cookies validation failed: no _session cookie found")
            return false
        }
        
        // Basic length check
        if (cookies.length < 50) {
            LOG.warn("Cookies validation failed: too short")
            return false
        }
        
        LOG.debug("Cookies validation passed")
        return true
    }
    
    /**
     * Notify all authentication state change listeners
     */
    private fun notifyAuthStateChange(isAuthenticated: Boolean) {
        ApplicationManager.getApplication().invokeLater {
            authStateChangeListeners.forEach { listener ->
                try {
                    listener(isAuthenticated)
                } catch (e: Exception) {
                    LOG.error("Error in auth state change listener", e)
                }
            }
        }
    }
    
    /**
     * Get authentication status summary
     */
    fun getAuthStatusSummary(): String {
        return buildString {
            append("Authenticated: ${isAuthenticated()}")
            if (isAuthenticated()) {
                append("\nCookies length: ${currentCookies.get()?.length ?: 0}")
                append("\nLast update: ${lastUpdateTime.get()?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: "Unknown"}")
                append("\nExpired: ${areCookiesExpired()}")
            }
        }
    }
    
    /**
     * Dispose resources
     */
    fun dispose() {
        LOG.info("Disposing AuthManager")
        authStateChangeListeners.clear()
    }
}
