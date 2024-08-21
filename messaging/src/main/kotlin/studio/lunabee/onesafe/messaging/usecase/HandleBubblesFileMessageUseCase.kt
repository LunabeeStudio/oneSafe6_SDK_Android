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
 * Created by Lunabee Studio / Date - 8/7/2024 - for the oneSafe6 SDK.
 * Last modified 07/08/2024 13:50
 */

package studio.lunabee.onesafe.messaging.usecase

import android.content.Context
import android.net.Uri
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import studio.lunabee.messaging.domain.MessagingConstant
import studio.lunabee.messaging.domain.model.DecryptResult
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.utils.mkdirs
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.importexport.usecase.ArchiveUnzipUseCase
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import javax.inject.Inject

class HandleBubblesFileMessageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
    private val unzipUseCase: ArchiveUnzipUseCase,
    private val decryptBubblesSafeItemMessageUseCase: DecryptBubblesSafeItemMessageUseCase,
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Message) private val messageArchiveDir: File,
) {
    operator fun invoke(fileUri: Uri): Flow<LBFlowResult<DecryptResult?>> = flow {
        isSafeReadyUseCase.wait()
        emit(LBFlowResult.Loading())
        messageArchiveDir.mkdirs(override = true)
        context.contentResolver.openInputStream(fileUri)?.use { stream ->
            emitAll(unzip(stream))
        }
    }.catch { e ->
        when (e) {
            is FileNotFoundException -> // ignore non-file URIs
                emit(LBFlowResult.Success(null))
            else -> throw e
        }
    }

    private fun unzip(stream: InputStream): Flow<LBFlowResult<DecryptResult?>> {
        return unzipUseCase(stream, messageArchiveDir).transformResult {
            // Assert that file is really a message file
            val messageFile = File(messageArchiveDir, MessagingConstant.MessageFileName)
            val result = if (messageFile.exists()) {
                decryptBubblesSafeItemMessageUseCase(messageArchiveDir).asFlowResult()
            } else {
                LBFlowResult.Success(null)
            }
            emit(result)
        }.catch { e ->
            when (e) {
                is IllegalArgumentException ->
                    emit(LBFlowResult.Failure(OSDomainError(code = OSDomainError.Code.DECRYPT_MESSAGE_NOT_BASE64, cause = e)))
                else -> throw e
            }
        }
    }
}
