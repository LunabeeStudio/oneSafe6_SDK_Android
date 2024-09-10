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
 * Created by Lunabee Studio / Date - 7/22/2024 - for the oneSafe6 SDK.
 * Last modified 22/07/2024 09:31
 */

package studio.lunabee.messaging.domain.model.proto

import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class ProtoTimestamp(
    @ProtoNumber(1)
    val seconds: Long,
    @ProtoNumber(2)
    val nanos: Int,
) {

    fun toInstant(): Instant {
        return Instant.fromEpochSeconds(seconds, nanos)
    }

    companion object {
        fun fromInstant(instant: Instant): ProtoTimestamp {
            return ProtoTimestamp(
                seconds = instant.epochSeconds,
                nanos = instant.nanosecondsOfSecond,
            )
        }
    }
}
