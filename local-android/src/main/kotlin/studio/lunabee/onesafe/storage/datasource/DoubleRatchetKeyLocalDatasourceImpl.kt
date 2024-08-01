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
 * Created by Lunabee Studio / Date - 7/29/2024 - for the oneSafe6 SDK.
 * Last modified 29/07/2024 14:14
 */

package studio.lunabee.onesafe.storage.datasource

import studio.lunabee.messaging.domain.model.EncDoubleRatchetKey
import studio.lunabee.messaging.repository.datasource.DoubleRatchetKeyLocalDatasource
import studio.lunabee.onesafe.storage.dao.DoubleRatchetKeyDao
import studio.lunabee.onesafe.storage.model.RoomDoubleRatchetKey
import javax.inject.Inject

class DoubleRatchetKeyLocalDatasourceImpl @Inject constructor(
    private val doubleRatchetKeyDao: DoubleRatchetKeyDao,
) : DoubleRatchetKeyLocalDatasource {
    override suspend fun insert(key: EncDoubleRatchetKey) {
        doubleRatchetKeyDao.insert(RoomDoubleRatchetKey.fromEncDoubleRatchetKey(key))
    }

    override suspend fun getById(id: String): ByteArray? {
        return doubleRatchetKeyDao.getById(id)
    }

    override suspend fun deleteById(id: String) {
        doubleRatchetKeyDao.deleteById(id)
    }
}
