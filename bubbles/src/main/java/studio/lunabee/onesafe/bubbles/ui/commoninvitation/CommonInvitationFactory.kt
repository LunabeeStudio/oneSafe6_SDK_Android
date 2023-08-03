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
 * Created by Lunabee Studio / Date - 7/20/2023 - for the oneSafe6 SDK.
 * Last modified 20/07/2023 11:24
 */

package studio.lunabee.onesafe.bubbles.ui.commoninvitation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.ui.res.OSDimens

object CommonInvitationFactory {

    fun invitationBarcodeCard(
        invitationQr: ImageBitmap,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            OSCard(
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Image(
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(OSDimens.SystemSpacing.Huge),
                    contentScale = ContentScale.FillWidth,
                    bitmap = invitationQr,
                )
            }
        }
    }

    fun finishButtonScreen(
        onClick: () -> Unit,
        lazyListScope: LazyListScope,
    ) {
        lazyListScope.item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OSFilledButton(
                    text = LbcTextSpec.StringResource(R.string.common_finish),
                    onClick = onClick,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
    }
}
