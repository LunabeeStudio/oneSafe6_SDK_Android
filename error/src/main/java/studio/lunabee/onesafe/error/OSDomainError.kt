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

data class OSDomainError(
    val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause) {
    enum class Code(val message: String) {
        SAFE_ITEM_DELETE_FAILURE("The SafeItem couldn't be move to bin"),
        SAFE_ITEM_REMOVE_FAILURE("The SafeItem couldn't be move to permanently removed"),
        SAFE_ITEM_NO_ICON("The SafeItem does not have an icon set"),
        DUPLICATE_ICON_FAILED("Icon duplication failed during item duplication"),
        OLD_DATA_IMPORT_FAILURE("The import of previous data has failed"),
        SIGNIN_NOT_SIGNED_UP("The user is not signed up"),
        UNZIP_FAILURE("Unable to unzip selected file"),
        ZIP_FAILURE("Unable to zip selected file"),
        WRONG_CONFIRMATION_PASSWORD("Password confirmation does not match"),
        NO_MATCHING_CONTACT("No contact key matching the encrypted message"),
        DUPLICATED_MESSAGE("The message has already been stored"),
    }
}
