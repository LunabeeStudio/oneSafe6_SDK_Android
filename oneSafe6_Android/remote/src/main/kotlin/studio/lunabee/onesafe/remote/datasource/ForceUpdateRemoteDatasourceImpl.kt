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

package studio.lunabee.onesafe.remote.datasource

import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeInfo
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeStrings
import studio.lunabee.onesafe.remote.api.ForceUpgradeApi
import studio.lunabee.onesafe.repository.datasource.ForceUpdateRemoteDatasource
import javax.inject.Inject

class ForceUpdateRemoteDatasourceImpl @Inject constructor(
    private val forceUpgradeApi: ForceUpgradeApi,
) : ForceUpdateRemoteDatasource {
    override suspend fun fetchForceUpgradeInfo(): ForceUpgradeInfo? = forceUpgradeApi
        .getForceUpgradeInfo()
        ?.toForceUpgradeInfo()

    override suspend fun fetchForceUpgradeStrings(languageFileUrl: String): ForceUpgradeStrings? = forceUpgradeApi
        .getForceUpgradeStrings(
            languageFileUrl,
        )?.toForceUpgradeStrings()
}
