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
 * Last modified 6/6/23, 3:11 PM
 */

package studio.lunabee.onesafe.domain.repository

import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface SupportOSRepository {
    val appVisitsCount: Flow<Int>
    val dismissInstant: Flow<Instant?>
    val ratingInstant: Flow<Instant?>
    val lastLanguageConfig: Flow<String?>
    val languageConfigCount: Flow<Int>
    suspend fun incrementAppVisit()
    suspend fun setAppVisit(count: Int)
    suspend fun resetAppVisit()
    suspend fun markLanguageConfigAsHandled()
    suspend fun resetLanguageConfigWithNewLocale(newLocale: String)
    suspend fun setRatingInstant(instant: Instant?)
    suspend fun setDismissInstant(instant: Instant?)
}
