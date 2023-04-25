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

package studio.lunabee.onesafe.domain.usecase

import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeData
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.repository.ForceUpgradeRepository
import java.util.Locale
import javax.inject.Inject

class FetchForceUpgradeDataUseCase @Inject constructor(
    @BuildNumber private val buildNumber: Int,
    private val forceUpgradeRepository: ForceUpgradeRepository,
) {

    suspend operator fun invoke() {
        forceUpgradeRepository.fetchForceUpgradeInfo()?.let { _info ->
            if (_info.forceUpdateBuildNumber > buildNumber || _info.softUpdateBuildNumber > buildNumber) {
                val languageFileUrl = Locale.getDefault().language.let { _language ->
                    _info.languageFiles[_language] ?: _info.fallbackLanguageFile
                }
                forceUpgradeRepository.fetchForceUpgradeStrings(languageFileUrl)?.let { _strings ->
                    val data = ForceUpgradeData(
                        forceBuildNumber = _info.forceUpdateBuildNumber,
                        softBuildNumber = _info.softUpdateBuildNumber,
                        strings = _strings,
                    )
                    forceUpgradeRepository.saveForceUpgradeData(data)
                }
            } else {
                forceUpgradeRepository.cleanForceUpgradeData()
            }
        }
    }
}
