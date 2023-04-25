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

package studio.lunabee.onesafe.domain.model.safeitem

import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.test.OSTestUtils
import java.time.Instant
import kotlin.test.assertEquals

class SafeItemTest {
    @Test
    fun daysBeforeRemove_test() {
        val safeItem = OSTestUtils.createSafeItem(deletedAt = Instant.ofEpochMilli(0))

        val msToDay: Long = 1000 * 60 * 60 * 24

        val actualLess1 = safeItem.daysBeforeRemove(Instant.ofEpochMilli(msToDay * 1 + 1))
        val actualLess0 = safeItem.daysBeforeRemove(Instant.ofEpochMilli(msToDay * 1 - 1))
        val actualEnd = safeItem.daysBeforeRemove(
            Instant.ofEpochMilli(msToDay * Constant.DefinitiveItemRemoveAfterDays.toLong() + 1),
        )

        assertEquals(Constant.DefinitiveItemRemoveAfterDays - 1, actualLess1)
        assertEquals(Constant.DefinitiveItemRemoveAfterDays, actualLess0)
        assertEquals(1, actualEnd)
    }
}
