package org.arend.intention

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.rd.framework.base.deepClonePolymorphic
import org.arend.core.context.binding.Binding
import org.arend.core.expr.Expression
import org.arend.core.expr.visitor.FreeVariablesCollector
import org.arend.error.DummyErrorReporter
import org.arend.naming.reference.TCReferable
import org.arend.psi.ArendFieldAcc
import org.arend.psi.ArendGoal
import org.arend.psi.ext.TCDefinition
import org.arend.psi.parentOfType
import org.arend.resolving.PsiConcreteProvider
import org.arend.typechecking.ArendTypechecking
import org.arend.typechecking.TypeCheckingService
import org.arend.typechecking.error.ErrorService
import org.arend.typechecking.error.local.GoalError
import org.arend.typechecking.visitor.CheckTypeVisitor
import org.arend.util.ArendBundle

class GenerateFunctionIntention : BaseArendIntention(ArendBundle.message("arend.generate.function")) {
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return element.parentOfType<ArendGoal>(false) != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        editor ?: return
        val goal = element.parentOfType<ArendGoal>(false) ?: return
        val errorService = project.service<ErrorService>()
        val arendError = errorService.errors[element.containingFile]?.firstOrNull { it.cause == goal } ?: return
        val expectedGoalType = (arendError.error as? GoalError)?.expectedType
        val freeVariables = FreeVariablesCollector.getFreeVariables(expectedGoalType)
        // todo: sort
        // todo: convert to psi
    }
}
