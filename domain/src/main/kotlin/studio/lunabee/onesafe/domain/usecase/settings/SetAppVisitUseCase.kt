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
 * Last modified 7/10/24, 10:04 PM
 */

package studio.lunabee.onesafe.domain.usecase.settings

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.repository.AppVisitRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

private val logger = LBLogger.get<SetAppVisitUseCase>()

class SetAppVisitUseCase @Inject constructor(
    private val appVisitRepository: AppVisitRepository,
    private val safeRepository: SafeRepository,
) {
    suspend fun setHasVisitedLoginKey(): Unit = appVisitRepository.setHasVisitedLogin(true)

    suspend fun setHasDoneTutorialOpenOsk(): Unit = appVisitRepository.setHasDoneTutorialOpenOsk(true)

    suspend fun setHasDoneTutorialLockOsk(): Unit = appVisitRepository.setHasDoneTutorialLockOsk(true)

    suspend fun setHasFinishOneSafeKOnBoarding(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        appVisitRepository.setHasFinishOneSafeKOnBoarding(safeId, true)
    }

    suspend fun setHasDoneOnboardingBubbles(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        appVisitRepository.setHasDoneOnBoardingBubbles(safeId, true)
    }

    suspend fun setHasHiddenCameraTips(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        appVisitRepository.setHasHiddenCameraTips(safeId, true)
    }

    suspend fun setHasSeenItemEditionUrlToolTip(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        appVisitRepository.setHasSeenItemEditionUrlToolTip(safeId, true)
    }

    suspend fun setHasSeenItemEditionEmojiToolTip(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        appVisitRepository.setHasSeenItemEditionEmojiToolTip(safeId, true)
    }

    suspend fun setHasSeenItemReadEditToolTip(): LBResult<Unit> = OSError.runCatching(logger) {
        val safeId = safeRepository.currentSafeId()
        appVisitRepository.setHasSeenItemReadEditToolTip(safeId, true)
    }
}
