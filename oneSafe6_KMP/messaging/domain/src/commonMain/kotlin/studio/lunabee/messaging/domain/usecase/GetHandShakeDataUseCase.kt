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

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.messaging.domain.model.HandShakeData
import studio.lunabee.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID

class GetHandShakeDataUseCase @Inject constructor(
    private val handShakeDataRepository: HandShakeDataRepository,
    private val cryptoHandShakeDataUseCase: CryptoHandShakeDataUseCase,
) {
    suspend operator fun invoke(conversationLocalId: DoubleRatchetUUID): LBResult<HandShakeData?> = OSError.runCatching {
        handShakeDataRepository.getById(conversationLocalId)?.let { encHandShakeData ->
            cryptoHandShakeDataUseCase.decrypt(encHandShakeData)
        }
    }
}
