/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 5/4/2023 - for the oneSafe6 SDK.
 * Last modified 5/4/23, 3:07 PM
 */

package studio.lunabee.onesafe.checks

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression
import studio.lunabee.onesafe.checks.rule.CommonUiMethodRule
import studio.lunabee.onesafe.checks.rule.CommonUiProcessPhoenixRule

class CommonUiCodeDetector : Detector(), SourceCodeScanner {

    override fun getApplicableMethodNames(): List<String> = methodRules.flatMap { it.methods }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        methodRules.forEach { rule ->
            if (rule.methods.contains(node.methodName)) {
                rule(context, node, method)
            }
        }
    }

    companion object {
        val methodRules: List<CommonUiMethodRule> = listOf(
            CommonUiProcessPhoenixRule,
        )
    }
}
