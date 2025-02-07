/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Last modified 3/6/24, 11:10 PM
 */

package studio.lunabee.onesafe.checks

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class OSCryptoIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = listOf(
        EnforceCryptoDispatcherDetector.SuspendCryptoDispatcherIssue,
        EnforceCryptoDispatcherDetector.FlowCryptoDispatcherIssue,
    )

    override val api: Int
        get() = CURRENT_API

    override val minApi: Int
        get() = 8

    override val vendor: Vendor = Vendor(
        vendorName = "Lunabee Studio",
        feedbackUrl = "https://github.com/LunabeeStudio/oneSafe6_Android/pull/945",
        contact = "https://lunabee-studio.slack.com/archives/C04BFHZBHJR",
    )
}
