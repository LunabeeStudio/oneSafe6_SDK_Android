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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 3:34 PM
 */

package studio.lunabee.onesafe.messaging.crypto

import kotlinx.coroutines.flow.first
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.onesafe.cryptography.CryptoDataMapper
import studio.lunabee.onesafe.cryptography.CryptoEngine
import studio.lunabee.onesafe.cryptography.DatastoreEngine
import studio.lunabee.onesafe.cryptography.RandomKeyProvider
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.messaging.domain.repository.MessagingCryptoRepository
import studio.lunabee.onesafe.use
import java.io.OutputStream
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidMessagingCryptoRepository @Inject constructor(
    private val crypto: CryptoEngine,
    private val randomKeyProvider: RandomKeyProvider,
    private val mapper: CryptoDataMapper,
    @DatastoreEngineProvider(DataStoreType.Encrypted) private val dataStoreEngine: DatastoreEngine,
) : MessagingCryptoRepository {

    override suspend fun decryptMessage(
        data: ByteArray,
        key: DRMessageKey,
    ): ByteArray {
        return crypto.decrypt(data, key.value, null)
    }

    override suspend fun encryptMessage(
        outputStream: OutputStream,
        key: DRMessageKey,
    ): OutputStream = try {
        crypto.getCipherOutputStream(outputStream, key.value, null)
    } catch (e: GeneralSecurityException) {
        throw OSCryptoError(OSCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_BAD_CONTACT_KEY, cause = e)
    }

    override suspend fun <Data : Any> queueEncrypt(encryptEntry: EncryptEntry<Data>): ByteArray {
        return getOrCreateQueueKey().use { key ->
            val data = encryptEntry.data
            val mapBlock = encryptEntry.mapBlock
            val rawData = mapper(mapBlock, data)
            try {
                crypto.encrypt(rawData, key, null)
            } catch (e: GeneralSecurityException) {
                throw OSCryptoError(OSCryptoError.Code.BUBBLES_ENCRYPTION_FAILED_QUEUE_KEY, cause = e)
            }
        }
    }

    override suspend fun <Data : Any> queueDecrypt(decryptEntry: DecryptEntry<Data>): Data {
        return getOrCreateQueueKey().use { key ->
            val rawData = try {
                crypto.decrypt(decryptEntry.data, key, null)
            } catch (e: GeneralSecurityException) {
                throw OSCryptoError(OSCryptoError.Code.BUBBLES_DECRYPTION_FAILED_QUEUE_KEY, cause = e)
            }

            mapper(decryptEntry.mapBlock, rawData, decryptEntry.clazz)
        }
    }

    private suspend fun getOrCreateQueueKey(): ByteArray {
        var key = dataStoreEngine.retrieveValue(QUEUE_KEY_ALIAS).first()
        if (key == null) {
            key = randomKeyProvider()
            dataStoreEngine.editValue(key, QUEUE_KEY_ALIAS)
        }
        return key
    }

    companion object {
        private const val QUEUE_KEY_ALIAS = "bfe3f6a9-918a-4bf6-af38-786679fa0c82"
    }
}
