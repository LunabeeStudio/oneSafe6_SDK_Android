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
 * Created by Lunabee Studio / Date - 10/2/2023 - for the oneSafe6 SDK.
 * Last modified 10/2/23, 10:53 AM
 */

package studio.lunabee.onesafe.importexport.model

import java.time.format.DateTimeFormatter

object ImportExportConstant {
    const val ExtensionOs6Backup: String = "os6lsb"
    const val ExtensionOs6Sharing: String = "os6lss"
    const val ArchiveToImportCopyFileName: String = "archiveToImportCopy.$ExtensionOs6Backup"

    const val ArchiveFilePrefix: String = "oneSafe"
    const val ArchiveFileSeparator: String = "-"
    val ArchiveTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmmss")
    val ArchiveDateFormatter: DateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE

    fun isOS6Extension(extension: String): Boolean {
        return extension == ExtensionOs6Backup || extension == ExtensionOs6Sharing
    }
}
