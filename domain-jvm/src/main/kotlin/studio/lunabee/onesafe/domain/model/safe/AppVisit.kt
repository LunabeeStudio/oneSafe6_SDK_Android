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
 * Last modified 6/19/24, 2:31 PM
 */

package studio.lunabee.onesafe.domain.model.safe

class AppVisit(
    val hasVisitedLogin: Boolean,
    val hasFinishOneSafeKOnBoarding: Boolean,
    val hasDoneOnBoardingBubbles: Boolean,
    val hasDoneTutorialOpenOsk: Boolean,
    val hasDoneTutorialLockOsk: Boolean,
    val hasHiddenCameraTips: Boolean,
    val hasSeenItemEditionUrlToolTip: Boolean,
    val hasSeenItemEditionEmojiToolTip: Boolean,
    val hasSeenItemReadEditToolTip: Boolean,
    val hasSeenDialogMessageSaveConfirmation: Boolean,
) {
    constructor() : this(
        hasVisitedLogin = false,
        hasFinishOneSafeKOnBoarding = false,
        hasDoneOnBoardingBubbles = false,
        hasDoneTutorialOpenOsk = false,
        hasDoneTutorialLockOsk = false,
        hasHiddenCameraTips = false,
        hasSeenItemEditionUrlToolTip = false,
        hasSeenItemEditionEmojiToolTip = false,
        hasSeenItemReadEditToolTip = false,
        hasSeenDialogMessageSaveConfirmation = false,
    )
}
