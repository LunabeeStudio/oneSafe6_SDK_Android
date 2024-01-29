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
 * Created by Lunabee Studio / Date - 1/26/2024 - for the oneSafe6 SDK.
 * Last modified 1/26/24, 4:18 PM
 */

package studio.lunabee.onesafe.test

import android.graphics.Color
import kotlin.random.Random

fun Random.colorInt(): Int {
    val red = (OSTestUtils.random.nextDouble(1.0) * 256).toInt()
    val green = (OSTestUtils.random.nextDouble(1.0) * 256).toInt()
    val blue = (OSTestUtils.random.nextDouble(1.0) * 256).toInt()
    return Color.argb(255, red, green, blue)
}
