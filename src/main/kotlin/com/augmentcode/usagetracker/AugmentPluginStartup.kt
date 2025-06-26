package com.augmentcode.usagetracker

import com.augmentcode.usagetracker.service.AugmentService
import com.augmentcode.usagetracker.service.AuthManager
import com.augmentcode.usagetracker.util.Constants
import com.augmentcode.usagetracker.util.StatusBarDiagnostics
import com.augmentcode.usagetracker.util.VersionCompatibilityChecker
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
            LOG.info("=== Augment Usage Tracker Plugin Initialization Started ===")
            LOG.info("Project: ${project.name}")

            // Check version compatibility first
            VersionCompatibilityChecker.logDetailedInfo()
            val compatibility = VersionCompatibilityChecker.checkCompatibility()

            if (compatibility.compatibility == VersionCompatibilityChecker.CompatibilityLevel.UNSUPPORTED) {
                LOG.error("Unsupported IDE version: ${compatibility.fullVersion}")
                return
            }

            // Initialize services in order
            LOG.info("Initializing services...")
            AuthManager.getInstance() // Ensure auth manager is initialized
            val augmentService = AugmentService.getInstance()
            LOG.info("Services initialized successfully")

            // Apply initial configuration
            applyInitialConfiguration(augmentService)

            // Wait longer for newer versions to ensure UI is fully initialized
            val waitTime = if (compatibility.requiresSpecialHandling) 5000L else 2000L
            LOG.info("Waiting ${waitTime}ms for UI initialization...")
            delay(waitTime)

            // Perform status bar diagnostics and attempt to fix issues
            ApplicationManager.getApplication().invokeLater {
                performStatusBarDiagnostics(project, compatibility)
            }

            LOG.info("=== Augment Usage Tracker Plugin Initialization Completed ===")

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
    private fun performStatusBarDiagnostics(project: Project, compatibility: VersionCompatibilityChecker.CompatibilityInfo) {
        try {
            LOG.info("Performing status bar diagnostics for project: ${project.name}")
            LOG.info("IDE Version: ${compatibility.fullVersion}")
            LOG.info("Requires Special Handling: ${compatibility.requiresSpecialHandling}")

            val diagnosticResult = StatusBarDiagnostics.performDiagnostics(project)

            if (!diagnosticResult.success) {
                LOG.warn("Status bar diagnostics found issues:")
                diagnosticResult.issues.forEach { issue ->
                    LOG.warn("  - $issue")
                }

                // For newer versions, try multiple times with delays
                val maxAttempts = if (compatibility.requiresSpecialHandling) 3 else 1
                var forceEnableSuccess = false

                for (attempt in 1..maxAttempts) {
                    LOG.info("Attempting to force enable status bar widget (attempt $attempt/$maxAttempts)...")
                    forceEnableSuccess = StatusBarDiagnostics.forceEnableWidget(project)

                    if (forceEnableSuccess) {
                        LOG.info("Successfully force-enabled status bar widget on attempt $attempt")
                        break
                    } else if (attempt < maxAttempts) {
                        LOG.warn("Attempt $attempt failed, waiting 2 seconds before retry...")
                        Thread.sleep(2000)
                    }
                }

                if (!forceEnableSuccess) {
                    LOG.error("Failed to force-enable status bar widget after $maxAttempts attempts")

                    // Log diagnostic report for user
                    val report = StatusBarDiagnostics.getDiagnosticReport(project)
                    LOG.info("Diagnostic Report:\n$report")

                    // For 2025.2+, log additional troubleshooting info
                    if (compatibility.requiresSpecialHandling) {
                        LOG.info("=== IntelliJ IDEA 2025.2+ Troubleshooting ===")
                        LOG.info("This version may require manual intervention:")
                        LOG.info("1. Go to Settings → Tools → Augment Usage Tracker")
                        LOG.info("2. Click 'Diagnose Status Bar' button")
                        LOG.info("3. If still not working, restart IDE")
                        LOG.info("4. Check if status bar is enabled in View → Appearance → Status Bar")
                    }
                }
            } else {
                LOG.info("Status bar diagnostics passed - widget should be visible")
            }

        } catch (e: Exception) {
            LOG.error("Error during status bar diagnostics", e)
        }
    }

}
