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
 * Created by Lunabee Studio / Date - 11/7/2023 - for the oneSafe6 SDK.
 * Last modified 11/7/23, 1:50 PM
 */

package studio.lunabee.onesafe.importexport

import android.content.Intent
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.error.OSError.Companion.get

private val logger = LBLogger.get<GoogleDriveHelper>()

object GoogleDriveHelper {
    fun getAuthorizationIntent(error: OSDriveError): LBResult<Intent> {
        val cause: UserRecoverableAuthIOException? = error.cause as? UserRecoverableAuthIOException
        if (cause == null) {
            logger.e(error, "Error cause's is not UserRecoverableAuthIOException")
        }
        return cause?.let {
            LBResult.Success(it.intent)
        } ?: LBResult.Failure(OSDriveError.Code.UNEXPECTED_NULL_AUTH_INTENT.get())
    }
}
