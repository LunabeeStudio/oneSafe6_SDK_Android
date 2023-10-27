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
 * Created by Lunabee Studio / Date - 7/4/2023 - for the oneSafe6 SDK.
 * Last modified 04/07/2023 09:28
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.model.DoubleRatchetError
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.SendMessageData
import studio.lunabee.onesafe.messaging.domain.extension.asOSError
import java.util.UUID
import javax.inject.Inject

class GetSendMessageDataUseCase @Inject constructor(
    private val doubleRatchetEngine: DoubleRatchetEngine,
) {
    suspend operator fun invoke(contactId: UUID): LBResult<SendMessageData> {
        return try {
            LBResult.Success(doubleRatchetEngine.getSendData(DoubleRatchetUUID(contactId)))
        } catch (e: DoubleRatchetError) {
            LBResult.Failure(e.asOSError())
        }
    }
}
