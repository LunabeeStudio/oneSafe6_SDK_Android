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
 * Created by Lunabee Studio / Date - 7/24/2023 - for the oneSafe6 SDK.
 * Last modified 24/07/2023 13:19
 */

package studio.lunabee.onesafe.bubbles.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSExtraLargeSpacer
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.action.TopAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.rtl
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

context(OnBoardingBubblesNavScope)
@Composable
fun OnBoardingBubblesRoute(
    viewModel: OnBoardingBubblesViewModel = hiltViewModel(),
) {
    OnBoardingBubblesScreen(
        onBackClick = navigateBack,
        onStartClick = {
            viewModel.setHasDoneOnBoarding()
            navigateOnBoardingToBubblesHome()
        },
    )
}

@Composable
fun OnBoardingBubblesScreen(
    onBackClick: () -> Unit,
    onStartClick: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.OnBoardingBubblesScreen,
        background = LocalDesignSystem.current.bubblesBackGround(),
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            OSTopAppBar(
                options = listOf(
                    TopAppBarOptionNavBack(
                        onBackClick,
                        true,
                    ),
                ),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(OSDimens.SystemSpacing.Regular),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.illustration_bubbles_onboarding),
                    contentDescription = null,
                    modifier = Modifier
                        .rtl(LocalLayoutDirection.current)
                        .fillMaxWidth(0.75f)
                        .padding(bottom = OSDimens.SystemSpacing.Regular),
                    contentScale = ContentScale.FillWidth,
                )

                OSText(
                    text = LbcTextSpec.StringResource(R.string.bubbles_welcomeScreen_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                OSRegularSpacer()
                OSText(
                    text = LbcTextSpec.StringResource(R.string.bubbles_welcomeScreen_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                OSExtraLargeSpacer()
                OSFilledButton(
                    text = LbcTextSpec.StringResource(R.string.bubbles_welcomeScreen_startButton),
                    onClick = onStartClick,
                )
            }
        }
    }
}

interface OnBoardingBubblesNavScope {
    val navigateBack: () -> Unit
    val navigateOnBoardingToBubblesHome: () -> Unit
}

@OsDefaultPreview
@Composable
private fun OnBoardingBubblesScreenPreview() {
    OSPreviewBackgroundTheme {
        OnBoardingBubblesScreen(
            onBackClick = {},
            onStartClick = {},
        )
    }
}
