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
 * Created by Lunabee Studio / Date - 3/20/2024 - for the oneSafe6 SDK.
 * Last modified 3/20/24, 11:37 AM
 */

package studio.lunabee.onesafe.domain.model.crypto

import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertContentEquals

class DatabaseKeyTest {

    private val databaseKey = DatabaseKey(Random.Default.nextBytes(DatabaseKey.DatabaseKeyByteSize))

    @Test
    fun asCharArray_test() {
        val actual = databaseKey.asCharArray()
        val expected = databaseKey.raw.joinToString("") { "%02X".format(it) }.toCharArray()
        assertContentEquals(expected, actual)
    }
}
