/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 5/26/2023 - for the oneSafe6 SDK.
 * Last modified 5/26/23, 9:28 AM
 */

package studio.lunabee.onesafe.domain.common

import kotlinx.coroutines.flow.Flow

interface FeatureFlags {
    fun florisBoard(): Boolean
    fun accessibilityService(): Boolean
    fun oneSafeK(): Boolean
    fun bubbles(): Flow<Boolean>
    fun quickSignIn(): Boolean
    fun cloudBackup(): Boolean
    fun backupWorkerExpedited(): Boolean
}
