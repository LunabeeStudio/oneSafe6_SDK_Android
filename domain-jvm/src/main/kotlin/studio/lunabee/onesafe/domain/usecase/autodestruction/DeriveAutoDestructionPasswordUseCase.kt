/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/20/2024 - for the oneSafe6 SDK.
 * Last modified 20/09/2024 10:32
 */

package studio.lunabee.onesafe.domain.usecase.autodestruction

import studio.lunabee.onesafe.di.Inject
import java.security.MessageDigest

class DeriveAutoDestructionPasswordUseCase @Inject constructor() {
    @OptIn(ExperimentalStdlibApi::class)
    operator fun invoke(password: String, safeSalt: String): String {
        val concatString = safeSalt + password
        return MessageDigest.getInstance(Algorithm).digest(concatString.encodeToByteArray()).toHexString()
    }

    private companion object {
        const val Algorithm = "SHA-256"
    }
}
