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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/8/24, 4:58 PM
 */

package studio.lunabee.onesafe.migration.migration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.util.AtomicFile
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.cryptography.android.AndroidCryptoDataMapper
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.migration.MigrationSafeData0
import studio.lunabee.onesafe.migration.utils.MigrationCryptoV1UseCase
import java.io.File
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV9ToV10>()

/**
 * Migrate photo captured and saved in png to jpeg
 * Find these field by looking for mimetype png + field value with jpeg extension
 * The migration is non-blocking, which means if some field failed to migrate, the migration will not fail
 */
class MigrationFromV9ToV10 @Inject constructor(
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val cryptoDataMapper: AndroidCryptoDataMapper,
    @param:ApplicationContext private val context: Context,
    private val migrationCryptoV1UseCase: MigrationCryptoV1UseCase,
) : AppMigration0(9, 10) {
    override suspend fun migrate(migrationSafeData: MigrationSafeData0): LBResult<Unit> {
        val safeId = migrationSafeData.id
        val masterKey = migrationSafeData.masterKey
        val fieldsByItemId = safeItemFieldRepository.getAllSafeItemFields(safeId).groupBy { it.itemId }
        fieldsByItemId.forEach { (itemId, fields) ->
            OSError.runCatching(logger) {
                val key = safeItemKeyRepository.getSafeItemKey(itemId)
                val plainKey = migrationCryptoV1UseCase.decrypt(key.encValue, masterKey)
                fields
                    .filter { field ->
                        val fieldKind = field.encKind?.let { encKind ->
                            val plainKind = migrationCryptoV1UseCase.decrypt(encKind, plainKey)
                            cryptoDataMapper(null, plainKind, SafeItemFieldKind::class)
                        }
                        fieldKind == SafeItemFieldKind.Photo
                    }
                    .forEach { field ->
                        field.encValue?.let { encValue ->
                            val plainValue = migrationCryptoV1UseCase.decrypt(encValue, plainKey)
                            val filenameExt = cryptoDataMapper(null, plainValue, String::class)
                                .split(FileTypeExtSeparator)
                            val extension = filenameExt[1]
                            if (extension == "jpeg") {
                                val filename = filenameExt[0]
                                val file = File(context.filesDir, "$FileDir/$filename")
                                val aFile = AtomicFile(file)
                                val options = BitmapFactory.Options()
                                options.inJustDecodeBounds = true
                                migrationCryptoV1UseCase.getDecryptStream(aFile, plainKey).use { stream ->
                                    BitmapFactory.decodeStream(stream, null, options)
                                }
                                val mimeType = options.outMimeType
                                if (mimeType == MimeTypePng) {
                                    val bitmap = migrationCryptoV1UseCase.getDecryptStream(aFile, plainKey).use { stream ->
                                        BitmapFactory.decodeStream(stream)
                                    }
                                    val fileStream = aFile.startWrite()
                                    try {
                                        migrationCryptoV1UseCase.getCipherOutputStream(fileStream, plainKey).use { cipherStream ->
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, cipherStream)
                                        }
                                        logger.i("Migrate png -> jpeg for $filenameExt")
                                    } finally {
                                        aFile.finishWrite(fileStream)
                                    }
                                }
                            }
                        } ?: logger.e("Unexpected empty value in file field")
                    }
            }
        }
        return LBResult.Success(Unit)
    }
}

private const val MimeTypePng: String = "image/png"
private const val FileDir: String = "files"
private const val FileTypeExtSeparator: Char = '|'
