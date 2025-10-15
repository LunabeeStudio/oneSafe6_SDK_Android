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
 * Created by Lunabee Studio / Date - 1/12/2024 - for the oneSafe6 SDK.
 * Last modified 1/12/24, 3:37 PM
 */

package studio.lunabee.onesafe.importexport.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.home.HomeInfoData
import studio.lunabee.onesafe.commonui.home.HomeInfoDataNavScope
import studio.lunabee.onesafe.commonui.home.HomeInfoType
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.organism.card.OSMessageCardAttributes
import studio.lunabee.onesafe.organism.card.OSMessageCardStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.time.Instant

class AutoBackupEnableCtaHomeInfoData(
    visibleSince: Instant,
    private val onDismiss: () -> Unit,
) : HomeInfoData(
        type = HomeInfoType.Standard,
        key = KeyAutoBackupEnableCtaCard,
        contentType = ContentTypeAutoBackupEnableCtaCard,
        visibleSince = visibleSince,
    ) {
    @Composable
    context(HomeInfoDataNavScope)
    override fun Composable(modifier: Modifier) {
        OSMessageCard(
            title = LbcTextSpec.StringResource(OSString.home_autoBackupCard_title),
            description = LbcTextSpec.StringResource(OSString.home_autoBackupCard_message),
            attributes = OSMessageCardAttributes()
                .dismissible(
                    icon = OSImageSpec.Drawable(OSDrawable.ic_baseline_close),
                    contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_dismissCta),
                    onDismiss = onDismiss,
                ).style(OSMessageCardStyle.Default),
            modifier = modifier
                .testTag(UiConstants.TestTag.Item.AutoBackupEnableCtaCard),
            action = {
                OSTextButton(
                    text = LbcTextSpec.StringResource(OSString.home_autoBackupCard_button),
                    onClick = navigateFromHomeInfoDataToBackupSettings,
                    modifier = Modifier.padding(bottom = OSDimens.SystemSpacing.Small),
                )
            },
        )
    }
}

@OsDefaultPreview
@Composable
private fun AutoBackupEnableCtaCardPreview() {
    OSPreviewBackgroundTheme {
        with(object : HomeInfoDataNavScope {
            override val navigateFromHomeInfoDataToBackupSettings: () -> Unit = {}
            override val navigateFromHomeInfoDataToBubblesOnBoarding: () -> Unit = {}
        }) {
            AutoBackupEnableCtaHomeInfoData(
                visibleSince = Instant.EPOCH,
                onDismiss = {},
            ).Composable(Modifier)
        }
    }
}

private const val KeyAutoBackupEnableCtaCard: String = "KeyAutoBackupEnableCtaCard"
private const val ContentTypeAutoBackupEnableCtaCard: String = "ContentTypeAutoBackupEnableCtaCard"
