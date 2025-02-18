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
 * Created by Lunabee Studio / Date - 9/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/09/2024 09:22
 */

package studio.lunabee.onesafe.feature.settings.autodestruction.password

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

sealed interface AutoDestructionPasswordCreationUiState {
    data class Idle(
        val passwordStrength: LbcTextSpec?,
    ) : AutoDestructionPasswordCreationUiState

    data object Error : AutoDestructionPasswordCreationUiState {
        val text: LbcTextSpec = LbcTextSpec.StringResource(OSString.changePassword_error_passwordAlreadyUsed)
    }

    class Success(
        val passwordHash: String,
        val salt: String,
    ) : AutoDestructionPasswordCreationUiState
}
