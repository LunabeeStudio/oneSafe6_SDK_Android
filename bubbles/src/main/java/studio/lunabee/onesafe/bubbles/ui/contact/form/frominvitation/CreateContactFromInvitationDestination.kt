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
 * Last modified 17/07/2023 10:31
 */

package studio.lunabee.onesafe.bubbles.ui.contact.form.frominvitation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.OSDestination

object CreateContactFromInvitationDestination : OSDestination {

    const val MessageString: String = "messageString"
    override val route: String = "create_contact_from_invitation/{$MessageString}/"

    fun getRoute(
        messageString: String,
    ): String = route.replace("{$MessageString}", Uri.encode(messageString))
}

context(CreateContactFromInvitationNavScope)
fun NavGraphBuilder.createContactFromInvitationScreen() {
    composable(
        route = CreateContactFromInvitationDestination.route,
    ) {
        CreateContactFromInvitationRoute()
    }
}
