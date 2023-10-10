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
 * Created by Lunabee Studio / Date - 10/4/2023 - for the oneSafe6 SDK.
 * Last modified 10/4/23, 1:59 PM
 */

package studio.lunabee.onesafe.importexport.utils

import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale
import kotlin.test.assertEquals

class BackupDateTimeLocaleFormatterTest {
    private val nowClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)
    private val backupDateTimeLocaleFormatter: BackupDateTimeLocaleFormatter = BackupDateTimeLocaleFormatter(
        Locale.US,
        nowClock,
    )

    private val instantNow = Instant.now(nowClock)

    @Test
    fun format_today_test() {
        val today = LocalDateTime.ofInstant(instantNow, ZoneOffset.UTC).plusHours(6)
        val actual = backupDateTimeLocaleFormatter.format(today)
        val expected = "Today, 6:00 AM"
        assertEquals(expected, actual)
    }

    @Test
    fun format_yesterday_test() {
        val today = LocalDateTime.ofInstant(instantNow, ZoneOffset.UTC).minusDays(1).plusHours(18)
        val actual = backupDateTimeLocaleFormatter.format(today)
        val expected = "Yesterday, 6:00 PM"
        assertEquals(expected, actual)
    }

    @Test
    fun format_other_test() {
        val today = LocalDateTime.ofInstant(instantNow, ZoneOffset.UTC).minusDays(1).minusHours(1)
        val actual = backupDateTimeLocaleFormatter.format(today)
        val expected = "December 30, 1969, 11:00 PM"
        assertEquals(expected, actual)
    }
}
