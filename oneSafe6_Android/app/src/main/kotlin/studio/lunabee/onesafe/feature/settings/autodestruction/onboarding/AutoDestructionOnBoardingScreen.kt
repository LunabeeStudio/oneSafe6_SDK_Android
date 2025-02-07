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
 * Created by Lunabee Studio / Date - 9/10/2024 - for the oneSafe6 SDK.
 * Last modified 10/09/2024 16:08
 */

package studio.lunabee.onesafe.feature.settings.autodestruction.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.feature.settings.autodestruction.onboarding.model.AutoDestructionOnBoardingStrings
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun AutoDestructionOnBoardingRoute(
    navScope: AutoDestructionOnBoardingNavScope,
    viewModel: AutoDestructionOnBoardingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings by remember(uiState.isAutoDestructionEnabled) {
        mutableStateOf(
            if (uiState.isAutoDestructionEnabled) {
                AutoDestructionOnBoardingStrings.Enabled
            } else {
                AutoDestructionOnBoardingStrings.Disabled
            },
        )
    }

    if (uiState.isExit) {
        LaunchedEffect(Unit) {
            navScope.navigateBack()
        }
    }

    AutoDestructionOnBoardingScreen(
        navigateBack = navScope.navigateBack,
        onActionClick = if (uiState.isAutoDestructionEnabled) {
            viewModel::disableAutoDestruction
        } else {
            navScope.navigateToPassword
        },
        strings = strings,
        isAutoBackupEnabled = uiState.isAutoBackupEnabled,
    )
}

@Composable
fun AutoDestructionOnBoardingScreen(
    navigateBack: () -> Unit,
    onActionClick: () -> Unit,
    strings: AutoDestructionOnBoardingStrings,
    isAutoBackupEnabled: Boolean,
) {
    val lazyListState = rememberLazyListState()
    OSScreen(
        testTag = UiConstants.TestTag.Screen.AutoDestructionOnBoardingScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height),
            contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Large),
        ) {
            item {
                OSTopImageBox(imageRes = OSDrawable.character_hello) {
                    OSMessageCard(
                        description = strings.description,
                        action = null,
                        modifier = Modifier.accessibilityMergeDescendants(),
                    )
                }
            }
            if (!isAutoBackupEnabled) {
                item {
                    OSMessageCard(
                        description = LbcTextSpec.StringResource(
                            OSString.autodestruction_onBoarding_warning,
                        ).markdown(),
                        action = null,
                        modifier = Modifier.accessibilityMergeDescendants(),
                    )
                }
            }
            item {
                Box(Modifier.fillMaxWidth()) {
                    OSFilledButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        text = strings.buttonText,
                        onClick = onActionClick,
                    )
                }
            }
        }
        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.autodestruction_onBoarding_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = lazyListState.topAppBarElevation,
        )
    }
}
