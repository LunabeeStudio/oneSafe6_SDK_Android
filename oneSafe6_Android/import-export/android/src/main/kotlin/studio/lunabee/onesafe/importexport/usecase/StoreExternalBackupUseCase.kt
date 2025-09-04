/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 4/12/2024 - for the oneSafe6 SDK.
 * Last modified 2/27/24, 4:39 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import android.content.Context
import android.net.Uri
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import studio.lunabee.onesafe.commonui.utils.FileDetails
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.importexport.utils.isOsFile
import java.io.FileNotFoundException
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/**
 * Cache an external backup file in internal storage
 */
class StoreExternalBackupUseCase @Inject constructor(
    private val backupRepository: LocalBackupRepository,
    @param:ApplicationContext private val context: Context,
    private val clock: Clock,
    private val safeRepository: SafeRepository,
) {
    suspend operator fun invoke(backupUri: Uri): Flow<LBFlowResult<LocalBackup>> = flow {
        val isOsFile = FileDetails.fromUri(backupUri, context).isOsFile()
        if (isOsFile) {
            val inputStream = try {
                context.contentResolver.openInputStream(backupUri)
            } catch (e: FileNotFoundException) {
                emit(LBFlowResult.Failure(OSImportExportError.Code.CANNOT_OPEN_URI.get(cause = e)))
                return@flow
            }

            if (inputStream == null) {
                emit(LBFlowResult.Failure(OSImportExportError.Code.CANNOT_OPEN_URI.get()))
            } else {
                val backup = inputStream.use {
                    val now = Instant.now(clock)
                    LocalBackup(
                        date = now,
                        file = backupRepository.cacheBackup(it, now),
                        safeId = safeRepository.currentSafeId(),
                    )
                }
                emit(LBFlowResult.Success(backup))
            }
        } else {
            emit(LBFlowResult.Failure(OSImportExportError.Code.FILE_NOT_A_BACKUP.get()))
        }
    }.onStart { emit(LBFlowResult.Loading()) }
}
