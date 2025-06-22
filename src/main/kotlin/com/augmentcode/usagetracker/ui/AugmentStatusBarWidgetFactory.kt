package com.augmentcode.usagetracker.ui

import com.augmentcode.usagetracker.util.Constants
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import org.jetbrains.annotations.Nls

/**
 * Factory for creating Augment status bar widgets
 */
class AugmentStatusBarWidgetFactory : StatusBarWidgetFactory {
    
    override fun getId(): String = Constants.WIDGET_ID
    
    override fun getDisplayName(): @Nls String = Constants.WIDGET_DISPLAY_NAME
    
    override fun isAvailable(project: Project): Boolean = true
    
    override fun createWidget(project: Project): StatusBarWidget {
        return AugmentStatusBarWidget(project)
    }
    
    override fun disposeWidget(widget: StatusBarWidget) {
        if (widget is AugmentStatusBarWidget) {
            widget.dispose()
        }
    }
    
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}
