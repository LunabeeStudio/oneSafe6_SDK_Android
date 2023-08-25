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
 * Created by Lunabee Studio / Date - 7/13/2023 - for the oneSafe6 SDK.
 * Last modified 13/07/2023 11:03
 */

package studio.lunabee.onesafe.bubbles.ui.contact.fromscratch

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.navigation.OSDestination
import java.util.UUID

object CreateContactFromScratchDestination : OSDestination {
    override val route: String = "create_contact"
}

fun NavGraphBuilder.createContactFromScratchDestination(
    navigateBack: () -> Unit,
    navigateToInvitationScreen: (UUID) -> Unit,
) {
    composable(
        route = CreateContactFromScratchDestination.route,
    ) {
        CreateContactFromScratchRoute(
            navigateBack = navigateBack,
            navigateToInvitationScreen = navigateToInvitationScreen,
        )
    }
}