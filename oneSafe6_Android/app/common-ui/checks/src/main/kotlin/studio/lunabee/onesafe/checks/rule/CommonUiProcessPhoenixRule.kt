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
import studio.lunabee.onesafe.checks.CommonUiCodeDetector

object CommonUiProcessPhoenixRule : CommonUiMethodRule {
    override val methods: List<String> = listOf(
        "triggerRebirth",
    )

    override val issue: Issue = Issue.create(
        id = "MultiProcessRebirth",
        briefDescription = "Use `OSProcessPhoenix` instead of `ProcessPhoenix`",
        explanation = """
                    Use `OSProcessPhoenix` to support multi-process kill
                    See https://github.com/JakeWharton/ProcessPhoenix/issues/1
                    """,
        category = Category.CORRECTNESS,
        priority = 6,
        severity = Severity.ERROR,
        implementation = Implementation(
            CommonUiCodeDetector::class.java,
            Scope.JAVA_FILE_SCOPE,
        ),
    )

    override operator fun invoke(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        method.getPackageName()?.let { packageName ->
            if (packageName == "com.jakewharton.processphoenix") {
                val quickfixData = LintFix.create().replace()
                    .name("Replace by `OSProcessPhoenix.triggerRebirth`")
                    .sharedName("Replace all by `OSProcessPhoenix.triggerRebirth`")
                    .text("ProcessPhoenix.triggerRebirth")
                    .with("studio.lunabee.onesafe.commonui.utils.OSProcessPhoenix.triggerRebirth")
                    .shortenNames()
                    .reformat(true)
                    .build()

                context.report(
                    issue = issue,
                    scope = node,
                    location = context.getLocation(node),
                    message = "Use `OSProcessPhoenix` to support multi-process kill",
                    quickfixData = quickfixData,
                )
            }
        }
    }
}
