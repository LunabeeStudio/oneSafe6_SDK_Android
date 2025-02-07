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
 * Last modified 11/09/2024 15:02
 */

package studio.lunabee.onesafe.feature.settings.autodestruction

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.settings.autodestruction.confirmpassword.AutoDestructionPasswordConfirmationDestination
import studio.lunabee.onesafe.feature.settings.autodestruction.confirmpassword.AutoDestructionPasswordConfirmationNavScope
import studio.lunabee.onesafe.feature.settings.autodestruction.confirmpassword.autoDestructionPasswordConfirmationGraph
import studio.lunabee.onesafe.feature.settings.autodestruction.onboarding.AutoDestructionOnBoardingNavScope
import studio.lunabee.onesafe.feature.settings.autodestruction.onboarding.autoDestructionOnBoardingGraph
import studio.lunabee.onesafe.feature.settings.autodestruction.password.AutoDestructionPasswordDestination
import studio.lunabee.onesafe.feature.settings.autodestruction.password.AutoDestructionPasswordNavScope
import studio.lunabee.onesafe.feature.settings.autodestruction.password.autoDestructionPasswordGraph
import studio.lunabee.onesafe.feature.settings.security.SecuritySettingDestination

fun NavGraphBuilder.autoDestructionSettingsGraph(
    mainNavController: NavHostController,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
) {
    autoDestructionOnBoardingGraph(
        navScope = object : AutoDestructionOnBoardingNavScope {
            override val navigateBack: () -> Unit = { mainNavController.popBackStack() }
            override val navigateToPassword: () -> Unit = { mainNavController.safeNavigate(AutoDestructionPasswordDestination.route) }
        },
    )
    autoDestructionPasswordGraph(
        navScope = object : AutoDestructionPasswordNavScope {
            override val navigateBack: () -> Unit = { mainNavController.popBackStack() }
            override val navigateToConfirm: (String, String) -> Unit = { passwordHash, salt ->
                mainNavController.safeNavigate(AutoDestructionPasswordConfirmationDestination.getRoute(passwordHash, salt))
            }
        },
    )
    autoDestructionPasswordConfirmationGraph(
        navScope = object : AutoDestructionPasswordConfirmationNavScope {
            override val navigateBack: () -> Unit = { mainNavController.popBackStack() }
            override val showSnackBar: (visuals: SnackbarVisuals) -> Unit = showSnackBar
            override val navigateBackToSettings: () -> Unit = {
                mainNavController.popBackStack(route = SecuritySettingDestination.route, inclusive = false)
            }
        },
    )
}
