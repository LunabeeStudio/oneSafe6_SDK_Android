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

data class OSStorageError(
    override val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause, code) {

    enum class Code(override val message: String) : ErrorCode<Code, OSStorageError> {
        ITEM_KEY_NOT_FOUND("The item key does not exist"),
        ITEM_NOT_FOUND("The item does not exist"),
        CONTACT_NOT_FOUND("The contact does not exist"),
        CONTACT_KEY_NOT_FOUND("The contact key does not exist"),
        UNKNOWN_DATABASE_ERROR("Unknown database error"),
        PROTO_DATASTORE_READ_ERROR("Cannot read datastore proto"),
        ENQUEUED_MESSAGE_ALREADY_EXIST_ERROR("The message has already been enqueued"),
        ENQUEUED_MESSAGE_NOT_FOUND_FOR_DELETE("No message with the id provided found for delete"),
        UNKNOWN_FILE_ERROR("Unknown file error"),
        MISSING_BACKUP_FILE("The file associated to the local backup does not exist on file system"),
        DATABASE_WRONG_KEY("Failed to decrypt the database with the provided key"),
        DATABASE_NOT_FOUND("The database does not exists"),
        DATABASE_BACKUP_ERROR("Something went wrong during database backup for migration"),
        DATABASE_CANNOT_ACCESS_DIR("Cannot access the database parent directory"),
        DATABASE_CANNOT_ACCESS_FILES("Cannot access files in database directory"),
        DATABASE_CORRUPTED("The database is corrupted"),
    }
}
