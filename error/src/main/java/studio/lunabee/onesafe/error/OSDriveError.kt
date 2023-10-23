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
 * Last modified 10/10/23, 9:48 AM
 */

package studio.lunabee.onesafe.error

data class OSDriveError(
    override val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause, code) {

    enum class Code(override val message: String) : ErrorCode<OSDriveError> {
        WRONG_ACCOUNT_TYPE("The provided account must have \"com.google\" type"),
        REQUEST_EXECUTION_FAILED("The request failed to execute"),
        AUTHENTICATION_REQUIRED("User must authenticate by using the intent wrapped in error's cause"),
        UNEXPECTED_NULL_ACCOUNT("Unable to retrieve the Google account from account manager"),
        DRIVE_ENGINE_NOT_INITIALIZED("GoogleDriveEngine must be initialized by calling initialize fun"),
    }
}
