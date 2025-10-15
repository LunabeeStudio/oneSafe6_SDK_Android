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
 * Created by Lunabee Studio / Date - 7/10/2024 - for the oneSafe6 SDK.
 * Last modified 7/10/24, 9:50 PM
 */

package studio.lunabee.onesafe.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safe.SafeId

interface AppVisitRepository {
    fun hasVisitedLogin(): Flow<Boolean>

    fun hasDoneTutorialOpenOsk(): Flow<Boolean>

    fun hasDoneTutorialLockOsk(): Flow<Boolean>

    fun hasFinishOneSafeKOnBoardingFlow(safeId: SafeId): Flow<Boolean>

    fun hasDoneOnBoardingBubblesFlow(safeId: SafeId): Flow<Boolean>

    fun hasHiddenCameraTipsFlow(safeId: SafeId): Flow<Boolean>

    fun hasSeenItemEditionUrlToolTipFlow(safeId: SafeId): Flow<Boolean>

    fun hasSeenItemEditionEmojiToolTipFlow(safeId: SafeId): Flow<Boolean>

    fun hasSeenItemReadEditToolTipFlow(safeId: SafeId): Flow<Boolean>

    suspend fun hasSeenDialogMessageSaveConfirmation(safeId: SafeId): Boolean

    suspend fun hasSeenItemEditionUrlToolTip(safeId: SafeId): Boolean

    suspend fun hasSeenItemEditionEmojiToolTip(safeId: SafeId): Boolean

    suspend fun setHasSeenDialogMessageSaveConfirmation(safeId: SafeId)

    suspend fun setHasVisitedLogin(value: Boolean)

    suspend fun setHasDoneTutorialOpenOsk(value: Boolean)

    suspend fun setHasDoneTutorialLockOsk(value: Boolean)

    suspend fun setHasFinishOneSafeKOnBoarding(safeId: SafeId, value: Boolean)

    suspend fun setHasDoneOnBoardingBubbles(safeId: SafeId, value: Boolean)

    suspend fun setHasHiddenCameraTips(safeId: SafeId, value: Boolean)

    suspend fun setHasSeenItemEditionUrlToolTip(safeId: SafeId, value: Boolean)

    suspend fun setHasSeenItemEditionEmojiToolTip(safeId: SafeId, value: Boolean)

    suspend fun setHasSeenItemReadEditToolTip(safeId: SafeId, value: Boolean)
}
