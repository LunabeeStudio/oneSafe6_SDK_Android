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

package studio.lunabee.onesafe.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeData
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeInfo
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeStrings
import studio.lunabee.onesafe.domain.repository.ForceUpgradeRepository
import studio.lunabee.onesafe.repository.datasource.ForceUpdateRemoteDatasource
import studio.lunabee.onesafe.repository.datasource.ForceUpgradeLocalDatasource
import javax.inject.Inject

class ForceUpgradeRepositoryImpl @Inject constructor(
    private val forceUpgradeLocalDatasource: ForceUpgradeLocalDatasource,
    private val forceUpgradeRemoteDatasource: ForceUpdateRemoteDatasource,
) : ForceUpgradeRepository {

    override suspend fun fetchForceUpgradeStrings(languageFileUrl: String): ForceUpgradeStrings? = forceUpgradeRemoteDatasource
        .fetchForceUpgradeStrings(
            languageFileUrl,
        )

    override suspend fun saveForceUpgradeData(forceUpgradeData: ForceUpgradeData) {
        forceUpgradeLocalDatasource.saveForceUpgradeData(forceUpgradeData)
    }

    override suspend fun cleanForceUpgradeData(): Unit = forceUpgradeLocalDatasource.cleanForceUpgradeData()

    override suspend fun fetchForceUpgradeInfo(): ForceUpgradeInfo? = forceUpgradeRemoteDatasource
        .fetchForceUpgradeInfo()

    override fun getForceUpgradeData(): Flow<ForceUpgradeData?> = forceUpgradeLocalDatasource.getForceUpgradeData()
}
