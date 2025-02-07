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
 * Last modified 7/10/24, 9:56 PM
 */

package studio.lunabee.onesafe.domain.usecase.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.repository.AppVisitRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import javax.inject.Inject

class GetAppVisitUseCase @Inject constructor(
    private val safeRepository: SafeRepository,
    private val appVisitRepository: AppVisitRepository,
) {
    fun hasVisitedLogin(): Flow<Boolean> = appVisitRepository.hasVisitedLogin()

    fun hasDoneTutorialOpenOsk(): Flow<Boolean> = appVisitRepository.hasDoneTutorialOpenOsk()

    fun hasDoneTutorialLockOsk(): Flow<Boolean> = appVisitRepository.hasDoneTutorialLockOsk()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun hasFinishOneSafeKOnBoarding(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            appVisitRepository.hasFinishOneSafeKOnBoardingFlow(safeId)
        } ?: flowOf(false)
    }

    suspend fun hasSeenDialogMessageSaveConfirmation(): Boolean {
        return safeRepository.currentSafeIdOrNull()?.let { safeId ->
            appVisitRepository.hasSeenDialogMessageSaveConfirmation(safeId)
        } ?: true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun hasDoneOnBoardingBubbles(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            appVisitRepository.hasDoneOnBoardingBubblesFlow(safeId)
        } ?: flowOf(false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun hasHiddenCameraTips(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            appVisitRepository.hasHiddenCameraTipsFlow(safeId)
        } ?: flowOf(false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun hasSeenItemEditionUrlToolTip(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            appVisitRepository.hasSeenItemEditionUrlToolTipFlow(safeId)
        } ?: flowOf(false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun hasSeenItemEditionEmojiToolTip(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            appVisitRepository.hasSeenItemEditionEmojiToolTipFlow(safeId)
        } ?: flowOf(false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun hasSeenItemReadEditToolTip(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            appVisitRepository.hasSeenItemReadEditToolTipFlow(safeId)
        } ?: flowOf(false)
    }
}
