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
 * Created by Lunabee Studio / Date - 7/12/2023 - for the oneSafe6 SDK.
 * Last modified 12/07/2023 10:40
 */

package studio.lunabee.onesafe.bubbles.ui.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.navigation.OSDestination
import java.util.UUID

object BubblesDestination : OSDestination {
    override val route: String = "bubbles_home"
}

@Suppress("LongParameterList")
fun NavGraphBuilder.bubblesHomeGraph(
    navigateBack: () -> Unit,
    navigateToContact: (contactId: UUID) -> Unit,
    navigateToConversation: (contactId: UUID) -> Unit,
    navigateToQrScan: () -> Unit,
    navigateToCreateContact: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToDecryptMessage: () -> Unit,
    createMessagingViewModel: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(
        route = BubblesDestination.route,
    ) { backStackEntry ->
        createMessagingViewModel(backStackEntry)
        BubblesAppRoute(
            navigateBack = navigateBack,
            navigateToContact = navigateToContact,
            navigateToConversation = navigateToConversation,
            navigateToCreateContact = navigateToCreateContact,
            navigateToQrScan = navigateToQrScan,
            navigateToSettings = navigateToSettings,
            navigateToDecryptMessage = navigateToDecryptMessage,
        )
    }
}
