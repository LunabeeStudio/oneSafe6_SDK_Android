/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/12/23, 5:47 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.destination

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.extension.getTextSharingIntent
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageNavScope
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute
import java.util.UUID

object WriteMessageDestination {

    const val ContactIdArgs: String = "contactId"
    const val route: String = "write_message/{$ContactIdArgs}"

    fun getRoute(
        contactId: UUID,
    ): String = route.replace("{$ContactIdArgs}", contactId.toString())
}

context(WriteMessageNavScope)
fun NavGraphBuilder.writeMessageScreen(
    createMessagingViewModel: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(
        route = WriteMessageDestination.route,
        arguments = listOf(
            navArgument(WriteMessageDestination.ContactIdArgs) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->
        val context = LocalContext.current
        createMessagingViewModel(backStackEntry)
        WriteMessageRoute(
            onChangeRecipient = null,
            sendMessage = { encMessage ->
                val intent = context.getTextSharingIntent(encMessage)
                context.startActivity(intent)
            },
            contactIdFlow = backStackEntry.savedStateHandle.getStateFlow(WriteMessageDestination.ContactIdArgs, null),
            sendIcon = OSImageSpec.Drawable(R.drawable.ic_share),
            hideKeyboard = null,
        )
    }
}
