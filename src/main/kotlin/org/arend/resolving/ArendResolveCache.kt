package org.arend.resolving

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.util.containers.ContainerUtil
import org.arend.naming.reference.Referable
import org.arend.naming.reference.TCDefReferable
import org.arend.psi.ext.ArendReferenceElement
import org.arend.typechecking.TypeCheckingService
import java.util.concurrent.ConcurrentMap

class ArendResolveCache(project: Project) {
    private val typeCheckingService = project.service<TypeCheckingService>()
    private val refMap: ConcurrentMap<ArendReferenceElement, Referable> = ContainerUtil.createConcurrentWeakKeySoftValueMap()
    private val refMapPsi: ConcurrentMap<ArendReferenceElement, Pair<Int, SmartPsiElementPointer<PsiElement>>> = ContainerUtil.createConcurrentWeakMap()

    fun getCached(reference: ArendReferenceElement): Referable? {
        val ref = refMap[reference]
        if (ref != null && ref != TCDefReferable.NULL_REFERABLE) return ref

        val entry = refMapPsi[reference]
        val psi = entry?.second?.element
        val code = entry?.first

        if (code != null && code != reference.text.hashCode() || psi != null && !psi.isValid) { // Cached value is probably incorrect/broken
            dropCache(reference)
            return null
        }

        if (psi is Referable) {
            return psi
        }

        return null
    }

    fun resolveCached(resolver: () -> Referable?, reference: ArendReferenceElement): Referable? {
        val ref = getCached(reference)
        if (ref != null) return ref

        val result = resolver()
        if (result == null && !typeCheckingService.isInitialized) {
            return null
        }

        doReplaceCache(result, reference)

        return result
    }

    fun replaceCache(newRef: Referable?, reference: ArendReferenceElement): Referable? {
        val oldValue = getCached(reference)
        doReplaceCache(newRef, reference)
        return oldValue
    }

    private fun doReplaceCache(newRef: Referable?, reference: ArendReferenceElement) {
        if (newRef is PsiElement && newRef.isValid) {
            refMapPsi[reference] = Pair(reference.text.hashCode(), SmartPointerManager.createPointer(newRef))
        } else
            refMap[reference] = newRef ?: TCDefReferable.NULL_REFERABLE
    }

    fun dropCache(reference: ArendReferenceElement) {
        refMap.remove(reference)
        refMapPsi.remove(reference)
    }

    fun clear() {
        refMap.clear()
        refMapPsi.clear()
    }
}