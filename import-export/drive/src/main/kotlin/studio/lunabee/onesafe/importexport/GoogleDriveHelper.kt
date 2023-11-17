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
import studio.lunabee.onesafe.error.OSDriveError
import timber.log.Timber

object GoogleDriveHelper {
    fun getAuthorizationIntent(error: OSDriveError): Intent? {
        val cause: UserRecoverableAuthIOException? = error.cause as? UserRecoverableAuthIOException
        if (cause == null) {
            Timber.e(error, "Error cause's is not UserRecoverableAuthIOException")
        }
        return cause?.intent
    }
}