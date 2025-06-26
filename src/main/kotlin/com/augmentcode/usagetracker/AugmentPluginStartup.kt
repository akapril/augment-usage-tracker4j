package com.augmentcode.usagetracker

import com.augmentcode.usagetracker.service.AugmentService
import com.augmentcode.usagetracker.service.AuthManager
import com.augmentcode.usagetracker.util.Constants
import com.augmentcode.usagetracker.util.StatusBarDiagnostics
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.delay

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

            // Wait a bit for the UI to be fully initialized
            delay(2000)

            // Perform status bar diagnostics and attempt to fix issues
            ApplicationManager.getApplication().invokeLater {
                performStatusBarDiagnostics(project)
            }

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

    /**
     * Perform status bar diagnostics and attempt to fix issues
     * 执行状态栏诊断并尝试修复问题
     */
    private fun performStatusBarDiagnostics(project: Project) {
        try {
            LOG.info("Performing status bar diagnostics for project: ${project.name}")

            val diagnosticResult = StatusBarDiagnostics.performDiagnostics(project)

            if (!diagnosticResult.success) {
                LOG.warn("Status bar diagnostics found issues:")
                diagnosticResult.issues.forEach { issue ->
                    LOG.warn("  - $issue")
                }

                LOG.info("Attempting to force enable status bar widget...")
                val forceEnableSuccess = StatusBarDiagnostics.forceEnableWidget(project)

                if (forceEnableSuccess) {
                    LOG.info("Successfully force-enabled status bar widget")
                } else {
                    LOG.error("Failed to force-enable status bar widget")

                    // Log diagnostic report for user
                    val report = StatusBarDiagnostics.getDiagnosticReport(project)
                    LOG.info("Diagnostic Report:\n$report")
                }
            } else {
                LOG.info("Status bar diagnostics passed - widget should be visible")
            }

        } catch (e: Exception) {
            LOG.error("Error during status bar diagnostics", e)
        }
    }

}
