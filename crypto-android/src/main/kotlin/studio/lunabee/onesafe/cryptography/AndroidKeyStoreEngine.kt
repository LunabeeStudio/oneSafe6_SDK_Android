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

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import studio.lunabee.onesafe.error.OSCryptoError
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject

@Suppress("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.M)
class AndroidKeyStoreEngine @Inject constructor() {

    private val keyStore: KeyStore
        get() = KeyStore.getInstance(provider).apply { this.load(null) }

    @Throws(OSCryptoError::class)
    fun retrieveSecretKeyFromKeyStore(keyName: String): SecretKey {
        val secretKey = keyStore.getKey(keyName, null) as? SecretKey
        return secretKey ?: throw OSCryptoError(OSCryptoError.Code.KEYSTORE_KEY_NOT_GENERATED)
    }

    @Throws(OSCryptoError::class)
    fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec): SecretKey {
        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                provider,
            )
            keyGenerator.init(keyGenParameterSpec)
            return keyGenerator.generateKey()
        } catch (e: Exception) {
            throw OSCryptoError(OSCryptoError.Code.KEYSTORE_KEY_ERROR_CREATION_ERROR, cause = e)
        }
    }

    fun retrieveOrGenerateSecretKey(keyName: String, keyGenParameterSpec: KeyGenParameterSpec): SecretKey {
        return try {
            retrieveSecretKeyFromKeyStore(keyName)
        } catch (e: OSCryptoError) {
            generateSecretKey(keyGenParameterSpec)
        }
    }

    fun removeSecretKey(keyName: String) {
        keyStore.deleteEntry(keyName)
    }

    companion object {
        private const val provider = "AndroidKeyStore"
    }
}
