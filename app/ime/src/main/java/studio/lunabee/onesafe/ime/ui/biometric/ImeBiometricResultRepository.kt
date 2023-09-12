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
 * Created by Lunabee Studio / Date - 9/5/2023 - for the oneSafe6 SDK.
 * Last modified 05/09/2023 13:45
 */

package studio.lunabee.onesafe.ime.ui.biometric

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import studio.lunabee.onesafe.randomize
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImeBiometricResultRepository @Inject constructor() {

    private val _result: MutableSharedFlow<LBResult<ByteArray>> = MutableSharedFlow()
    val result: SharedFlow<LBResult<ByteArray>> = _result.asSharedFlow()

    suspend fun setResult(result: LBResult<ByteArray>) {
        _result.emit(result)
        if (result is LBResult.Success) {
            // Suspend until subscriber used the result then randomize
            _result.emit(LBResult.Success(byteArrayOf()))
            result.successData.randomize()
        }
    }
}
