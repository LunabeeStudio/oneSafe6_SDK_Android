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
 * Created by Lunabee Studio / Date - 7/6/2023 - for the oneSafe6 SDK.
 * Last modified 7/6/23, 4:58 PM
 */

package studio.lunabee.onesafe.commonui.notification

enum class OSNotificationChannelId {
    MESSAGING_CHANNEL_ID,
    BACKUP_CHANNEL_ID,
}

internal val OSNotificationChannelId.id: String
    get() = when (this) {
        OSNotificationChannelId.MESSAGING_CHANNEL_ID -> "988ee200-a89e-429c-9394-76961dd22d61"
        OSNotificationChannelId.BACKUP_CHANNEL_ID -> "cd1c3caf-46de-4e6a-85eb-debac4e349b3"
    }
