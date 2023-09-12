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

package studio.lunabee.onesafe.bubbles.ui.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.commonui.navigation.OSDestination

object BubblesHomeDestination : OSDestination {
    const val BubblesHomeTabArg: String = "tab"

    override val route: String = "bubbles_home?" +
        "$BubblesHomeTabArg={$BubblesHomeTabArg}"

    fun getRoute(tab: BubblesHomeTab?): String = if (tab != null) {
        this.route.replace("{$BubblesHomeTabArg}", tab.name)
    } else {
        this.route
    }
}

context(BubblesHomeNavScope)
fun NavGraphBuilder.bubblesHomeScreen(
    createMessagingViewModel: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(
        route = BubblesHomeDestination.route,
    ) { backStackEntry ->
        createMessagingViewModel(backStackEntry)
        BubblesHomeRoute()
    }
}
