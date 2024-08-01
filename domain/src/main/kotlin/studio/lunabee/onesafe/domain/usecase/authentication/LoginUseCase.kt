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
 * Created by Lunabee Studio / Date - 7/8/2024 - for the oneSafe6 SDK.
 * Last modified 7/8/24, 4:44 PM
 */

package studio.lunabee.onesafe.domain.usecase.authentication

import com.lunabee.lbcore.model.LBResult
import javax.crypto.Cipher

interface LoginUseCase {
    suspend operator fun invoke(password: CharArray): LBResult<Unit>

    suspend operator fun invoke(cipher: Cipher): LBResult<Unit>
}
