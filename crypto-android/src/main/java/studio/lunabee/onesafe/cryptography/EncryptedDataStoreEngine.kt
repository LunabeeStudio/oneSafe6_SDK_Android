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

package studio.lunabee.onesafe.cryptography

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import com.google.protobuf.kotlin.toByteString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.cryptography.extension.use
import studio.lunabee.onesafe.error.OSCryptoError
import java.io.ByteArrayOutputStream
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class EncryptedDataStoreEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val androidKeyStoreEngine: AndroidKeyStoreEngine,
    private val ivProvider: IVProvider,
    override val dataStore: DataStore<ProtoData>,
) : DatastoreEngine {

    private fun getCipher() = Cipher.getInstance(TRANSFORMATION)

    /**
     * Encrypt the value and store it in datastore
     */
    override suspend fun editValue(value: ByteArray?, key: String) {
        val encValue = value?.let { encryptData(value) }
        dataStore.updateData { data ->
            if (encValue == null) {
                data.toBuilder().removeData(key).build()
            } else {
                data.toBuilder().putData(key, encValue.toByteString()).build()
            }
        }
    }

    /**
     * Retrieve Value from datastore and decrypt it
     */
    override fun retrieveValue(
        key: String,
    ): Flow<ByteArray?> = dataStore.data
        .map { securedData ->
            val encValue = try {
                securedData.dataMap[key]
            } catch (e: Exception) {
                null
            }
            encValue?.toByteArray()?.let(::decryptData)
        }

    @Throws(OSCryptoError::class)
    @Suppress("ThrowsCount")
    private fun decryptData(encData: ByteArray): ByteArray {
        try {
            withMasterKey { masterKey ->
                val cipher = getCipher()
                val iv: ByteArray = encData.copyOfRange(0, AES_GCM_IV_LENGTH)
                val ivSpec = getGcmParameterSpec(iv)
                cipher.init(Cipher.DECRYPT_MODE, masterKey, ivSpec)
                return cipher.doFinal(
                    encData,
                    AES_GCM_IV_LENGTH,
                    encData.size - AES_GCM_IV_LENGTH,
                )
            }
        } catch (e: Exception) {
            when (e) {
                is KeyPermanentlyInvalidatedException,
                is AEADBadTagException,
                -> throw OSCryptoError(OSCryptoError.Code.DATASTORE_KEY_PERMANENTLY_INVALIDATE, cause = e)
                is IndexOutOfBoundsException -> throw OSCryptoError(OSCryptoError.Code.DECRYPTION_UNKNOWN_FAILURE, cause = e)
                else -> throw e
            }
        }
    }

    private fun encryptData(value: ByteArray): ByteArray {
        return withMasterKey { masterKey ->
            val bos = ByteArrayOutputStream()
            val cipher = getCipher()
            val iv = ivProvider(AES_GCM_IV_LENGTH)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey, getGcmParameterSpec(iv))
            @Suppress("BlockingMethodInNonBlockingContext")
            bos.write(iv)
            CipherOutputStream(bos, cipher).use { cos ->
                value.inputStream().use { input ->
                    input.copyTo(cos)
                }
            }
            bos.toByteArray()
        }
    }

    override suspend fun clearDataStore() {
        super.clearDataStore()
        androidKeyStoreEngine.removeSecretKey(MASTER_KEY_ALIAS)
        androidKeyStoreEngine.removeSecretKey(BiometricEngine.KEY_ALIAS)
    }

    private fun getGcmParameterSpec(iv: ByteArray) = GCMParameterSpec(AES_GCM_TAG_LENGTH_IN_BITS, iv)

    private fun getKeyGenParameterSpec(): KeyGenParameterSpec {
        val builder = KeyGenParameterSpec.Builder(MASTER_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        builder.setBlockModes(BLOCK_MODE)
        builder.setEncryptionPaddings(PADDING)
        builder.setUserAuthenticationRequired(false)
        builder.setRandomizedEncryptionRequired(false) // Required
        builder.setStrongBoxBackedIfAvailable()
        return builder.build()
    }

    private fun KeyGenParameterSpec.Builder.setStrongBoxBackedIfAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val isAvailable = context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
            this.setIsStrongBoxBacked(isAvailable)
        }
    }

    private inline fun <T> withMasterKey(block: (masterKey: SecretKey) -> T): T {
        return androidKeyStoreEngine.retrieveOrGenerateSecretKey(
            keyName = MASTER_KEY_ALIAS,
            keyGenParameterSpec = getKeyGenParameterSpec(),
        ).use { block(it) }
    }

    companion object {
        private const val AES_GCM_IV_LENGTH = 12
        private const val AES_GCM_TAG_LENGTH_IN_BITS = 128
        private const val MASTER_KEY_ALIAS = "5ce9163a-e77a-4966-8966-a575cf286608"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
