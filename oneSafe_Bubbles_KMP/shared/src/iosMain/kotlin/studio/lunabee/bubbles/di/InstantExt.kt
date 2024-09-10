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
 */

package studio.lunabee.bubbles.di

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.usePinned
import kotlinx.datetime.Instant
import platform.Foundation.NSDate
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun Instant.toDate(): NSDate = usePinned {
    NSDate.create(timeIntervalSince1970 = this.epochSeconds.toDouble())
}

@OptIn(ExperimentalForeignApi::class)
fun NSDate.toInstant(): Instant = usePinned {
    Instant.fromEpochSeconds(this.timeIntervalSince1970().toLong())
}
