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
 * Created by Lunabee Studio / Date - 1/29/2024 - for the oneSafe6 SDK.
 * Last modified 1/29/24, 8:53 AM
 */

package studio.lunabee.onesafe.test

import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import java.util.UUID
import kotlin.random.Random

object CommonTestUtils {
    fun createItemFieldData(
        id: UUID = UUID.randomUUID(),
        name: String? = UUID.randomUUID().toString(),
        kind: SafeItemFieldKind? = SafeItemFieldKind.Text,
        position: Double = Random.nextDouble(),
        placeholder: String? = UUID.randomUUID().toString(),
        value: String? = UUID.randomUUID().toString(),
        showPrediction: Boolean = Random.nextBoolean(),
        isItemIdentifier: Boolean = false,
        formattingMask: String? = null,
        secureDisplayMask: String? = null,
        isSecured: Boolean = false,
    ): ItemFieldData = ItemFieldData(
        id = id,
        name = name,
        position = position,
        placeholder = placeholder,
        value = value,
        kind = kind,
        showPrediction = showPrediction,
        isItemIdentifier = isItemIdentifier,
        formattingMask = formattingMask,
        secureDisplayMask = secureDisplayMask,
        isSecured = isSecured,
    )
}
