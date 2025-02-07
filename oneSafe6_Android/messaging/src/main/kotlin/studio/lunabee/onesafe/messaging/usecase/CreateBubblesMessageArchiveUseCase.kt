/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/8/2024 - for the oneSafe6 SDK.
 * Last modified 08/08/2024 10:14
 */

package studio.lunabee.onesafe.messaging.usecase

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.importexport.usecase.ArchiveZipUseCase
import java.io.File
import java.util.UUID
import javax.inject.Inject

class CreateBubblesMessageArchiveUseCase @Inject constructor(
    private val archiveZipUseCase: ArchiveZipUseCase,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Message) private val archiveDir: File,
) {
    operator fun invoke(
        messageData: ByteArray,
        attachmentFile: File?,
    ): Flow<LBFlowResult<File>> {
        archiveDir.mkdirs()
        val finalArchiveFile = File(archiveDir, "${UUID.randomUUID()}.zip")
        val tempArchiveFile = File(archiveDir, UUID.randomUUID().toString())
        val tempMessageFile = File(archiveDir, UUID.randomUUID().toString())
        attachmentFile?.let {
            val finalAttachmentFile = File(tempArchiveFile, MessagingConstant.AttachmentFileName)
            attachmentFile.copyTo(finalAttachmentFile)
            attachmentFile.delete()
        }
        tempMessageFile.outputStream().use { it.write(messageData) }
        val messageFile = File(tempArchiveFile, MessagingConstant.MessageFileName)
        tempMessageFile.copyTo(messageFile)
        return archiveZipUseCase(
            folderToZip = tempArchiveFile,
            outputZipFile = finalArchiveFile,
        ).map {
            if (it !is LBFlowResult.Loading) {
                tempArchiveFile.deleteRecursively()
                tempMessageFile.delete()
            }
            it
        }
    }
}
