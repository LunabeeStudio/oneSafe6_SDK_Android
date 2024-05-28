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
 * Created by Lunabee Studio / Date - 2/26/2024 - for the oneSafe6 SDK.
 * Last modified 2/26/24, 3:05 PM
 */

package studio.lunabee.onesafe.test

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import java.io.File

class TestConfigTest {

    @Test
    fun export_test_config() {
        // export test config
        val file = File("test_config.json")
        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }
        val configJson = json.encodeToString(OSTestConfig.config)
        println(configJson)
        file.writeText(configJson)
    }
}
