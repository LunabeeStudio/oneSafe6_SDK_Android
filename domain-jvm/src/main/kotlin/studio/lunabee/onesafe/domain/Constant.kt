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

package studio.lunabee.onesafe.domain

import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

object Constant {
    const val DefinitiveItemRemoveAfterDays: Int = 30
    const val MinimumCharForSearch: Int = 2
    val DelayBeforeSearch: Duration = 500.milliseconds
    const val FileTypeExtSeparator: Char = '|'
    val ForbiddenCharacterFileName: Regex
        get() = "[:\\\\/*?|<>']".toRegex()
    const val FileMaxSizeMegaBytes: Int = 50
    const val FileMaxSizeBytes: Int = FileMaxSizeMegaBytes * 1024 * 1024
    const val IndeterminateProgress: Float = -1f
    val ThumbnailPlaceHolderName: UUID = UUID.fromString("2ae4e851-508b-456c-93b6-dd4236d6b6a1")
    val DelayBeforeShowingCtaState: Long = 30.days.inWholeDays
    private val DelayBeforeFirstShowPreventionWarningCtaState: Long = 10.days.inWholeDays
    val InitialDelay: Long = DelayBeforeShowingCtaState - DelayBeforeFirstShowPreventionWarningCtaState
    val PreventionWarningBackupAge: Duration = 30.days
    val DelayUnitPreventionWarningCtaState: ChronoUnit = ChronoUnit.DAYS
}
