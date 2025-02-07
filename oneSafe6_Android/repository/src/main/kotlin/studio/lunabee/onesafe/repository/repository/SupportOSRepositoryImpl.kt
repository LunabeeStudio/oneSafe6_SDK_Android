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
 * Last modified 6/6/23, 3:33 PM
 */

package studio.lunabee.onesafe.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import studio.lunabee.onesafe.repository.datasource.SupportOSDataSource
import java.time.Instant
import javax.inject.Inject

class SupportOSRepositoryImpl @Inject constructor(
    private val supportOSDataSource: SupportOSDataSource,
) : SupportOSRepository {

    override val appVisitsCount: Flow<Int> = supportOSDataSource.appVisitsCount
    override val dismissInstant: Flow<Instant?> = supportOSDataSource.dismissInstant
    override val ratingInstant: Flow<Instant?> = supportOSDataSource.ratingInstant
    override val lastLanguageConfig: Flow<String?> = supportOSDataSource.lastLanguageConfig
    override val languageConfigCount: Flow<Int> = supportOSDataSource.languageConfigCount
    override val visibleSince: Flow<Instant?> = supportOSDataSource.visibleSince

    override suspend fun incrementAppVisit() {
        supportOSDataSource.incrementAppVisit()
    }

    override suspend fun setAppVisit(count: Int) {
        supportOSDataSource.setAppVisit(count)
    }

    override suspend fun resetAppVisit() {
        supportOSDataSource.resetAppVisit()
    }

    override suspend fun setRatingInstant(instant: Instant?) {
        supportOSDataSource.setRatingInstant(instant)
    }

    override suspend fun setDismissInstant(instant: Instant?) {
        supportOSDataSource.setRattingInstant(instant)
    }

    override suspend fun setVisibleSince(visibleSince: Instant?) {
        supportOSDataSource.setVisibleSince(visibleSince)
    }

    override suspend fun markLanguageConfigAsHandled() {
        supportOSDataSource.markLanguageConfigAsHandled()
    }

    override suspend fun resetLanguageConfigWithNewLocale(newLocale: String) {
        supportOSDataSource.resetLanguageConfigWithNewLocale(newLocale = newLocale)
    }
}
