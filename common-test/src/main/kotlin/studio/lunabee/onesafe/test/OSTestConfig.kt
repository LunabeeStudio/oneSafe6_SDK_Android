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

import kotlinx.serialization.Serializable
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemsLayoutSettings
import kotlin.random.Random

object OSTestConfig {

    internal val config = Config()

    val random: Random
        get() = config.random
    val itemsLayoutSettings: ItemsLayoutSettings
        get() = config.itemsLayoutSettings
    val cameraSystem: CameraSystem
        get() = config.cameraSystem

    @Serializable
    internal class Config {

        // ⚠️ Replaced by CI, see ci/set_test_seed.py
        private val seed = Random.nextInt()

        @kotlinx.serialization.Transient
        val random: Random = Random(seed)

        val itemsLayoutSettings: ItemsLayoutSettings = ItemsLayoutSettings.entries[
            Math.floorMod(
                seed,
                ItemsLayoutSettings.entries.size,
            ),
        ]

        // FIXME <flaky> CameraSystem.InApp lead to compose test timeout
        //        val cameraSystem: CameraSystem = CameraSystem.entries[Math.floorMod(seed, CameraSystem.entries.size)]
        val cameraSystem: CameraSystem = CameraSystem.External
    }
}
