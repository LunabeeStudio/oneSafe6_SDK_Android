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
 * Created by Lunabee Studio / Date - 7/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/07/2024 09:43
 */

package studio.lunabee.onesafe.storage.datasource

import kotlinx.coroutines.flow.first
import studio.lunabee.bubbles.domain.di.Inject
import studio.lunabee.messaging.repository.datasource.MessageQueueLocalDatasource
import studio.lunabee.onesafe.cryptography.DatastoreEngine
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider

class MessageQueueLocalDatasourceImpl @Inject constructor(
    @DatastoreEngineProvider(DataStoreType.Encrypted) private val dataStoreEngine: DatastoreEngine,
) : MessageQueueLocalDatasource {
    override suspend fun retrieveValue(key: String): ByteArray? {
        return dataStoreEngine.retrieveValue(key).first()
    }

    override suspend fun insertValue(key: String, value: ByteArray) {
        dataStoreEngine.insertValue(key, value)
    }
}
