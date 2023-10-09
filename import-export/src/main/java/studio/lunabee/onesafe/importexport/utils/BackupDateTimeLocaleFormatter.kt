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
 * Created by Lunabee Studio / Date - 10/3/2023 - for the oneSafe6 SDK.
 * Last modified 10/3/23, 7:05 PM
 */

package studio.lunabee.onesafe.importexport.utils

import android.icu.text.RelativeDateTimeFormatter
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

class BackupDateTimeLocaleFormatter(
    private val locale: Locale,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun format(date: LocalDateTime): String {
        val fmt: RelativeDateTimeFormatter = RelativeDateTimeFormatter.getInstance(locale)

        val daysUntilNow = date.toLocalDate().until(LocalDate.now(clock), ChronoUnit.DAYS)
        val dateStr = when (daysUntilNow) {
            0L -> fmt.format(RelativeDateTimeFormatter.Direction.THIS, RelativeDateTimeFormatter.AbsoluteUnit.DAY)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            1L ->
                fmt
                    .format(RelativeDateTimeFormatter.Direction.LAST, RelativeDateTimeFormatter.AbsoluteUnit.DAY)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            else -> DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale).format(date)
        }

        val timeStr = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale).format(date)

        return fmt.combineDateAndTime(dateStr, timeStr)
    }
}
