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
 * Created by Lunabee Studio / Date - 1/15/2024 - for the oneSafe6 SDK.
 * Last modified 1/15/24, 4:06 PM
 */

package studio.lunabee.onesafe.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import studio.lunabee.onesafe.test.assertDoesNotThrow

@OptIn(ExperimentalSerializationApi::class)
class LocalCtaStateTest {

    // Fail if serialization change (ie class renamed)
    @Test
    fun deserialize_test() {
        // Generated with ProtoBuf.encodeToHexString(LocalCtaState.serializer(), xxx)
        val hiddenEncoded = "0a144c6f63616c43746153746174652e48696464656e1200"
        val visibleSince123Encoded = "0a1a4c6f63616c43746153746174652e56697369626c6553696e6365107b"
        val dismissedAt456Encoded = "0a194c6f63616c43746153746174652e4469736d6973736564417410c803"

        // expected
        val hidden = LocalCtaState.Hidden
        val visibleSince123 = LocalCtaState.VisibleSince(123)
        val dismissedAt456 = LocalCtaState.DismissedAt(456)

        assertDoesNotThrow {
            val hiddenDecoded = ProtoBuf.decodeFromHexString<LocalCtaState>(hiddenEncoded)
            assertEquals(hidden, hiddenDecoded)

            val visibleSince123Decoded = ProtoBuf.decodeFromHexString<LocalCtaState>(visibleSince123Encoded)
            assertEquals(visibleSince123, visibleSince123Decoded)

            val dismissedAt456Decoded = ProtoBuf.decodeFromHexString<LocalCtaState>(dismissedAt456Encoded)
            assertEquals(dismissedAt456, dismissedAt456Decoded)
        }
    }

    @Test
    fun serialize_map_test() {
        val expected = "0a200a046374613112180a144c6f63616c43746153746174652e48696464656e12000a270a0463746132121f0a1a4c6f63616c43746153746" +
            "174652e56697369626c6553696e636510d209"
        val expectedMap = mapOf(
            "cta1" to LocalCtaState.Hidden,
            "cta2" to LocalCtaState.VisibleSince(1234L),
        )
        val actual: String = ProtoBuf.encodeToHexString(
            LocalCtaStateMap(
                expectedMap,
            ),
        )
        assertEquals(expected, actual)
        val actualMap = ProtoBuf.decodeFromHexString<LocalCtaStateMap>(actual)
        assertEquals(expectedMap.keys, actualMap.data.keys)
        assertContentEquals(expectedMap.values, actualMap.data.values)
    }
}
