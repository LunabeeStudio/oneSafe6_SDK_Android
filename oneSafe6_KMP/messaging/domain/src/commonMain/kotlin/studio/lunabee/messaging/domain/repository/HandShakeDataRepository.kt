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
 * Created by Lunabee Studio / Date - 7/6/2023 - for the oneSafe6 SDK.
 * Last modified 06/07/2023 10:48
 */

package studio.lunabee.messaging.domain.repository

import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.EncHandShakeData

interface HandShakeDataRepository {
    suspend fun insert(handShakeData: EncHandShakeData)
    suspend fun delete(conversationLocalId: DoubleRatchetUUID)
    suspend fun getById(conversationLocalId: DoubleRatchetUUID): EncHandShakeData?
}
