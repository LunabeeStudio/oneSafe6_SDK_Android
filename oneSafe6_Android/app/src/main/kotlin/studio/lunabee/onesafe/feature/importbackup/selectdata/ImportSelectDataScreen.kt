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
 * Created by Lunabee Studio / Date - 8/27/2024 - for the oneSafe6 SDK.
 * Last modified 27/08/2024 11:08
 */

package studio.lunabee.onesafe.feature.importbackup.selectdata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSSwitchRow
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSTopImageLoadingCard
import studio.lunabee.onesafe.organism.card.param.OSCardImageParam
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun ImportSelectDataRoute(
    navScope: ImportSelectDataNavScope,
    viewModel: ImportSelectDataViewModel = hiltViewModel(),
) {
    val isBubblesChecked by viewModel.isBubblesImported.collectAsStateWithLifecycle()
    val isItemsChecked by viewModel.isItemsImported.collectAsStateWithLifecycle()
    ImportSelectDataScreen(
        navigateBack = navScope.navigateBack,
        onItemsToggle = viewModel::setImportItems,
        onBubblesToggle = viewModel::setImportBubbles,
        isBubblesChecked = isBubblesChecked,
        isItemChecked = isItemsChecked,
        onContinueClick = {
            viewModel.setImportData()
            navScope.continueImport(isBubblesChecked)
        },
        itemsToImport = viewModel.numberOfItemsToImport,
    )
}

@Composable
fun ImportSelectDataScreen(
    navigateBack: () -> Unit,
    isBubblesChecked: Boolean,
    isItemChecked: Boolean,
    onBubblesToggle: (Boolean) -> Unit,
    onItemsToggle: (Boolean) -> Unit,
    onContinueClick: () -> Unit,
    itemsToImport: Int,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ImportSaveDataScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        val scrollState = rememberScrollState()
        val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)
        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(top = OSDimens.ItemTopBar.Height)
                .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            OSTopImageLoadingCard(
                title = LbcTextSpec.StringResource(OSString.import_selectData_title),
                description = null,
                cardProgress = null,
                cardImage = OSCardImageParam(
                    imageRes = OSDrawable.character_jamy_cool,
                    offset = OSDimens.Card.DefaultImageCardOffset,
                ),
                action = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        OSSwitchRow(
                            checked = isItemChecked,
                            onCheckedChange = onItemsToggle,
                            label = LbcTextSpec.StringResource(OSString.import_selectData_items_label),
                            description = LbcTextSpec.PluralsResource(
                                OSPlurals.import_selectData_items_description,
                                itemsToImport,
                                itemsToImport,
                            ),
                            enabled = itemsToImport != 0,
                            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                        )
                        OSRegularSpacer()
                        OSSwitchRow(
                            checked = isBubblesChecked,
                            onCheckedChange = onBubblesToggle,
                            label = LbcTextSpec.StringResource(OSString.import_selectData_bubbles_label),
                            description = LbcTextSpec.StringResource(OSString.import_selectData_bubbles_description),
                            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                        )
                    }
                },
                modifier = Modifier,
            )
            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.import_selectData_confirmAction),
                onClick = onContinueClick,
                modifier = Modifier.align(Alignment.End),
                state = if (isBubblesChecked || isItemChecked) {
                    OSActionState.Enabled
                } else {
                    OSActionState.Disabled
                },
            )
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                options = listOf(topAppBarOptionNavBack(navigateBack)),
            )
        }
    }
}

@Preview
@Composable
private fun ImportSelectDataPreview() {
    OSTheme {
        ImportSelectDataScreen(
            navigateBack = {},
            isBubblesChecked = true,
            isItemChecked = true,
            onBubblesToggle = {},
            onItemsToggle = {},
            onContinueClick = {},
            itemsToImport = 10,
        )
    }
}
