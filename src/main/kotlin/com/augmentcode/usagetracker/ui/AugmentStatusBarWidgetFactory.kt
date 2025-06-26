package com.augmentcode.usagetracker.ui

import com.augmentcode.usagetracker.util.Constants
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import org.jetbrains.annotations.Nls

/**
 * Factory for creating Augment status bar widgets
 */
class AugmentStatusBarWidgetFactory : StatusBarWidgetFactory {

    companion object {
        private val LOG = Logger.getInstance(AugmentStatusBarWidgetFactory::class.java)
    }

    override fun getId(): String {
        LOG.info("StatusBarWidgetFactory.getId() called, returning: ${Constants.WIDGET_ID}")
        return Constants.WIDGET_ID
    }

    override fun getDisplayName(): @Nls String {
        LOG.info("StatusBarWidgetFactory.getDisplayName() called, returning: ${Constants.WIDGET_DISPLAY_NAME}")
        return Constants.WIDGET_DISPLAY_NAME
    }

    override fun isAvailable(project: Project): Boolean {
        LOG.info("StatusBarWidgetFactory.isAvailable() called for project: ${project.name}")
        return true
    }

    override fun createWidget(project: Project): StatusBarWidget {
        LOG.info("StatusBarWidgetFactory.createWidget() called for project: ${project.name}")
        try {
            val widget = AugmentStatusBarWidget(project)
            LOG.info("Successfully created AugmentStatusBarWidget for project: ${project.name}")
            return widget
        } catch (e: Exception) {
            LOG.error("Error creating AugmentStatusBarWidget for project: ${project.name}", e)
            throw e
        }
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        LOG.info("StatusBarWidgetFactory.disposeWidget() called for widget: ${widget.ID()}")
        if (widget is AugmentStatusBarWidget) {
            widget.dispose()
            LOG.info("Successfully disposed AugmentStatusBarWidget")
        }
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean {
        LOG.info("StatusBarWidgetFactory.canBeEnabledOn() called")
        return true
    }
}
