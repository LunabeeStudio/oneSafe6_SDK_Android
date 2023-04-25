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

package studio.lunabee.onesafe.domain.engine

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import java.io.File

interface ExportEngine {
    val exportKey: ByteArray
    fun prepareBackup(password: CharArray, platformInfo: String, masterSalt: ByteArray): Flow<LBFlowResult<Unit>>
    fun prepareSharing(password: CharArray, platformInfo: String): Flow<LBFlowResult<Unit>>
    fun createExportArchiveContent(
        dataHolderFolder: File,
        safeItemsWithKeys: Map<SafeItem, SafeItemKey>,
        safeItemFields: List<SafeItemField>,
        icons: List<File>,
        archiveKind: OSArchiveKind,
    ): Flow<LBFlowResult<Unit>>
}
