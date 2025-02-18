package studio.lunabee.onesafe.checks.rule

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import studio.lunabee.onesafe.checks.OSCodeDetector

object OSSwitchRule : OSMethodRule {
    override val methods: List<String> = listOf(
        "Switch",
    )

    override val issue: Issue = Issue.create(
        id = "NonCoreUiComposable",
        briefDescription = "Use `OSSwitch` instead of `Switch`",
        explanation = """
                    Prefer core-ui composable over material3 for consistency across the application
                    Replace Switch from material3 by OSSwitch from core-ui module
                    """,
        category = Category.CORRECTNESS,
        priority = 6,
        severity = Severity.WARNING,
        implementation = Implementation(
            OSCodeDetector::class.java,
            Scope.JAVA_FILE_SCOPE,
        ),
    )

    override operator fun invoke(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        method.getPackageName()?.let { packageName ->
            if (packageName.matches("androidx\\.compose\\.material\\d?".toRegex())) {
                context.report(
                    issue = issue,
                    scope = node,
                    location = context.getLocation(node),
                    message = "Switch must use core-ui composable instead",
                )
            }
        }
    }
}
