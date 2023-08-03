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
 * Created by Lunabee Studio / Date - 7/17/2023 - for the oneSafe6 SDK.
 * Last modified 17/07/2023 11:15
 */

package studio.lunabee.onesafe.bubbles.ui.invitationresponse

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.navigation.OSDestination
import java.util.UUID

object InvitationResponseDestination : OSDestination {
    const val ContactIdArgs: String = "contactId"
    override val route: String = "response_invitation/{$ContactIdArgs}"

    fun getRoute(
        contactId: UUID,
    ): String = route.replace("{$ContactIdArgs}", contactId.toString())
}

fun NavGraphBuilder.invitationResponseGraph(
    navigateBack: () -> Unit,
    navigateToConversation: (UUID) -> Unit,
) {
    composable(
        route = InvitationResponseDestination.route,
        arguments = listOf(
            navArgument(InvitationResponseDestination.ContactIdArgs) {
                type = NavType.StringType
            },
        ),
    ) {
        InvitationResponseRoute(
            navigateBack = navigateBack,
            navigateToConversation = navigateToConversation,
        )
    }
}
