package org.arend.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.components.service
import org.arend.settings.ArendProjectSettings

object ArendNormalizeToggleAction : ToggleAction("Normalize") {
    override fun isSelected(e: AnActionEvent) =
            e.project?.run { service<ArendProjectSettings>().data.normalizePopup } ?: false

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        e.project?.run { service<ArendProjectSettings>().data.normalizePopup = state }
    }
}