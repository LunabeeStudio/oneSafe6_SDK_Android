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
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

class BackupDateTimeLocaleFormatterTest {
    private val zone = ZoneOffset.UTC
    private val nowClock = Clock.fixed(Instant.EPOCH, zone)
    private val backupDateTimeLocaleFormatter: BackupDateTimeLocaleFormatter = BackupDateTimeLocaleFormatter(
        Locale.US,
        nowClock,
        zone,
    )

    private val instantNow = Instant.now(nowClock)

    @Test
    fun format_today_test() {
        val today = instantNow.plus(6.hours.toJavaDuration())
        val actual = backupDateTimeLocaleFormatter.format(today)
        val expected = "Today, 6:00 AM"
        assertEquals(expected, actual)
    }

    @Test
    fun format_yesterday_test() {
        val today = instantNow.minus(1.days.toJavaDuration()).plus(18.hours.toJavaDuration())
        val actual = backupDateTimeLocaleFormatter.format(today)
        val expected = "Yesterday, 6:00 PM"
        assertEquals(expected, actual)
    }

    @Test
    fun format_other_test() {
        val today = instantNow.minus(1.days.toJavaDuration()).minus(1.hours.toJavaDuration())
        val actual = backupDateTimeLocaleFormatter.format(today)
        val expected = "December 30, 1969, 11:00 PM"
        assertEquals(expected, actual)
    }

    /**
     * Create now var at 6:00 AM UTC+2 and format it UTC zone (expected 4:00 AM)
     */
    @Test
    fun format_today_cross_zone_test() {
        val zoneOffset = ZoneOffset.ofHours(2) // Zone UTC+2
        val zonedDateTime = ZonedDateTime.of(1970, 1, 1, 6, 0, 0, 0, zoneOffset)

        val now = zonedDateTime.toInstant()
        val actual = backupDateTimeLocaleFormatter.format(now)
        val expected = "Today, 4:00 AM"
        assertEquals(expected, actual)
    }
}
