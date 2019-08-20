package org.arend.toolWindow

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory


class ArendMessagesFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        ServiceManager.getService(project, ArendMessagesView::class.java).initView(toolWindow)
    }

    override fun isDoNotActivateOnStart() = true
}