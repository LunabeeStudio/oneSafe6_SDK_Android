/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/23/2024 - for the oneSafe6 SDK.
 * Last modified 23/09/2024 09:15
 */

package studio.lunabee.onesafe.domain.repository

/**
 * Used to encrypt data before passing it to coroutine worker, prevent plain sensitive data to be kept in worker database.
 * Available without master key loaded
 */
interface WorkerCryptoRepository {
    suspend fun decrypt(data: ByteArray): ByteArray

    suspend fun encrypt(data: ByteArray): ByteArray
}
