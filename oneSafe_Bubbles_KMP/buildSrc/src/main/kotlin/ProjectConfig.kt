/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 */

import org.gradle.api.JavaVersion

object ProjectConfig {
    const val LIBRARY_URL: String = "https://github.com/LunabeeStudio/oneSafe_Bubbles_KMP"
    const val GROUP_ID: String = "studio.lunabee.bubbles"
    const val COMPILE_SDK: Int = 34
    const val MIN_SDK: Int = 21

    val JDK_VERSION: JavaVersion = JavaVersion.VERSION_17
}
