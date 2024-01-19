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
 * Created by Lunabee Studio / Date - 6/6/2023 - for the oneSafe6 SDK.
 * Last modified 6/6/23, 3:09 PM
 */

package studio.lunabee.onesafe.domain.usecase.support

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Check whereas the app should show the "ask for support" CTA
 */
class ShouldAskForSupportUseCase @Inject constructor(
    private val supportOSRepository: SupportOSRepository,
) {

    suspend operator fun invoke(): Boolean = combine(
        supportOSRepository.appVisitsCount,
        supportOSRepository.dismissInstant,
        supportOSRepository.ratingInstant,
    ) { visitCount, dismissTimeStamp, ratingTimeStamp ->

        val hasNeverInteractedWithSupport = dismissTimeStamp == null && ratingTimeStamp == null
        when {
            visitCount >= CountToAskForSupport && hasNeverInteractedWithSupport -> true
            ratingTimeStamp != null -> {
                var askTime = LocalDateTime.ofInstant(ratingTimeStamp, ZoneId.systemDefault())
                askTime = askTime.plusMonths(BetweenRatingMonthDelay)
                askTime.isBefore(LocalDateTime.now()) && visitCount >= CountToAskForSupport
            }
            dismissTimeStamp != null -> {
                var askTime = LocalDateTime.ofInstant(dismissTimeStamp, ZoneId.systemDefault())
                askTime = askTime.plusMonths(BetweenDismissMonthDelay)
                askTime.isBefore(LocalDateTime.now()) && visitCount >= CountToAskForSupport
            }
            else -> false
        }
    }.first()

    companion object {
        const val CountToAskForSupport: Int = 10
        const val BetweenRatingMonthDelay: Long = 3
        const val BetweenDismissMonthDelay: Long = 2
    }
}
