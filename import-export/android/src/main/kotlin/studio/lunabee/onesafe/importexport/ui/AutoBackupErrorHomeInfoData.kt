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
 * Created by Lunabee Studio / Date - 11/23/2023 - for the oneSafe6 SDK.
 * Last modified 11/23/23, 3:25 PM
 */

package studio.lunabee.onesafe.importexport.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.home.HomeInfoData
import studio.lunabee.onesafe.commonui.home.HomeInfoType
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.organism.card.OSMessageCardAttributes
import studio.lunabee.onesafe.organism.card.OSMessageCardStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

class AutoBackupErrorHomeInfoData(
    private val code: LbcTextSpec,
    private val onDismiss: () -> Unit,
) : HomeInfoData(
    type = HomeInfoType.Error,
    key = KeyAutoBackupErrorCard,
    contentType = ContentTypeAutoBackupErrorCard,
) {
    @Composable
    override fun Composable(modifier: Modifier) {
        val uriHandler = LocalUriHandler.current

        OSMessageCard(
            title = LbcTextSpec.StringResource(R.string.autoBackup_errorCard_title),
            description = LbcTextSpec.StringResource(R.string.autoBackup_errorCard_message, code),
            attributes = OSMessageCardAttributes()
                .dismissible(OSImageSpec.Drawable(R.drawable.ic_baseline_close), onDismiss)
                .style(OSMessageCardStyle.Alert),
            modifier = modifier
                .testTag(UiConstants.TestTag.Item.AutoBackupErrorCard),
            action = {
                OSTextButton(
                    text = LbcTextSpec.StringResource(R.string.autoBackup_errorCard_action_askForHelp),
                    onClick = { uriHandler.openUri(CommonUiConstants.ExternalLink.Discord) },
                    modifier = Modifier.padding(bottom = OSDimens.SystemSpacing.Small),
                )
            },
        )
    }
}

@OsDefaultPreview
@Composable
private fun AutoBackupErrorCardPreview() {
    OSPreviewBackgroundTheme {
        AutoBackupErrorHomeInfoData(
            code = loremIpsumSpec(1),
        ) {}.Composable(Modifier)
    }
}

private const val KeyAutoBackupErrorCard: String = "KeyAutoBackupErrorCard"
private const val ContentTypeAutoBackupErrorCard: String = "ContentTypeAutoBackupErrorCard"
