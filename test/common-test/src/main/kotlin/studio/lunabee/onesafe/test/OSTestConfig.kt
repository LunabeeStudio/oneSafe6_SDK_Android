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
 * Created by Lunabee Studio / Date - 2/21/2024 - for the oneSafe6 SDK.
 * Last modified 2/21/24, 11:30 AM
 */

package studio.lunabee.onesafe.test

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.threeten.extra.MutableClock
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.random.Random

object OSTestConfig {

    internal val config = Config()

    val random: Random
        get() = config.random
    val itemLayouts: ItemLayout
        get() = config.itemLayouts
    val cameraSystem: CameraSystem
        get() = config.cameraSystem
    val clock: MutableClock = MutableClock.epochUTC()
    val extraSafeIds: List<SafeId>
        get() = config.extraSafeIds

    @Serializable
    internal class Config {

        // ⚠️ Replaced by CI, see ci/set_test_seed.py
        private val seed = Random.nextInt()

        @kotlinx.serialization.Transient
        val random: Random = Random(seed)

        val itemLayouts: ItemLayout = ItemLayout.entries[
            Math.floorMod(
                seed,
                ItemLayout.entries.size,
            ),
        ]

        // FIXME <flaky> CameraSystem.InApp lead to compose test timeout
        //        val cameraSystem: CameraSystem = CameraSystem.entries[Math.floorMod(seed, CameraSystem.entries.size)]
        val cameraSystem: CameraSystem = CameraSystem.External

        val extraSafeIds: List<
            @Serializable(with = SafeIdAsStringSerializer::class)
            SafeId,
            > = List(random.nextInt(0, 3)) {
            val randomBytes = random.nextBytes(16)
            val buffer = ByteBuffer.wrap(randomBytes)
            SafeId(buildUUID(randomBytes, buffer))
        }
    }
}

object SafeIdAsStringSerializer : KSerializer<SafeId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SafeId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SafeId) {
        val string = value.toString()
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): SafeId {
        val string = decoder.decodeString()
        return SafeId(UUID.fromString(string))
    }
}
