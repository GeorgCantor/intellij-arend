package org.arend.codeInsight.completion

class ArendReferenceCompletionTest : ArendCompletionTestBase() {
    fun `test accessibility modifiers`() =
        checkCompletionVariants("""
          -- ! Main.ard
            \import MyModule

            \func lol => func{-caret-}
          -- ! MyModule.ard
          
          \func func_f => 101
          \private \func func_g => 102            
        """, listOf("func_f"))

    fun `test no variants delegator + access modifiers`() =
        checkCompletionVariants("""
          -- ! Main.ard

            \func lol => func{-caret-}
          -- ! MyModule.ard
          
          \func func_f => 101
          \private \func func_g => 102            
        """, listOf("func_f"))

}