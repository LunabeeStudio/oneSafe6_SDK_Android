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

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError
import java.security.KeyStore
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@HiltAndroidTest
class EncryptedDatastoreTest {

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var dataStore: EncryptedDataStoreEngine

    private val keyAlias: String = "KeyAlias"

    @Before
    fun setUp() {
        hiltRule.inject()
        runTest {
            dataStore.removeValue(keyAlias)
        }
    }

    @Test
    fun insert_edit_retrieve_remove_test() {
        runTest {
            dataStore.insertValue(keyAlias, "value".encodeToByteArray())
            dataStore.insertValue(keyAlias, "value2".encodeToByteArray())
            assertEquals("value2", dataStore.retrieveValue(keyAlias).first()?.decodeToString())
            dataStore.removeValue(keyAlias)
            val actual = dataStore.retrieveValue(keyAlias).firstOrNull()
            assertNull(actual)
        }
    }

    @Test
    fun edit_value_override_error_test() {
        runTest {
            dataStore.insertValue(key = keyAlias, value = "key".encodeToByteArray(), override = false)
            val error = assertFailsWith<OSCryptoError> {
                dataStore.insertValue(key = keyAlias, value = byteArrayOf(), override = false)
            }
            assertEquals(OSCryptoError.Code.DATASTORE_ENTRY_KEY_ALREADY_EXIST, error.code)
        }
    }

    @Test
    fun delete_master_key_and_try_to_get_value_test() {
        runTest {
            dataStore.insertValue(keyAlias, "key".encodeToByteArray())
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(accessMasterKeyString())
            val error = assertFailsWith<OSError> {
                dataStore.retrieveValue(keyAlias).first()
            }
            assertEquals(OSCryptoError.Code.ANDROID_KEYSTORE_KEY_PERMANENTLY_INVALIDATE, (error as OSCryptoError).code)
        }
    }

    private fun accessMasterKeyString(): String {
        return EncryptedDataStoreEngine::class.java.getDeclaredField("MASTER_KEY_ALIAS").apply {
            this.isAccessible = true
        }.get(dataStore) as String
    }
}
