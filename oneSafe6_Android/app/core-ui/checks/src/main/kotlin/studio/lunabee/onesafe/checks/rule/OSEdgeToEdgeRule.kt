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

object OSEdgeToEdgeRule : OSMethodRule {
    override val methods: List<String> = listOf(
        "enableEdgeToEdge",
    )

    override val issue: Issue = Issue.create(
        id = "NonCoreExtension",
        briefDescription = "Use `oSDefaultEnableEdgeToEdge` instead of `enableEdgeToEdge`",
        explanation = """
                    Prefer core-ui extension over activity default for consistency across the application
                    Replace enableEdgeToEdge from AndroidX.Activity by oSDefaultEnableEdgeToEdge from core-ui module
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
            if (packageName == "androidx.activity.enableEdgeToEdge") {
                context.report(
                    issue = issue,
                    scope = node,
                    location = context.getLocation(node),
                    message = "Edge to edge API must use core-ui extension instead",
                )
            }
        }
    }
}
