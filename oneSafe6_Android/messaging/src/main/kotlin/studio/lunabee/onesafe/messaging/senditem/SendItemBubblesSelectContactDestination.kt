/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/7/2024 - for the oneSafe6 SDK.
 * Last modified 06/08/2024 16:42
 */

package studio.lunabee.onesafe.messaging.senditem

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.OSDestination
import java.util.UUID

object SendItemBubblesSelectContactDestination : OSDestination {
    const val ItemToShareIdArgument: String = "itemToShare"
    const val IncludeChildrenArgument: String = "includeChildren"

    override val route: String = "send_item_bubbles_select_contact/$ItemToShareIdArgument={$ItemToShareIdArgument}/" +
        "$IncludeChildrenArgument={$IncludeChildrenArgument}"

    fun getRoute(itemId: UUID, includeChildren: Boolean): String =
        route
            .replace("{$ItemToShareIdArgument}", itemId.toString())
            .replace("{$IncludeChildrenArgument}", includeChildren.toString())
}

context(SendItemBubblesSelectContactNavScope)
fun NavGraphBuilder.sendItemBubblesSelectContactScreen() {
    composable(
        route = SendItemBubblesSelectContactDestination.route,
        arguments = listOf(
            navArgument(SendItemBubblesSelectContactDestination.ItemToShareIdArgument) { type = NavType.StringType },
            navArgument(SendItemBubblesSelectContactDestination.IncludeChildrenArgument) { type = NavType.BoolType },
        ),
    ) {
        SendItemBubblesSelectContactRoute()
    }
}

interface SendItemBubblesSelectContactNavScope {
    val navigateBack: () -> Unit
    val sendItemBubblesNavigateToBubblesHome: () -> Unit
    val showSnackbar: (visuals: SnackbarVisuals) -> Unit
    val navigateToWriteMessage: (contactId: UUID) -> Unit
}
