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
 * Created by Lunabee Studio / Date - 9/26/2024 - for the oneSafe6 SDK.
 * Last modified 26/09/2024 17:22
 */

package studio.lunabee.onesafe.feature.settings.panicwidget

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.feature.settings.panicwidget.model.PanicWidgetSettingsStrings
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun PanicWidgetSettingsRoute(
    navScope: PanicWidgetSettingsNavScope,
    viewModel: PanicWidgetSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings by remember(uiState) {
        mutableStateOf(PanicWidgetSettingsStrings.fromPanicWidgetUiState(uiState))
    }

    if (uiState.isExit) {
        LaunchedEffect(Unit) {
            navScope.navigateBack()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.updatePanicEnabledWidgetState()
    }

    PanicWidgetSettingsScreen(
        isAutoBackupEnabled = uiState.isAutoBackupEnabled,
        strings = strings,
        navigateBack = navScope.navigateBack,
        onActionClick = {
            when {
                uiState.isWidgetEnabled && uiState.isPanicDestructionEnabled -> viewModel.togglePanicDestruction(false)
                uiState.isWidgetEnabled -> viewModel.togglePanicDestruction(true)
                else -> viewModel.pinWidgetToHomeScreen()
            }
        },
    )
}

@Composable
fun PanicWidgetSettingsScreen(
    isAutoBackupEnabled: Boolean,
    strings: PanicWidgetSettingsStrings,
    navigateBack: () -> Unit,
    onActionClick: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    OSScreen(
        testTag = UiConstants.TestTag.Screen.WidgetPanicModeSettingsScreen,
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
                OSMessageCard(
                    description = strings.description,
                    action = null,
                    modifier = Modifier.accessibilityMergeDescendants(),
                )
            }
            if (!isAutoBackupEnabled) {
                item {
                    OSMessageCard(
                        description = LbcTextSpec.StringResource(OSString.panicdestruction_settings_warning).markdown(),
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
