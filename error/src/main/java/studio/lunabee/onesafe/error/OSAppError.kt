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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.error

data class OSAppError(
    val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause) {
    enum class Code(val message: String) {
        UNIMPLEMENTED_FEATURE("The feature is not implemented yet"),
        NO_ITEM_FOUND_FOR_ID("No item found for the provided id"),
        SAFE_ITEM_CREATION_FAILURE("The SafeItem couldn't be created"),
        SAFE_ITEM_EDITION_FAILURE("The SafeItem couldn't be edited"),
        SIGN_UP_FAILURE("The signUp failed"),
        BIOMETRIC_TOO_WEAK("This device has no strong biometric"),
        BIOMETRIC_ERROR("Error during biometric setup"),
        BIOMETRIC_LOGIN_ERROR("Error during biometric login"),
        URI_INVALID("Invalid uri from content provider"),
        MIGRATION_IMPORT_MODE_NOT_SET("Import mode expected to be set"),
        MIGRATION_ONESAFE5_SERVICE_NULL_BINDING("The oneSafe 5 service bind is null"),
        URL_DATA_FETCHING_FAIL("Error during data fetching"),
        MIGRATION_MISSING_PASSWORD("No encrypted password set in migration manager"),
    }
}
