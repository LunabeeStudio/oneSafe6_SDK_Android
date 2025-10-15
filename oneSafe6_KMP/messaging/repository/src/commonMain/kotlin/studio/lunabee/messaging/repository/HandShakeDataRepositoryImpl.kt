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
 * Last modified 06/07/2023 10:47
 */

package studio.lunabee.messaging.repository

import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.EncHandShakeData
import studio.lunabee.messaging.domain.repository.HandShakeDataRepository
import studio.lunabee.messaging.repository.datasource.HandShakeDataLocalDatasource
import studio.lunabee.onesafe.di.Inject

class HandShakeDataRepositoryImpl @Inject constructor(
    private val handShakeDataLocalDatasource: HandShakeDataLocalDatasource,
) : HandShakeDataRepository {
    override suspend fun insert(handShakeData: EncHandShakeData) {
        handShakeDataLocalDatasource.insert(handShakeData)
    }

    override suspend fun delete(conversationLocalId: DoubleRatchetUUID) {
        handShakeDataLocalDatasource.delete(conversationLocalId)
    }

    override suspend fun getById(conversationLocalId: DoubleRatchetUUID): EncHandShakeData? = handShakeDataLocalDatasource
        .getById(
            conversationLocalId,
        )
}
