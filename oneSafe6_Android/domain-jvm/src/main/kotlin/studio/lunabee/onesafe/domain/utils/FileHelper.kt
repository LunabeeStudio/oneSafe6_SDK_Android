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
 * Created by Lunabee Studio / Date - 10/19/2023 - for the oneSafe6 SDK.
 * Last modified 10/19/23, 6:12 PM
 */

package studio.lunabee.onesafe.domain.utils

import studio.lunabee.onesafe.domain.Constant

object FileHelper {
    fun String.getValidFileName(
        extension: String,
    ): String {
        return if (this.endsWith(".$extension")) {
            this
        } else {
            "$this.$extension"
        }.replace(Constant.ForbiddenCharacterFileName, "_")
    }

    fun String.clearExtension(): String = this.substringBeforeLast('.').ifBlank { this }

    fun String.extension(): String? = substringAfterLast('.')
        .takeIf { substringResult -> substringResult != this } // i.e file does not have an '.' in name
}
