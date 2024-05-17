/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 5/2/2024 - for the oneSafe6 SDK.
 * Last modified 5/2/24, 4:02 PM
 */

package studio.lunabee.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.test.platform.app.InstrumentationRegistry
import com.google.protobuf.kotlin.toByteStringUtf8
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import studio.lunabee.onesafe.cryptography.EncryptedDataStoreEngine
import studio.lunabee.onesafe.cryptography.ProtoData
import studio.lunabee.onesafe.cryptography.utils.ProtoDataSerializer
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@HiltAndroidTest
class RenameLegacyDatastoreTest {

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    private val legacyFilename = "legacy_datastore"

    private val Context.legacyDatastore: DataStore<ProtoData> by dataStore(
        fileName = legacyFilename,
        serializer = ProtoDataSerializer,
    )
    private val newFilename = "crypto_datastore" // CryptoDatastoreTestModule.ProtoDataDatastoreFile

    private val Context.newDatastore: DataStore<ProtoData> by dataStore(
        fileName = newFilename,
        serializer = ProtoDataSerializer,
    )

    @Inject
    internal lateinit var dataStore: dagger.Lazy<EncryptedDataStoreEngine>

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Inject
    lateinit var encryptedDatastoreFileMigration: RenameLegacyDatastore

    @Test
    fun migrateLegacyFile_test(): TestResult = runTest {
        // Add value in legacy file
        val testKey = "test"
        val testValue = "test_value"
        context.legacyDatastore.updateData { data ->
            data.toBuilder().putData(testKey, testValue.toByteStringUtf8()).build()
        }
        assertTrue(context.dataStoreFile(legacyFilename).exists())

        encryptedDatastoreFileMigration(legacyFilename, newFilename)

        // Instantiate and add value to new file
        val testKey2 = "test2"
        val testValue2 = "test_value2"
        dataStore.get().insertValue(testKey2, testValue2.encodeToByteArray())

        assertEquals(testValue, context.newDatastore.data.first().dataMap[testKey]!!.toStringUtf8())
        assertEquals(testValue2, dataStore.get().retrieveValue(testKey2).first()!!.decodeToString())
        assertFalse(context.dataStoreFile(legacyFilename).exists())
    }
}
