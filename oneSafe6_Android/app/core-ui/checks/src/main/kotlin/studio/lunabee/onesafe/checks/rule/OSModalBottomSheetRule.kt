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

object OSModalBottomSheetRule : OSMethodRule {
    override val methods: List<String> = listOf(
        "ModalBottomSheet",
    )

    override val issue: Issue = Issue.create(
        id = "NonCoreUiComposable",
        briefDescription = "Use `OSModalBottomSheet` instead of `ModalBottomSheet`",
        explanation = """
                    Prefer core-ui composable over material3 for consistency across the application
                    Replace ModalBottomSheet from material3 by OSModalBottomSheet from core-ui module
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
            if (packageName == "androidx.compose.material3") {
                val quickfixData = LintFix.create().replace()
                    .name("Replace by `OSModalBottomSheet`")
                    .sharedName("Replace all by `OSModalBottomSheet`")
                    .text("ModalBottomSheet")
                    .with("studio.lunabee.onesafe.bottomsheet.OSModalBottomSheet")
                    .shortenNames()
                    .reformat(true)
                    .build()

                context.report(
                    issue = issue,
                    scope = node,
                    location = context.getLocation(node),
                    message = "ModalBottomSheet must use core-ui composable instead",
                    quickfixData = quickfixData,
                )
            }
        }
    }
}
