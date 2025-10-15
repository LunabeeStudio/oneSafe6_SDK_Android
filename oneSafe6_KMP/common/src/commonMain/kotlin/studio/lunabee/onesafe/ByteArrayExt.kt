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
 * Created by Lunabee Studio / Date - 9/3/2024 - for the oneSafe6 SDK.
 * Last modified 9/3/24, 9:07 AM
 */

package studio.lunabee.onesafe

import kotlin.random.Random

/**
 * Randomize data in place. Not suitable for random cryptographic key generation.
 */
fun ByteArray.randomize(): ByteArray {
    Random.nextBytes(this)
    return this
}

/**
 * Randomize data on close.
 * @see randomize
 */
inline fun <T> ByteArray.use(block: (ByteArray) -> T): T = try {
    block(this)
} finally {
    this.randomize()
}
