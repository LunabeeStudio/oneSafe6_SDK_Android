/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/29/2023 - for the oneSafe6 SDK.
 * Last modified 9/29/23, 5:25 PM
 */

package studio.lunabee.onesafe.importexport.engine

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.importexport.model.ExportData
import java.io.File

interface ExportEngine {
    fun createExportArchiveContent(
        dataHolderFolder: File,
        data: ExportData,
        archiveKind: OSArchiveKind,
    ): Flow<LBFlowResult<Unit>>
}

interface ShareExportEngine : ExportEngine {
    val exportKey: ByteArray
    fun buildExportInfo(password: CharArray): Flow<LBFlowResult<Unit>>
}

interface BackupExportEngine : ExportEngine
