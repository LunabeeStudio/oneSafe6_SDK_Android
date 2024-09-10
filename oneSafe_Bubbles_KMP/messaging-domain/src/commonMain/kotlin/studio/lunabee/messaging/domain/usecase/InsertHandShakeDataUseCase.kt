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
 * Created by Lunabee Studio / Date - 6/3/2024 - for the oneSafe6 SDK.
 * Last modified 6/3/24, 9:01 AM
 */

package studio.lunabee.messaging.domain.usecase

import studio.lunabee.bubbles.domain.di.Inject
import studio.lunabee.messaging.domain.model.HandShakeData
import studio.lunabee.messaging.domain.repository.HandShakeDataRepository

class InsertHandShakeDataUseCase @Inject constructor(
    private val handShakeDataRepository: HandShakeDataRepository,
    private val cryptoHandShakeDataUseCase: CryptoHandShakeDataUseCase,
) {
    suspend operator fun invoke(handShakeData: HandShakeData) {
        val encHandShakeData = cryptoHandShakeDataUseCase.encrypt(handShakeData)
        handShakeDataRepository.insert(encHandShakeData)
    }
}
