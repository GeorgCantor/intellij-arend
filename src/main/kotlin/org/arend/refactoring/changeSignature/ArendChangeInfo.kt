package org.arend.refactoring.changeSignature

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.refactoring.changeSignature.ChangeInfo
import com.intellij.refactoring.changeSignature.ParameterInfo
import org.arend.ArendLanguage
import org.arend.psi.*
import org.arend.psi.ext.*
import java.util.Collections.singletonList

class ArendChangeInfo (val parameterInfo : List<ArendParameterInfo>,
                       val returnType: String?,
                       val locatedReferable: PsiLocatedReferable /* TODO: Use more persistent pointers */) : ChangeInfo {
    override fun getNewParameters(): Array<ParameterInfo> = parameterInfo.toTypedArray()

    override fun isParameterSetOrOrderChanged(): Boolean = true //TODO: Implement me

    override fun isParameterTypesChanged(): Boolean = true //TODO: Implement me

    override fun isParameterNamesChanged(): Boolean = true //TODO: Implement me

    override fun isGenerateDelegate(): Boolean = false

    override fun getMethod(): PsiElement = locatedReferable

    override fun isReturnTypeChanged(): Boolean = true //TODO: Implement me

    override fun isNameChanged(): Boolean = false

    override fun getNewName(): String = ""

    override fun getLanguage(): Language = ArendLanguage.INSTANCE

    fun signature(): String { //TODO: This works poorly; fix me
        val teleEntries = ArrayList<Pair<Pair<String?, Boolean>, MutableList<String?>>>()
        val whitespaceList = ArrayList<String>()
        var lastWhitespace = " "
        when (locatedReferable) {
            is ArendDefFunction -> {
                var buffer = ""
                var currNode = locatedReferable.parameters.firstOrNull()?.findPrevSibling()?.nextSibling
                while (currNode != null) {
                    if (currNode is ArendNameTele) {
                        lastWhitespace = buffer
                        whitespaceList.add(buffer)
                        buffer = ""
                    } else {
                        buffer += currNode.text
                    }
                    currNode = currNode.nextSibling
                }
            }
        }

        for (parameter in parameterInfo) {
            if (teleEntries.isEmpty() || (teleEntries.last().first.first != parameter.typeText || teleEntries.last().first.second != parameter.isExplicit())) {
                teleEntries.add(Pair(Pair(parameter.typeText, parameter.isExplicit()), singletonList(parameter.name).toMutableList()))
            } else {
                teleEntries.last().second.add(parameter.name)
            }
        }

        val newTeles = StringBuilder()
        for ((index, entry) in teleEntries.withIndex()) {
            val whitespace = whitespaceList.getOrNull(index) ?: lastWhitespace
            if (index > 0) newTeles.append(whitespace)
            newTeles.append (if (entry.first.second) "(" else "{")
            for ((i, p) in entry.second.withIndex()) {
                if (i > 0) newTeles.append(" ")
                newTeles.append(p ?: "_")
            }
            if (entry.first.first != null)
                newTeles.append(" : ${entry.first.first}")
            newTeles.append (if (entry.first.second) ")" else "}")
        }

        return when (locatedReferable) {
            is ArendDefFunction -> "${locatedReferable.functionKw.text}${(locatedReferable.precedence as? PsiElement)?.text?.let{ " $it" } ?: ""}${locatedReferable.defIdentifier?.text?.let{ " $it"} ?: ""} $newTeles${returnType?.let { " : $it" } ?: ""}"
            else -> throw IllegalStateException()
        }
    }

    companion object {
        fun getParameterInfo(locatedReferable: PsiLocatedReferable): MutableList<ArendParameterInfo> {
            var index = 0
            val result = ArrayList<ArendParameterInfo>()
            for (t in getTeles(locatedReferable)) when (t) {
                is ArendNameTele -> for (parameter in t.identifierOrUnknownList) {
                    result.add(ArendParameterInfo(parameter.defIdentifier?.name ?: "_", t.type?.text, index, t.isExplicit))
                    index++
                }
                is ArendTypeTele -> {
                    result.add(ArendParameterInfo(null, t.typedExpr?.text, index, t.isExplicit))
                    index++
                }
                is ArendNameTeleUntyped -> {
                    result.add(ArendParameterInfo(t.defIdentifier.name, null, index, t.isExplicit))
                    index++
                }
            }
            return result
        }
        fun getTeles(locatedReferable: PsiLocatedReferable): List<PsiElement> = when (locatedReferable) {
            is ArendFunctionDefinition<*> -> locatedReferable.parameters
            is ArendDefData -> locatedReferable.parameters
            is ArendDefClass -> locatedReferable.fieldTeleList
            is ArendDefMeta -> locatedReferable.parameters
            is ArendConstructor -> locatedReferable.parameters
            else -> throw IllegalStateException()
        }
    }
}