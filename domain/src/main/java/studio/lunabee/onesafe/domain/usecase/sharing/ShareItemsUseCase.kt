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

package studio.lunabee.onesafe.domain.usecase.sharing

import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.domain.model.password.PasswordConfig
import studio.lunabee.onesafe.domain.model.share.SharingData
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.qualifier.VersionName
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.GeneratePasswordUseCase
import studio.lunabee.onesafe.error.OSImportExportError
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ShareItemsUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val generatePasswordUseCase: GeneratePasswordUseCase,
    private val prepareSharingUseCase: PrepareSharingUseCase,
    private val exportShareUseCase: ExportShareUseCase,
    @BuildNumber private val buildNumber: Int,
    @VersionName private val versionName: String,
) {
    operator fun invoke(
        itemId: UUID,
        includeChildren: Boolean,
        archiveExtractedDirectory: File,
    ): Flow<LBFlowResult<SharingData>> = flow {
        emit(LBFlowResult.Loading())

        // Generate a password for the sharing
        val password = generatePasswordUseCase(
            PasswordConfig(
                length = PasswordLength,
                includeUpperCase = true,
                includeLowerCase = true,
                includeNumber = true,
                includeSymbol = true,
            ),
        ).value

        prepareSharingUseCase(password.toCharArray(), buildNumber, versionName).collect { prepareSharingResult ->
            when (prepareSharingResult) {
                is LBFlowResult.Success -> {
                    exportShareUseCase(itemId, includeChildren, archiveExtractedDirectory).collect { result ->
                        when (result) {
                            is LBFlowResult.Success -> {
                                val file = result.data
                                if (file != null) {
                                    emit(
                                        LBFlowResult.Success(
                                            SharingData(
                                                password = password,
                                                file = file,
                                                itemsNbr = safeItemRepository.getSafeItemsAndChildren(itemId, includeChildren).size,
                                            ),
                                        ),
                                    )
                                } else {
                                    emit(LBFlowResult.Failure(OSImportExportError(OSImportExportError.Code.EXPORT_DATA_FAILURE)))
                                }
                            }
                            is LBFlowResult.Failure -> emit(LBFlowResult.Failure(result.throwable))
                            is LBFlowResult.Loading -> {
                                /* no-op */
                            }
                        }
                    }
                }
                is LBFlowResult.Failure -> emit(LBFlowResult.Failure(prepareSharingResult.throwable))
                is LBFlowResult.Loading -> {
                    /* no-op */
                }
            }
        }
    }
}

private const val PasswordLength: Int = 14
