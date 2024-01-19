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

import androidx.core.util.AtomicFile
import androidx.test.platform.app.InstrumentationRegistry
import studio.lunabee.onesafe.test.CryptoTestUtils
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import java.io.File
import java.util.Base64
import kotlin.test.assertContentEquals

object CryptoAndroidTestUtils : CryptoTestUtils({ Base64.getDecoder().decode(it) })

suspend fun CryptoEngine.encrypt_chacha20poly1305_data() {
    val actualData = this.encrypt(CryptoAndroidTestUtils.plainData, CryptoAndroidTestUtils.key256, null)
    assertContentEquals(CryptoAndroidTestUtils.chacha20poly1305_data, actualData)
}

suspend fun CryptoEngine.encrypt_chacha20poly1305_data_authenticated() {
    val actualData = this.encrypt(CryptoAndroidTestUtils.plainData, CryptoAndroidTestUtils.key256, CryptoAndroidTestUtils.ad)
    assertContentEquals(CryptoAndroidTestUtils.chacha20poly1305_data_authenticated, actualData)
}

suspend fun CryptoEngine.decrypt_chacha20poly1305_data() {
    val actualData = this.decrypt(CryptoAndroidTestUtils.chacha20poly1305_data, CryptoAndroidTestUtils.key256, null)
    assertContentEquals(CryptoAndroidTestUtils.plainData, actualData)
}

suspend fun CryptoEngine.decrypt_chacha20poly1305_data_authenticated() {
    val actualData = this.decrypt(
        CryptoAndroidTestUtils.chacha20poly1305_data_authenticated,
        CryptoAndroidTestUtils.key256,
        CryptoAndroidTestUtils.ad,
    )
    assertContentEquals(CryptoAndroidTestUtils.plainData, actualData)
}

suspend fun CryptoEngine.decrypt_chacha20poly1305_file() {
    val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir

    val cipherFile = File(cacheDir, "cipher_file")
    LbcResourcesHelper.copyResourceToDeviceFile(CryptoAndroidTestUtils.chacha20poly1305_file.name, cipherFile)

    val plainFile = File(cacheDir, "plain_file")
    LbcResourcesHelper.copyResourceToDeviceFile(CryptoAndroidTestUtils.plainFile.name, plainFile)

    val aEncFile = AtomicFile(cipherFile)
    val actualData = this.decrypt(aEncFile, CryptoAndroidTestUtils.key256, null)
    val expectedData = plainFile.readBytes()
    assertContentEquals(expectedData, actualData)
}

suspend fun CryptoEngine.encrypt_aes256gcm_data() {
    val actualData = this.encrypt(CryptoAndroidTestUtils.plainData, CryptoAndroidTestUtils.key256, null)
    assertContentEquals(CryptoAndroidTestUtils.aes256gcm_data, actualData)
}

suspend fun CryptoEngine.encrypt_aes256gcm_data_authenticated() {
    val actualData = this.encrypt(CryptoAndroidTestUtils.plainData, CryptoAndroidTestUtils.key256, CryptoAndroidTestUtils.ad)
    assertContentEquals(CryptoAndroidTestUtils.aes256gcm_data, actualData)
}

suspend fun CryptoEngine.decrypt_aes256gcm_data() {
    val actualData = this.decrypt(CryptoAndroidTestUtils.aes256gcm_data, CryptoAndroidTestUtils.key256, null)
    assertContentEquals(CryptoAndroidTestUtils.plainData, actualData)
}

suspend fun CryptoEngine.decrypt_aes256gcm_data_authenticated() {
    val actualData = this.decrypt(CryptoAndroidTestUtils.aes256gcm_data, CryptoAndroidTestUtils.key256, CryptoAndroidTestUtils.ad)
    assertContentEquals(CryptoAndroidTestUtils.plainData, actualData)
}

suspend fun CryptoEngine.decrypt_aes256gcm_file() {
    val cacheDir = InstrumentationRegistry.getInstrumentation().targetContext.cacheDir

    val cipherFile = File(cacheDir, "cipher_file")
    LbcResourcesHelper.copyResourceToDeviceFile(CryptoAndroidTestUtils.aes256gcm_file.name, cipherFile)

    val plainFile = File(cacheDir, "plain_file")
    LbcResourcesHelper.copyResourceToDeviceFile(CryptoAndroidTestUtils.plainFile.name, plainFile)

    val aEncFile = AtomicFile(cipherFile)
    val actualData = this.decrypt(aEncFile, CryptoAndroidTestUtils.key256, null)
    val expectedData = plainFile.readBytes()
    assertContentEquals(expectedData, actualData)
}
