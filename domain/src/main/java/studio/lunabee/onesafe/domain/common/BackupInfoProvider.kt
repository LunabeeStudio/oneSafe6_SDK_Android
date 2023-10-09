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
 * Created by Lunabee Studio / Date - 9/28/2023 - for the oneSafe6 SDK.
 * Last modified 9/28/23, 3:30 PM
 */

package studio.lunabee.onesafe.domain.common

import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.qualifier.VersionName
import javax.inject.Inject

class BackupInfoProvider @Inject constructor(
    @VersionName private val versionName: String,
    @BuildNumber private val buildNumber: Int,
) {
    operator fun invoke(): String {
        return "$Platform-$versionName-$buildNumber"
    }

    companion object {
        private const val Platform: String = "android"
    }
}
