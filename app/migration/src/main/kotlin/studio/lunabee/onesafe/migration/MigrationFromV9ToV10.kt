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
 * Created by Lunabee Studio / Date - 8/31/2023 - for the oneSafe6 SDK.
 * Last modified 31/08/2023 15:18
 */

package studio.lunabee.onesafe.migration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.util.AtomicFile
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.cryptography.CryptoDataMapper
import studio.lunabee.onesafe.cryptography.CryptoEngine
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.error.OSError
import java.io.File
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV9ToV10>()

/**
 * Migrate photo captured and saved in png to jpeg
 * Find these field by looking for mimetype png + field value with jpeg extension
 */
class MigrationFromV9ToV10 @Inject constructor(
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val cryptoEngine: CryptoEngine,
    private val cryptoDataMapper: CryptoDataMapper,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(masterKey: ByteArray): LBResult<Unit> = OSError.runCatching {
        val fields = safeItemFieldRepository.getAllSafeItemFields().groupBy { it.itemId }
        fields.forEach { (itemId, fields) ->
            val key = safeItemKeyRepository.getSafeItemKey(itemId)
            val plainKey = cryptoEngine.decrypt(key.encValue, masterKey, null)
            fields
                .filter { field ->
                    val fieldKind = field.encKind?.let { encKind ->
                        val plainKind = cryptoEngine.decrypt(encKind, plainKey, null)
                        cryptoDataMapper(null, plainKind, SafeItemFieldKind::class)
                    }
                    fieldKind == SafeItemFieldKind.Photo
                }
                .forEach { field ->
                    field.encValue?.let { encValue ->
                        val plainValue = cryptoEngine.decrypt(encValue, plainKey, null)
                        val filenameExt = cryptoDataMapper(null, plainValue, String::class)
                            .split(FileTypeExtSeparator)
                        val extension = filenameExt[1]
                        if (extension == "jpeg") {
                            val filename = filenameExt[0]
                            val file = File(context.filesDir, "$FileDir/$filename")
                            val aFile = AtomicFile(file)
                            val options = BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            cryptoEngine.getDecryptStream(aFile, plainKey, null).use { stream ->
                                BitmapFactory.decodeStream(stream, null, options)
                            }
                            val mimeType = options.outMimeType
                            if (mimeType == MimeTypePng) {
                                val bitmap = cryptoEngine.getDecryptStream(aFile, plainKey, null).use { stream ->
                                    BitmapFactory.decodeStream(stream)
                                }
                                val fileStream = aFile.startWrite()
                                try {
                                    cryptoEngine.getCipherOutputStream(fileStream, plainKey, null).use { cipherStream ->
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
}

private const val MimeTypePng: String = "image/png"
private const val FileDir: String = "files"
private const val FileTypeExtSeparator: Char = '|'
