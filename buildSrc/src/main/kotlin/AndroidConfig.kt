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
    private const val VersionCode: Int = 9999
    private const val VersionName: String = "6.4.9.0"
    const val CompileSdk: Int = 36
    const val TargetSdk: Int = CompileSdk
    const val MinAppSdk: Int = 24
    const val MinLibSdk: Int = 23
    const val OnesafeSdkVersion: String = "2.1.0"

    const val CryptoBackendFlavorDimension: String = "crypto"
    const val CryptoBackendFlavorJce: String = "jce"
    const val CryptoBackendFlavorTink: String = "tink"
    const val CryptoBackendFlavorDefault: String = CryptoBackendFlavorJce

    val envVersionCode: Int = System.getenv(EnvConfig.EnvVersionCode)?.toInt() ?: VersionCode
    val envVersionName: String = System.getenv(EnvConfig.EnvVersionName) ?: VersionName
}
