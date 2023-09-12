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
 * Created by Lunabee Studio / Date - 7/12/2023 - for the oneSafe6 SDK.
 * Last modified 12/07/2023 10:46
 */

package studio.lunabee.onesafe.bubbles.ui.contact.detail

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.navigation.OSDestination
import java.util.UUID

object ContactDetailDestination : OSDestination {
    const val ContactIdArgs: String = "contactId"
    override val route: String = "contact_detail/{$ContactIdArgs}"

    fun getRoute(
        contactId: UUID,
    ): String = route.replace("{$ContactIdArgs}", contactId.toString())
}

context(ContactDetailNavScope)
fun NavGraphBuilder.contactDetailScreen() {
    composable(
        route = ContactDetailDestination.route,
        arguments = listOf(
            navArgument(ContactDetailDestination.ContactIdArgs) {
                type = NavType.StringType
            },
        ),
    ) {
        ContactDetailRoute()
    }
}
