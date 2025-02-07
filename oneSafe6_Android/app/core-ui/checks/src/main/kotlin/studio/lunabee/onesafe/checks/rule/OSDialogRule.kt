package studio.lunabee.onesafe.checks.rule

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import studio.lunabee.onesafe.checks.OSCodeDetector

object OSDialogRule : OSMethodRule {
    override val methods: List<String> = listOf(
        "Dialog",
    )

    override val issue: Issue = Issue.create(
        id = "NonCoreUiComposable",
        briefDescription = "Use `OSDialog` instead of `Dialog`",
        explanation = """
                    Prefer core-ui composable over material3 for consistency across the application
                    Replace `Dialog` from compose ui by `OSDialog` from core-ui module
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
            if (packageName == "androidx.compose.ui.window") {
                val quickfixData = LintFix.create().replace()
                    .name("Replace by `OSDialog`")
                    .sharedName("Replace all by `OSDialog`")
                    .text("Dialog")
                    .with("studio.lunabee.onesafe.dialog.OSDialog")
                    .shortenNames()
                    .reformat(true)
                    .build()

                context.report(
                    issue = issue,
                    scope = node,
                    location = context.getLocation(node),
                    message = "Dialog must use core-ui composable instead",
                    quickfixData = quickfixData,
                )
            }
        }
    }
}
