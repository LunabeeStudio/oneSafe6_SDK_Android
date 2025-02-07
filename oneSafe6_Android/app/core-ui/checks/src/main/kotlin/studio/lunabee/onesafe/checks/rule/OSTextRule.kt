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

object OSTextRule : OSMethodRule {
    override val methods: List<String> = listOf(
        "Text",
    )

    override val issue: Issue = Issue.create(
        id = "NonCoreUiComposable",
        briefDescription = "Use `OSText` instead of `Text`",
        explanation = """
                    Prefer core-ui composable over material3 for consistency across the application
                    Replace Text from material3 by OSText from core-ui module
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
                val quickfixData = LintFix.create().replace()
                    .name("Replace by `OSText`")
                    .sharedName("Replace all by `OSText`")
                    .text("Text")
                    .with("studio.lunabee.onesafe.atom.text.OSText")
                    .shortenNames()
                    .reformat(true)
                    .build()

                context.report(
                    issue = issue,
                    scope = node,
                    location = context.getLocation(node),
                    message = "Text must use core-ui composable instead",
                    quickfixData = quickfixData,
                )
            }
        }
    }
}
