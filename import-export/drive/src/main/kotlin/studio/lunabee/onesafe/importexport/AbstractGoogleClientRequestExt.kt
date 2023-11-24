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
 * Created by Lunabee Studio / Date - 10/10/2023 - for the oneSafe6 SDK.
 * Last modified 10/10/23, 10:02 AM
 */

package studio.lunabee.onesafe.importexport

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest
import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.error.OSError.Companion.get
import java.io.IOException

internal fun <T> AbstractGoogleClientRequest<T>.executeAsFlow() = flow<LBFlowResult<T>> {
    emit(LBFlowResult.Success(execute()))
}.onStart {
    emit(LBFlowResult.Loading())
}.catch { error ->
    val osError = when (error) {
        is UserRecoverableAuthIOException -> OSDriveError(code = OSDriveError.Code.AUTHENTICATION_REQUIRED, cause = error)
        is IOException -> OSDriveError.Code.NETWORK_FAILURE.get(cause = error)
        else -> OSDriveError(code = OSDriveError.Code.REQUEST_EXECUTION_FAILED, cause = error)
    }

    emit(LBFlowResult.Failure(osError))
}
