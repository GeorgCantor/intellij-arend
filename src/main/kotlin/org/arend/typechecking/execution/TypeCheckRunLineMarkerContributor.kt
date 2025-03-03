package org.arend.typechecking.execution

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.arend.core.definition.Definition.TypeCheckingStatus.*
import org.arend.psi.ArendElementTypes
import org.arend.psi.ext.*

class TypeCheckRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (!(element is LeafPsiElement && element.node.elementType == ArendElementTypes.ID)) {
            return null
        }

        val parent = when (val id = element.parent) {
            is ArendDefIdentifier -> id.parent as? TCDefinition
            is ArendRefIdentifier -> ((id.parent as? ArendLongName)?.parent as? ArendCoClause)?.functionReference
            else -> null
        } ?: return null

        val def = parent.tcReferable?.typechecked
        val icon = when (def?.status()) {
            NO_ERRORS, DEP_ERRORS, DEP_WARNiNGS -> AllIcons.RunConfigurations.TestState.Green2
            HAS_WARNINGS -> AllIcons.RunConfigurations.TestState.Yellow2
            null, TYPE_CHECKING, NEEDS_TYPE_CHECKING -> AllIcons.RunConfigurations.TestState.Run
            else -> AllIcons.RunConfigurations.TestState.Red2
        }

        return Info(
                icon,
                { runReadAction {
                    if (parent.isValid) "Typecheck ${parent.fullName}" else "Typecheck definition"
                } },
                *ExecutorAction.getActions(1)
        )
    }
}
