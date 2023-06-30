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
 * Created by Lunabee Studio / Date - 6/13/2023 - for the oneSafe6 SDK.
 * Last modified 6/13/23, 3:20 PM
 */

package studio.lunabee.onesafe.messaging.domain.extension

import com.google.protobuf.Timestamp
import com.google.protobuf.TimestampKt
import java.time.Instant

fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(
    this.seconds,
    this.nanos.toLong(),
)

fun TimestampKt.Dsl.now() {
    val now = Instant.now()
    seconds = now.epochSecond
    nanos = now.nano
}
