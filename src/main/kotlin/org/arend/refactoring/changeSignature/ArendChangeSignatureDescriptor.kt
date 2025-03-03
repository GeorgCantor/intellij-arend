package org.arend.refactoring.changeSignature

import com.intellij.refactoring.changeSignature.MethodDescriptor
import org.arend.psi.ext.ArendDefClass
import org.arend.psi.ext.PsiLocatedReferable

class ArendChangeSignatureDescriptor(private val psiReferable: PsiLocatedReferable) :
    MethodDescriptor<ArendParameterInfo, String> {
    override fun getName(): String = psiReferable.name ?: ""

    override fun getParameters(): List<ArendParameterInfo> = ArendChangeInfo.getParameterInfo(psiReferable)

    override fun getParametersCount(): Int = parameters.size

    override fun getVisibility() = ""

    override fun getMethod() = psiReferable

    override fun canChangeVisibility() = false

    override fun canChangeParameters() = true

    override fun canChangeName() = true

    override fun canChangeReturnType(): MethodDescriptor.ReadWriteOption = if (psiReferable !is ArendDefClass) MethodDescriptor.ReadWriteOption.ReadWrite else MethodDescriptor.ReadWriteOption.Read
}