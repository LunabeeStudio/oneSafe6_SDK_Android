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
 * Last modified 11/7/23, 2:48 PM
 */

package studio.lunabee.onesafe.importexport

import studio.lunabee.onesafe.domain.model.safe.SafeId

object ImportExportAndroidConstants {
    fun autoBackupWorkerName(safeId: SafeId): String = "bdacea8b-714e-4053-b3a7-add07dbe1c50_${safeId.id}"
    fun autoBackupWorkerTag(safeId: SafeId): String = "fac62754-ddfe-4777-aeb6-e59591bbfc5c_${safeId.id}"
    const val AUTO_BACKUP_SCHEME: String = "autobackup"
    const val MimeTypeOs6lsb: String = "*/*"
}
