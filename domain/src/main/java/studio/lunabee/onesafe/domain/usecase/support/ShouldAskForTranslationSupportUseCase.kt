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
 * Last modified 6/6/23, 3:09 PM
 */

package studio.lunabee.onesafe.domain.usecase.support

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import javax.inject.Inject

class ShouldAskForTranslationSupportUseCase @Inject constructor(
    private val supportOSRepository: SupportOSRepository,
    private val isLanguageGeneratedUseCase: IsLanguageGeneratedUseCase,
) {

    operator fun invoke(currentLocale: String): Flow<Boolean> = combine(
        supportOSRepository.languageConfigCount,
        supportOSRepository.lastLanguageConfig,
    ) { languageConfigCount, lastLanguageConfig ->
        when {
            !isLanguageGeneratedUseCase(currentLocale) -> false
            lastLanguageConfig != currentLocale -> {
                supportOSRepository.resetLanguageConfigWithNewLocale(newLocale = currentLocale)
                false
            }
            else -> languageConfigCount >= CountToAskForSupport
        }
    }

    companion object {
        const val CountToAskForSupport: Int = 5
    }
}
