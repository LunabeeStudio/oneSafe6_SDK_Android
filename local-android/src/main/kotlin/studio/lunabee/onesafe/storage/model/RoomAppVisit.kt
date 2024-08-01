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
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 2:29 PM
 */

package studio.lunabee.onesafe.storage.model

import androidx.room.ColumnInfo
import studio.lunabee.onesafe.domain.model.safe.AppVisit

class RoomAppVisit(
    @ColumnInfo(name = "has_finish_one_safe_k_on_boarding")
    val hasFinishOneSafeKOnBoarding: Boolean,
    @ColumnInfo(name = "has_done_on_boarding_bubbles")
    val hasDoneOnBoardingBubbles: Boolean,
    @ColumnInfo(name = "has_hidden_camera_tips")
    val hasHiddenCameraTips: Boolean,
    @ColumnInfo(name = "has_seen_item_edition_url_tool_tip")
    val hasSeenItemEditionUrlToolTip: Boolean,
    @ColumnInfo(name = "has_seen_item_edition_emoji_tool_tip")
    val hasSeenItemEditionEmojiToolTip: Boolean,
    @ColumnInfo(name = "has_seen_item_read_edit_tool_tip")
    val hasSeenItemReadEditToolTip: Boolean,
) {
    companion object {
        fun fromAppVisit(appVisit: AppVisit): RoomAppVisit = RoomAppVisit(
            hasFinishOneSafeKOnBoarding = appVisit.hasFinishOneSafeKOnBoarding,
            hasDoneOnBoardingBubbles = appVisit.hasDoneOnBoardingBubbles,
            hasHiddenCameraTips = appVisit.hasHiddenCameraTips,
            hasSeenItemEditionUrlToolTip = appVisit.hasSeenItemEditionUrlToolTip,
            hasSeenItemEditionEmojiToolTip = appVisit.hasSeenItemEditionEmojiToolTip,
            hasSeenItemReadEditToolTip = appVisit.hasSeenItemReadEditToolTip,
        )
    }
}
