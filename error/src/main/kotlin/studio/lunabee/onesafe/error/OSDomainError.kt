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
    override val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause, code) {
    enum class Code(override val message: String) : ErrorCode<Code, OSDomainError> {
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
        DECRYPT_MESSAGE_NOT_BASE64("The incoming message is not a base 64"),
        HAND_SHAKE_DATA_NOT_FOUND("The hand shake data required is missing and shouldn't be"),
        WRONG_CONTACT("this message is not for this contact"),
        NOT_AN_INVITATION_MESSAGE("this message is not an invitation message"),
        CRYPTO_NOT_READY_TIMEOUT("Time out on waiting for cryptographic keys to be loaded in memory"),
        MISSING_FILE_ID_IN_FIELD("The field does not contain the file id"),
        MISSING_URI_OUTPUT_STREAM("The selected file don't exist"),
        UNZIP_SECURITY_TRAVERSAL_VULNERABILITY("Archive contains unauthorized transversal path"),
        ALPHA_INDEX_COMPUTE_FAILED("Failed to compute the alphabetic index"),
        UNKNOWN_ERROR("Unknown error happened"),
        NO_HTML_PAGE_FOUND("No html page found for the provided url"),
        DATABASE_ENCRYPTION_NOT_ENABLED("The database encryption is not enabled"),
        DATABASE_ENCRYPTION_ALREADY_ENABLED("The database encryption is already enabled"),
        DATABASE_KEY_BAD_FORMAT("The string does not represent a 32 bytes database key"),
        DATABASE_ENCRYPTION_KEY_KEYSTORE_LOST("Unable to decrypt the database encryption key using the Android keystore"),
        SAFE_ID_NOT_READY_TIMEOUT("Time out on waiting for safe id to be loaded in memory"),
        MISSING_BIOMETRIC_KEY("The safe crypto object does not contain an encrypted biometric key"),
    }
}
