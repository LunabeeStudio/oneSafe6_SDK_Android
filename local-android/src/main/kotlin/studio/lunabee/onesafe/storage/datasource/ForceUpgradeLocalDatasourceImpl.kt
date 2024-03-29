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
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeData
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeStrings
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeTypeStrings
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.repository.datasource.ForceUpgradeLocalDatasource
import studio.lunabee.onesafe.storage.OSForceUpgradeProto.ForceUpgradeProtoData
import studio.lunabee.onesafe.storage.copy
import javax.inject.Inject

class ForceUpgradeLocalDatasourceImpl @Inject constructor(
    @BuildNumber private val buildNumber: Int,
    private val dataStore: DataStore<ForceUpgradeProtoData>,
    @FileDispatcher private val fileDispatcher: CoroutineDispatcher,
) : ForceUpgradeLocalDatasource {

    override fun getForceUpgradeData(): Flow<ForceUpgradeData?> = dataStore.data.map { data ->
        if (data.buildNumber == buildNumber) {
            ForceUpgradeData(
                strings = ForceUpgradeStrings(
                    forceUpgrade = ForceUpgradeTypeStrings(
                        title = data.forceTitle,
                        description = data.forceDescription,
                        buttonLabel = data.softButtonLabel,
                    ),
                    softUpgrade = ForceUpgradeTypeStrings(
                        title = data.softTitle,
                        description = data.softDescription,
                        buttonLabel = data.softButtonLabel,
                    ),
                ),
                softBuildNumber = data.softBuildNumber,
                forceBuildNumber = data.forceBuildNumber,
            )
        } else {
            null
        }
    }.flowOn(fileDispatcher)

    override suspend fun cleanForceUpgradeData() {
        withContext(fileDispatcher) {
            dataStore.updateData { _data ->
                _data.defaultInstanceForType
            }
        }
    }

    override suspend fun saveForceUpgradeData(data: ForceUpgradeData) {
        withContext(fileDispatcher) {
            dataStore.updateData { _data ->
                _data.copy {
                    buildNumber = this@ForceUpgradeLocalDatasourceImpl.buildNumber
                    forceBuildNumber = data.forceBuildNumber
                    softBuildNumber = data.softBuildNumber
                    forceTitle = data.strings.forceUpgrade.title
                    softTitle = data.strings.softUpgrade.title
                    softDescription = data.strings.softUpgrade.description
                    forceDescription = data.strings.forceUpgrade.description
                    forceButtonLabel = data.strings.forceUpgrade.buttonLabel
                    softButtonLabel = data.strings.softUpgrade.buttonLabel
                }
            }
        }
    }
}
