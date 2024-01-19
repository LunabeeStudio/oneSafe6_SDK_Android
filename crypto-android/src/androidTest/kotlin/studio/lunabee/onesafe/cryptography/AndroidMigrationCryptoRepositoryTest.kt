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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.toByteArray
import javax.inject.Inject
import kotlin.test.assertContentEquals

@HiltAndroidTest
class AndroidMigrationCryptoRepositoryTest {

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    internal lateinit var androidMigrationCryptoRepository: AndroidMigrationCryptoRepository

    @Inject
    internal lateinit var rsaCryptoEngine: JceRsaCryptoEngine

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun encrypt_decrypt_keypair_test() {
        val expected = charArrayOf('1', '2', '3', '4', '5', '6')
        val rawPubKey = androidMigrationCryptoRepository.getMigrationPubKey()
        val publicKey = rsaCryptoEngine.getPublicKey(rawPubKey)
        val encData = rsaCryptoEngine.encrypt(publicKey, expected.toByteArray())
        val actualData = androidMigrationCryptoRepository.decryptMigrationArchivePassword(encData)

        assertContentEquals(expected, actualData)
    }
}
