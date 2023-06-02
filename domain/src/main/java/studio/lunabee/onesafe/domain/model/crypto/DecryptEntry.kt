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

package studio.lunabee.onesafe.domain.model.crypto

import kotlin.reflect.KClass

/**
 * Wrapper for the data to decrypt
 *
 * @param data The encrypted data
 * @param clazz The target class to return after decrypt
 * @param mapper Optional mapper to map raw [ByteArray] to [Data]
 */
class DecryptEntry<Data : Any>(
    val data: ByteArray,
    val clazz: KClass<Data>,
    val mapper: (ByteArray.() -> Data)? = null,
)

/**
 * Wrapper for the data to encrypt
 *
 * @param data The plain data
 * @param mapper Optional mapper to map [Data] to raw [ByteArray]
 */
class EncryptEntry<Data : Any>(
    val data: Data,
    val mapper: ((Data) -> ByteArray)? = null,
)
