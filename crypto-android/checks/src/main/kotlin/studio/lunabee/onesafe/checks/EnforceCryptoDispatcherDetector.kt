/*
 * Copyright (c) 2024-2024 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 3/6/2024 - for the oneSafe6 SDK.
 * Last modified 3/6/24, 6:00 PM
 */

package studio.lunabee.onesafe.checks

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiModifierListOwner
import kotlinx.coroutines.flow.Flow
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.tryResolve
import org.jetbrains.uast.visitor.AbstractUastVisitor

class EnforceCryptoDispatcherDetector : Detector(), SourceCodeScanner {

    companion object Issues {
        val SuspendCryptoDispatcherIssue: Issue = Issue.create(
            id = "EnforceCryptoDispatcherContext",
            briefDescription = "Enforce crypto dispatcher context",
            explanation = "Cryptographic operations must be run on the provided dispatcher to make sure every calls are run as expected",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                EnforceCryptoDispatcherDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
            androidSpecific = null,
        )
        val FlowCryptoDispatcherIssue: Issue = Issue.create(
            id = "EnforceCryptoDispatcherFlow",
            briefDescription = "Enforce crypto dispatcher flow",
            explanation = "Cryptographic operations must be run on the provided dispatcher to make sure every calls are run as expected",
            category = Category.CORRECTNESS,
            priority = 8,
            severity = Severity.ERROR,
            implementation = Implementation(
                EnforceCryptoDispatcherDetector::class.java,
                Scope.JAVA_FILE_SCOPE,
            ),
            androidSpecific = null,
        )

        private const val withContextFun = "withContext"
        private const val flowOnFun = "flowOn"
    }

    override fun getApplicableUastTypes(): List<Class<out UElement>> {
        return listOf(UMethod::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return CheckDispatcherUElementHandler(context)
    }

    // It looks like we cannot use reflection to get class names
    private val dispatcherAnnotationName = "CryptoDispatcher"
    private val mainCryptoRepositoryQualified = "studio.lunabee.onesafe.domain.repository.MainCryptoRepository"
    private val dispatcherAnnotationQualified = "studio.lunabee.onesafe.domain.qualifier.CryptoDispatcher"

    inner class CheckDispatcherUElementHandler(private val context: JavaContext) : UElementHandler() {

        override fun visitMethod(node: UMethod) {
            // Not sure if this the good way and place to dismiss unwanted class
            if (node.containingClass?.interfaces?.any { it.qualifiedName == mainCryptoRepositoryQualified } != true) {
                return
            }

            if (context.evaluator.isSuspend(node) && context.evaluator.isPublic(node)) {
                val withContextCallUastVisitor = CryptoDispatcherCheckUastVisitor(context, withContextFun)
                node.uastBody?.accept(withContextCallUastVisitor)
                if (!withContextCallUastVisitor.hasExpectedCall) {
                    context.report(
                        issue = SuspendCryptoDispatcherIssue,
                        location = context.getNameLocation(node),
                        message = "Expected call to `$withContextFun($dispatcherAnnotationName)` not found",
                    )
                }
            } else if (context.evaluator.isPublic(node) &&
                context.evaluator.getTypeClass(node.returnType)?.qualifiedName == Flow::class.qualifiedName
            ) {
                val flowOnCallUastVisitor = CryptoDispatcherCheckUastVisitor(context, flowOnFun)
                node.uastBody?.accept(flowOnCallUastVisitor)
                if (!flowOnCallUastVisitor.hasExpectedCall) {
                    context.report(
                        issue = FlowCryptoDispatcherIssue,
                        location = context.getNameLocation(node),
                        message = "Expected call to `$flowOnFun($dispatcherAnnotationName)` not found",
                    )
                }
            }
        }
    }

    inner class CryptoDispatcherCheckUastVisitor(private val context: JavaContext, private val funName: String) : AbstractUastVisitor() {

        var hasExpectedCall: Boolean = false

        override fun visitCallExpression(node: UCallExpression): Boolean {
            if (!hasExpectedCall) {
                hasExpectedCall = if (node.methodIdentifier?.name == funName) {
                    (node.valueArguments.first().tryResolve() as? PsiModifierListOwner)?.let { firstArg ->
                        context.evaluator.getAnnotations(firstArg)
                            .any { it.qualifiedName == dispatcherAnnotationQualified }
                    } ?: false
                } else {
                    false
                }
            }
            return hasExpectedCall
        }
    }
}
