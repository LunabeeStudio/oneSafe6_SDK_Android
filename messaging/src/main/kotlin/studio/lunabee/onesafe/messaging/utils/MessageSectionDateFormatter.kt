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
 *
 * Created by Lunabee Studio / Date - 6/23/2023 - for the oneSafe6 SDK.
 * Last modified 6/20/23, 11:13 AM
 */

package studio.lunabee.onesafe.messaging.utils

import android.content.Context
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.messaging.extension.isSameDayAs
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Format the instant into a human readable date format.
 * If the instant is the same year as today, the result will not include the year (ex: June 14).
 * If it's not on the same year it will include the date (ex: June 14 2022)
 */
class MessageSectionDateFormatter(
    private val context: Context,
) {
    operator fun invoke(date: Instant, now: Instant = Instant.now()): LbcTextSpec {
        val locale = Locale(context.getString(OSString.locale_lang))
        return when {
            date.isSameDayAs(now) -> LbcTextSpec.StringResource(OSString.oneSafeK_messageDate_today)
            now.atZone(ZoneId.systemDefault()).year == date.atZone(ZoneId.systemDefault()).year -> {
                val pattern = context.getString(OSString.oneSafeK_messageDate_pattern)
                LbcTextSpec.Raw(
                    date
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern(pattern, locale)),
                )
            }
            else -> LbcTextSpec.Raw(
                date.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)),
            )
        }
    }
}
