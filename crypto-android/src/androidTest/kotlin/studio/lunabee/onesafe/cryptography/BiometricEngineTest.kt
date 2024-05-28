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

import androidx.biometric.BiometricManager
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test
import studio.lunabee.onesafe.error.OSCryptoError
import java.lang.reflect.InvocationTargetException
import java.security.KeyStore
import javax.inject.Inject
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@HiltAndroidTest
class BiometricEngineTest {
    private val context by lazy { InstrumentationRegistry.getInstrumentation().targetContext }

    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var dataStore: EncryptedDataStoreEngine

    @Inject
    internal lateinit var androidKeyStoreEngine: AndroidKeyStoreEngine

    private val biometricEngine by lazy {
        BiometricEngine(androidKeyStoreEngine, dataStore)
    }

    private val encMasterKeyAlias: String by lazy(::accessEncMasterKeyString)

    private val keyStoreKeyName: String by lazy(::accessKeyStoreKeyName)

    @Before
    fun setUp() {
        hiltRule.inject()
        runTest {
            dataStore.clearDataStore()
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(keyStoreKeyName)
        }
    }

    @Test
    fun get_cipher_login_secret_key_not_generated_test(): TestResult = runTest {
        val error = assertFailsWith<OSCryptoError> {
            biometricEngine.getCipherBiometricForDecrypt()
        }
        assertEquals(OSCryptoError.Code.BIOMETRIC_KEY_NOT_GENERATED, error.code)
    }

    @Test
    fun retrieve_key_not_generated_test(): TestResult = runTest {
        if (hasBiometric()) {
            val cipher = biometricEngine.createCipherBiometricForEncrypt()
            val error = assertFailsWith<OSCryptoError> {
                biometricEngine.retrieveKey(cipher)
            }
            assertEquals(OSCryptoError.Code.BIOMETRIC_DECRYPTION_FAIL, error.code)
        } else {
            println("retrieve_key_not_generated_test requires biometrics, bypassing")
        }
    }

    @Test
    fun retrieve_key_not_authenticated_test(): TestResult = runTest {
        if (hasBiometric()) {
            dataStore.insertValue(encMasterKeyAlias, "key".encodeToByteArray())
            assertEquals(true, biometricEngine.hasEncryptedMasterKeyStored().first())

            val cipher = biometricEngine.createCipherBiometricForEncrypt()
            val error = assertFailsWith<OSCryptoError> {
                biometricEngine.retrieveKey(cipher)
            }
            assertEquals(OSCryptoError.Code.BIOMETRIC_DECRYPTION_NOT_AUTHENTICATED, error.code)
        } else {
            println("retrieve_key_not_authenticated_test requires biometrics, bypassing")
        }
    }

    @Test
    fun set_new_encrypted_master_key_already_exist(): TestResult = runTest {
        setEncMasterKey("key".encodeToByteArray())
        val error = assertFailsWith<InvocationTargetException> {
            setEncMasterKey("newKey".encodeToByteArray())
        }
        assertEquals(OSCryptoError.Code.BIOMETRIC_MASTER_KEY_ALREADY_GENERATED, (error.cause as OSCryptoError).code)
    }

    @Test
    fun set_new_iv_master_key_already_exist(): TestResult = runTest {
        setIVBiometric("iv".encodeToByteArray())
        val error = assertFailsWith<InvocationTargetException> {
            setIVBiometric("ivNew".encodeToByteArray())
        }
        assertEquals(OSCryptoError.Code.BIOMETRIC_IV_ALREADY_GENERATED, (error.cause as OSCryptoError).code)
    }

    @Test
    fun has_biometric_flow_test() {
        runTest {
            this.launch {
                biometricEngine.hasEncryptedMasterKeyStored().collectIndexed { index, value ->
                    when (index) {
                        0 -> {
                            assertEquals(false, value)
                            setEncMasterKey("key".encodeToByteArray())
                        }
                        1 -> {
                            assertEquals(true, value)
                            setEncMasterKey(null)
                        }
                        2 -> {
                            assertEquals(false, value)
                            this@launch.cancel()
                        }
                    }
                }
            }
        }
    }

    private fun setEncMasterKey(value: ByteArray?) {
        val field = (
            biometricEngine::class.declaredMemberProperties.find { it.name == "encryptedMasterKey" }
                .apply { this?.isAccessible = true } as KMutableProperty<*>
            )
        field.setter.call(biometricEngine, value)
    }

    private fun setIVBiometric(value: ByteArray?) {
        val field = (
            biometricEngine::class.declaredMemberProperties.find { it.name == "biometricIV" }
                .apply { this?.isAccessible = true } as KMutableProperty<*>
            )
        field.setter.call(biometricEngine, value)
    }

    private fun accessEncMasterKeyString(): String {
        return BiometricEngine::class.java.getDeclaredField("ENC_MASTER_KEY_KEY").apply {
            this.isAccessible = true
        }.get(biometricEngine) as String
    }

    private fun accessKeyStoreKeyName(): String {
        return BiometricEngine::class.java.getDeclaredField("KEY_ALIAS").apply {
            this.isAccessible = true
        }.get(biometricEngine) as String
    }

    private fun hasBiometric(): Boolean {
        return BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }
}
