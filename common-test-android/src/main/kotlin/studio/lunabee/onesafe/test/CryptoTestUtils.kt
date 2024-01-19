/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Last modified 4/7/23, 12:30 AM
 */

package studio.lunabee.onesafe.test

import java.io.File

@Suppress("PropertyName", "VariableNaming")
open class CryptoTestUtils(base64Decode: (String) -> ByteArray) {
    val iv12: ByteArray = base64Decode("tTvx8LSk38YNztWs")
    val ad: ByteArray = base64Decode("YXplcnR5")
    val key256: ByteArray = base64Decode("NPK+Xd162lfUdcekSXF/l4gKO3PwSGOTLOQ8Kd+0hVs=")

    val chacha20poly1305_data: ByteArray = base64Decode("tTvx8LSk38YNztWsB7bdukMLZpTfCc/WyCiBOy+I8tba/A==")
    val chacha20poly1305_data_authenticated: ByteArray =
        base64Decode("tTvx8LSk38YNztWsB7bdukMLwgOvQbi4lP/B5ZnXjOkfkw==")
    val aes256gcm_data: ByteArray = base64Decode("tTvx8LSk38YNztWsLHtmXVAFlB9RuTOhJvG0ZXN29xmTww==")
    val plainData: ByteArray = base64Decode("AQIDBAUG")

    val aes256gcm_file: File = File("../common-test-android/src/main/resources", "pixel_3xl_cam_encrypted_aes256gcm")
    val chacha20poly1305_file: File =
        File("../common-test-android/src/main/resources", "pixel_3xl_cam_encrypted_chacha20poly1305")
    val plainFile: File = File("../common-test-android/src/main/resources", "pixel_3xl_cam.webp")
}
