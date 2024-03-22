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

object AndroidConfig {
    private const val VERSION_CODE: Int = 9999
    private const val VERSION_NAME: String = "6.2.25.0"
    const val COMPILE_SDK: Int = 34
    const val TARGET_SDK: Int = COMPILE_SDK
    const val MIN_APP_SDK: Int = 24
    const val MIN_LIB_SDK: Int = 21
    const val BUILD_TOOLS_VERSION: String = "34.0.0"
    const val ONESAFE_SDK_VERSION: String = "2.1.0"

    const val CRYPTO_BACKEND_FLAVOR_DIMENSION: String = "crypto"
    const val CRYPTO_BACKEND_FLAVOR_JCE: String = "jce"
    const val CRYPTO_BACKEND_FLAVOR_TINK: String = "tink"
    const val CRYPTO_BACKEND_FLAVOR_DEFAULT: String = CRYPTO_BACKEND_FLAVOR_JCE

    val envVersionCode: Int = System.getenv(EnvConfig.ENV_VERSION_CODE)?.toInt() ?: VERSION_CODE
    val envVersionName: String = System.getenv(EnvConfig.ENV_VERSION_NAME) ?: VERSION_NAME
}
