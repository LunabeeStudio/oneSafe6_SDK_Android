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

object OSInputChipRule : OSMethodRule {
    override val methods: List<String> = listOf(
        "InputChip",
    )

    override val issue: Issue = Issue.create(
        id = "NonCoreUiComposable",
        briefDescription = "Use `OSInputChip` instead of `InputChip`",
        explanation = """
                    Prefer core-ui composable over material3 for consistency across the application
                    Replace InputChip from material3 by OSInputChip from core-ui module
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
                    .name("Replace by `OSInputChip`")
                    .sharedName("Replace all by `OSInputChip`")
                    .text("InputChip")
                    .with("studio.lunabee.onesafe.atom.text.OSInputChip")
                    .shortenNames()
                    .reformat(true)
                    .build()

                context.report(
                    issue = issue,
                    scope = node,
                    location = context.getLocation(node),
                    message = "InputChip must use core-ui composable instead",
                    quickfixData = quickfixData,
                )
            }
        }
    }
}
