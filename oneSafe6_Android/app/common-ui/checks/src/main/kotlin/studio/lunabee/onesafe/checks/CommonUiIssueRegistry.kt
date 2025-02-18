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
 * Last modified 5/4/23, 4:52 PM
 */

package studio.lunabee.onesafe.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class CommonUiIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = CommonUiCodeDetector.methodRules.map { rule -> rule.issue }

    override val api: Int
        get() = CURRENT_API

    override val minApi: Int
        get() = 8

    override val vendor: Vendor = Vendor(
        vendorName = "Lunabee Studio",
        feedbackUrl = "https://www.notion.so/lunabeestudio/528259251d234853bdef15fc4bd792a6?v=383c1ed8b6b04a9f81f77bd488eff729",
        contact = "https://www.notion.so/lunabeestudio/528259251d234853bdef15fc4bd792a6?v=383c1ed8b6b04a9f81f77bd488eff729",
    )
}
