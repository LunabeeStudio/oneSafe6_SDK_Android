/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/15/2024 - for the oneSafe6 SDK.
 * Last modified 7/15/24, 10:18 AM
 */

package studio.lunabee.onesafe.repository.datasource

import kotlinx.coroutines.flow.Flow

interface GlobalSettingsLocalDataSource {
    fun hasVisitedLogin(): Flow<Boolean>
    fun hasDoneTutorialOpenOsk(): Flow<Boolean>
    fun hasDoneTutorialLockOsk(): Flow<Boolean>

    suspend fun setHasVisitedLogin(value: Boolean)
    suspend fun setHasDoneTutorialOpenOsk(value: Boolean)
    suspend fun setHasDoneTutorialLockOsk(value: Boolean)
}
