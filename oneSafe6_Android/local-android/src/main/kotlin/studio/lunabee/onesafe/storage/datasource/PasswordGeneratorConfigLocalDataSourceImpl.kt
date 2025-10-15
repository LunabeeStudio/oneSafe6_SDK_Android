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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.storage.datasource

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.model.password.PasswordConfig
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.repository.datasource.PasswordGeneratorConfigLocalDataSource
import studio.lunabee.onesafe.storage.OSPasswordGeneratorConfigProto
import javax.inject.Inject

class PasswordGeneratorConfigLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<OSPasswordGeneratorConfigProto.PasswordGeneratorConfigProto>,
    @param:FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) : PasswordGeneratorConfigLocalDataSource {
    override fun getConfig(): Flow<PasswordConfig> = dataStore.data
        .map { proto ->
            if (proto.length != 0) {
                PasswordConfig(
                    length = proto.length,
                    includeUpperCase = proto.includeUpperCase,
                    includeLowerCase = proto.includeLowerCase,
                    includeNumber = proto.includeNumber,
                    includeSymbol = proto.includeSymbol,
                )
            } else {
                PasswordConfig.default()
            }
        }.flowOn(fileDispatcher)

    override suspend fun setConfig(config: PasswordConfig) {
        withContext(fileDispatcher) {
            dataStore.updateData {
                OSPasswordGeneratorConfigProto.PasswordGeneratorConfigProto
                    .newBuilder()
                    .setLength(config.length)
                    .setIncludeUpperCase(config.includeUpperCase)
                    .setIncludeLowerCase(config.includeLowerCase)
                    .setIncludeNumber(config.includeNumber)
                    .setIncludeSymbol(config.includeSymbol)
                    .build()
            }
        }
    }
}
