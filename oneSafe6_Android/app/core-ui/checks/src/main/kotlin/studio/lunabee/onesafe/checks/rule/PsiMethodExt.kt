package studio.lunabee.onesafe.checks.rule

import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.compiled.ClsFileImpl

fun PsiMethod.getPackageName(): String? = (containingFile as? ClsFileImpl)?.packageName
