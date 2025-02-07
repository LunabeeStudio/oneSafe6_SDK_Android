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
 * Created by Lunabee Studio / Date - 9/11/2024 - for the oneSafe6 SDK.
 * Last modified 11/09/2024 14:32
 */

package studio.lunabee.onesafe.feature.settings.autodestruction.onboarding.model

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

data class AutoDestructionOnBoardingStrings(
    val description: LbcTextSpec,
    val buttonText: LbcTextSpec,
) {
    companion object {
        val Disabled: AutoDestructionOnBoardingStrings = AutoDestructionOnBoardingStrings(
            description = LbcTextSpec.StringResource(OSString.autodestruction_onBoarding_disabled_description),
            buttonText = LbcTextSpec.StringResource(OSString.autodestruction_onBoarding_disabled_action),
        )
        val Enabled: AutoDestructionOnBoardingStrings = AutoDestructionOnBoardingStrings(
            description = LbcTextSpec.StringResource(OSString.autodestruction_onBoarding_enabled_description),
            buttonText = LbcTextSpec.StringResource(OSString.autodestruction_onBoarding_enabled_action),
        )
    }
}
