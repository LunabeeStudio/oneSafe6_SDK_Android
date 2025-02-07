package studio.lunabee.onesafe.checks.rule

import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

interface OSMethodRule {
    val methods: List<String>
    val issue: Issue
    operator fun invoke(context: JavaContext, node: UCallExpression, method: PsiMethod)
}
