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

package studio.lunabee.onesafe.domain.usecase.archive

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.engine.ImportEngine
import studio.lunabee.onesafe.domain.qualifier.ArchiveDir
import java.io.File
import javax.inject.Inject

class ImportAuthUseCase @Inject constructor(
    private val importEngine: ImportEngine,
    @ArchiveDir(type = ArchiveDir.Type.Import) private val archiveExtractedDirectory: File,
) {
    operator fun invoke(
        password: CharArray,
    ): Flow<LBFlowResult<Unit>> {
        return importEngine.authenticateAndExtractData(
            archiveExtractedDirectory = archiveExtractedDirectory,
            password = password,
        )
    }
}
