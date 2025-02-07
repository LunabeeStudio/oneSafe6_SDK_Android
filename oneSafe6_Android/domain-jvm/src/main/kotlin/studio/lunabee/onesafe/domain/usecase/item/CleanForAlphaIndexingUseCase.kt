/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 12/11/2023 - for the oneSafe6 SDK.
 * Last modified 12/11/23, 11:31 AM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbextensions.remove
import java.text.Normalizer
import javax.inject.Inject

/**
 * Clean the input string (usually an item name) to allow alphabetic indexing computation
 *
 * @see <a href="https://shorturl.at/dvNR6">spec</a>
 * @see <a href="https://stackoverflow.com/a/49516025/10935947">regex source</a>
 */
class CleanForAlphaIndexingUseCase @Inject constructor() {

    operator fun invoke(name: String): String {
        return name
            .remove("[^\\p{L}\\p{N}\\p{P}\\p{Z}]".toRegex()) // keep letters + numerics + punctuations + spaces
            .run {
                // Replace diacritics
                Normalizer
                    .normalize(this, Normalizer.Form.NFD)
                    .remove("\\p{Mn}+".toRegex())
            }
            .trim()
            .lowercase()
    }
}
