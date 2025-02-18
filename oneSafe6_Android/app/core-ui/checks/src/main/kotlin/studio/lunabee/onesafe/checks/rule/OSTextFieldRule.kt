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

object OSTextFieldRule : OSMethodRule {
    override val methods: List<String> = listOf(
        "OutlinedTextField",
        "BasicTextField",
    )

    override val issue: Issue = Issue.create(
        id = "NonCoreUiComposable",
        briefDescription = "Use core-ui composable instead of material",
        explanation = """
                    Prefer core-ui composable over material3 for consistency across the application
                    Use text field from core-ui module
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
            if (packageName.matches("androidx\\.compose\\.material\\d?||androidx\\.compose\\.foundation\\.text".toRegex())) {
                val quickfixData = when (node.methodName) {
                    "OutlinedTextField" -> {
                        LintFix.create().replace()
                            .name("Replace by `OSTextField`")
                            .sharedName("Replace all by `OSTextField`")
                            .text("OutlinedTextField")
                            .with("studio.lunabee.onesafe.atom.textfield.OSTextField")
                    }
                    "BasicTextField" -> {
                        LintFix.create().replace()
                            .name("Replace by `OSBasicTextField`")
                            .sharedName("Replace all by `OSBasicTextField`")
                            .text("BasicTextField")
                            .with("studio.lunabee.onesafe.atom.textfield.OSBasicTextField")
                    }
                    else -> null
                }
                    ?.shortenNames()
                    ?.reformat(true)
                    ?.build()

                context.report(
                    issue = issue,
                    scope = node,
                    location = context.getLocation(node),
                    message = "Text field must use core-ui composable instead",
                    quickfixData = quickfixData,
                )
            }
        }
    }
}
