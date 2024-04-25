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
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest
import com.google.api.services.drive.Drive
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.error.OSError.Companion.get
import java.io.IOException
import java.io.InputStream
import java.net.UnknownHostException

internal fun <T> AbstractGoogleClientRequest<T>.executeAsFlow() = flow<LBFlowResult<T>> {
    emit(LBFlowResult.Success(execute()))
}.setupFlow()

internal fun <T : Drive.Files.Get> T.executeMediaAsInputStreamAsFlow() = flow<LBFlowResult<InputStream>> {
    emit(LBFlowResult.Success(executeMediaAsInputStream()))
}.setupFlow()

private fun <T> Flow<LBFlowResult<T>>.setupFlow(): Flow<LBFlowResult<T>> = onStart {
    emit(LBFlowResult.Loading())
}.catch { error ->
    val osError = when (error) {
        is UserRecoverableAuthIOException -> OSDriveError(
            code = OSDriveError.Code.DRIVE_AUTHENTICATION_REQUIRED,
            cause = error,
        )
        is GoogleJsonResponseException -> error.toOSDriveError()
        is UnknownHostException -> OSDriveError.Code.DRIVE_NETWORK_FAILURE.get(cause = error)
        is IOException -> OSDriveError.Code.DRIVE_REQUEST_EXECUTION_FAILED.get(cause = error)
        else -> OSDriveError.Code.DRIVE_UNKNOWN_ERROR.get(cause = error, message = error.localizedMessage)
    }

    emit(LBFlowResult.Failure(osError))
}

private fun GoogleJsonResponseException.toOSDriveError(): OSDriveError {
    LBLogger.get("DriveError").e(details.toPrettyString())
    val message = "${details.code} - ${details.message}"
    return when {
        details.code == 404 &&
            details.errors.firstOrNull()?.location == "fileId" &&
            details.errors.firstOrNull()?.reason == "notFound" -> OSDriveError.Code.DRIVE_BACKUP_REMOTE_ID_NOT_FOUND.get(cause = this)
        else -> OSDriveError.Code.DRIVE_REQUEST_EXECUTION_FAILED.get(cause = this, message = message)
    }
}
