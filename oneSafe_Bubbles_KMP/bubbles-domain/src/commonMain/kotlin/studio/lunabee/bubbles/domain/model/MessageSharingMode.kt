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
 * Created by Lunabee Studio / Date - 6/12/2024 - for the oneSafe6 SDK.
 * Last modified 6/12/24, 2:35 PM
 */

package studio.lunabee.bubbles.domain.model

enum class MessageSharingMode(val id: String) {
    Deeplink(DeeplinkId),
    CypherText(CypherTextId),
    Archive(ArchiveId),
    ;

    companion object {
        fun fromString(raw: String): MessageSharingMode {
            return when (raw) {
                DeeplinkId -> Deeplink
                CypherTextId -> CypherText
                ArchiveId -> Archive
                else -> CypherText
            }
        }
    }
}

private const val DeeplinkId: String = "deeplink"
private const val CypherTextId: String = "cypherText"
private const val ArchiveId: String = "archive"
