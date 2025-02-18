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
 * Created by Lunabee Studio / Date - 7/19/2024 - for the oneSafe6 SDK.
 * Last modified 7/19/24, 5:04 PM
 */

package studio.lunabee.onesafe.feature.settings.libraries

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryColors
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun LibrariesRoute(
    navigateBack: () -> Unit,
) {
    LibrariesScreen(
        navigateBack = navigateBack,
    )
}

@Composable
fun LibrariesScreen(
    navigateBack: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.Libraries,
        background = LocalDesignSystem.current.simpleBackground(),
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column {
            ElevatedTopAppBar(
                title = LbcTextSpec.StringResource(OSString.librariesScreen_title),
                options = listOf(topAppBarOptionNavBack(navigateBack)),
                elevation = OSDimens.Elevation.TopAppBarElevation,
            )
            LibrariesContainer(
                colors = object : LibraryColors {
                    override val backgroundColor = LocalDesignSystem.current.getBackgroundGradientStartColor()
                    override val contentColor = MaterialTheme.colorScheme.onBackground
                    override val badgeBackgroundColor = MaterialTheme.colorScheme.primaryContainer
                    override val badgeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    override val dialogConfirmButtonColor = MaterialTheme.colorScheme.onBackground
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
fun LibrariesScreenPreview() {
    OSTheme {
        LibrariesScreen(
            navigateBack = {},
        )
    }
}
