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
 * Created by Lunabee Studio / Date - 6/6/2023 - for the oneSafe6 SDK.
 * Last modified 6/6/23, 3:29 PM
 */

package studio.lunabee.onesafe.repository.datasource

import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface SupportOSDataSource {
    val appVisitsCount: Flow<Int>
    val dismissInstant: Flow<Instant?>
    val ratingInstant: Flow<Instant?>
    val lastLanguageConfig: Flow<String?>
    val languageConfigCount: Flow<Int>
    val visibleSince: Flow<Instant?>
    suspend fun incrementAppVisit()
    suspend fun resetAppVisit()
    suspend fun setAppVisit(count: Int)
    suspend fun setRatingInstant(instant: Instant?)
    suspend fun setRattingInstant(instant: Instant?)
    suspend fun markLanguageConfigAsHandled()
    suspend fun resetLanguageConfigWithNewLocale(newLocale: String)
    suspend fun setVisibleSince(visibleSince: Instant?)
}
