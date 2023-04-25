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

data class OSImportExportError(
    val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause) {

    enum class Code(val message: String) {
        METADATA_FILE_NOT_FOUND("Metadata file was not found"),
        ARCHIVE_MALFORMED("Unexpected archive content"),
        DATA_FILE_NOT_FOUND("Data file was not found"),
        ID_NOT_FOUND("Unexpected error while retrieving new id"),
        EXPORT_METADATA_FAILURE("Fail to export metadata"),
        EXPORT_DATA_FAILURE("Fail to export data"),
        EXPORT_ICON_FAILURE("Fail to export icons"),
        WRONG_CREDENTIALS("Wrong credentials"),
        UNEXPECTED_ERROR("Unexpected error occurred"),
        METADATA_NOT_IN_CACHE("Metadata not in cache"),
        SALT_INVALID("Salt is empty or invalid"),
    }
}
