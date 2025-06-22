package com.augmentcode.usagetracker

import com.augmentcode.usagetracker.service.AugmentService
import com.augmentcode.usagetracker.service.AuthManager
import com.augmentcode.usagetracker.util.Constants
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Plugin startup activity to initialize services
 */
class AugmentPluginStartup : ProjectActivity {

    companion object {
        private val LOG = Logger.getInstance(AugmentPluginStartup::class.java)
    }

    override suspend fun execute(project: Project) {
        try {
            LOG.info("Initializing Augment Usage Tracker plugin for project: ${project.name}")
            
            // Initialize services in order
            AuthManager.getInstance() // Ensure auth manager is initialized
            val augmentService = AugmentService.getInstance()
            
            // Apply initial configuration
            applyInitialConfiguration(augmentService)

            LOG.info("Augment Usage Tracker plugin initialized successfully")
            
        } catch (e: Exception) {
            LOG.error("Error initializing Augment Usage Tracker plugin", e)
        }
    }
    
    /**
     * Apply initial configuration from stored settings
     */
    private fun applyInitialConfiguration(augmentService: AugmentService) {
        // Apply enabled state (default to true)
        val enabled = true // We'll use default for now
        augmentService.setEnabled(enabled)
        
        // Apply refresh interval (use default)
        val refreshInterval = Constants.DEFAULT_REFRESH_INTERVAL
        augmentService.setRefreshInterval(refreshInterval)
        
        LOG.info("Initial configuration applied: enabled=$enabled, refreshInterval=${refreshInterval}s")
    }


}
