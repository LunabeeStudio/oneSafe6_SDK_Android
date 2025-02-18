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
 * Created by Lunabee Studio / Date - 12/13/2024 - for the oneSafe6 SDK.
 * Last modified 12/13/24, 12:15 PM
 */

package studio.lunabee.onesafe.organism.card.component

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import studio.lunabee.onesafe.coreui.R

sealed interface OSCardGlobalAction {

    val onClick: () -> Unit

    val icon: (@Composable (modifier: Modifier) -> Unit)?

    class Default(override val onClick: () -> Unit) : OSCardGlobalAction {
        override val icon: (@Composable (modifier: Modifier) -> Unit)? = null
    }

    class Navigation(override val onClick: () -> Unit) : OSCardGlobalAction {
        override val icon: (@Composable (modifier: Modifier) -> Unit) = { modifier ->
            Icon(
                modifier = modifier,
                painter = painterResource(id = R.drawable.os_ic_navigate_next),
                contentDescription = null,
            )
        }
    }
}
