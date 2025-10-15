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

package studio.lunabee.onesafe.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import studio.lunabee.onesafe.domain.qualifier.ForceUpgradeUrl
import studio.lunabee.onesafe.remote.model.ApiForceUpgradeInfo
import studio.lunabee.onesafe.remote.model.ApiForceUpgradeStrings
import javax.inject.Inject

class ForceUpgradeApi @Inject constructor(
    @ForceUpgradeUrl
    private val forceUpgradeUrl: String,
    private val httpClient: HttpClient,
) {

    suspend fun getForceUpgradeInfo(): ApiForceUpgradeInfo? = try {
        httpClient.get(urlString = "$forceUpgradeUrl/android-info-force-upgrade.json").body()
    } catch (e: Exception) {
        null
    }

    suspend fun getForceUpgradeStrings(languageFileUrl: String): ApiForceUpgradeStrings? = try {
        httpClient.get(urlString = languageFileUrl).body()
    } catch (e: Exception) {
        null
    }
}
