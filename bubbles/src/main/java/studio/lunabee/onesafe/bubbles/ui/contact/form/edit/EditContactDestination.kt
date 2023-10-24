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
 * Created by Lunabee Studio / Date - 8/22/2023 - for the oneSafe6 SDK.
 * Last modified 22/08/2023 10:03
 */

package studio.lunabee.onesafe.bubbles.ui.contact.form.edit

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.commonui.OSDestination
import java.util.UUID

object EditContactDestination : OSDestination {
    const val ContactIdArgs: String = "contactId"
    override val route: String = "edit_contact/{$ContactIdArgs}"

    fun getRoute(
        contactId: UUID,
    ): String = route.replace("{$ContactIdArgs}", contactId.toString())
}

context(EditContactNavScope)
fun NavGraphBuilder.editContactScreen() {
    composable(
        route = EditContactDestination.route,
        arguments = listOf(
            navArgument(EditContactDestination.ContactIdArgs) {
                type = NavType.StringType
            },
        ),
    ) {
        EditContactRoute()
    }
}
