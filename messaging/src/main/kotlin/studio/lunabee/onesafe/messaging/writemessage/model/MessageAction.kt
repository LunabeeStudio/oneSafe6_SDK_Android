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
 * Created by Lunabee Studio / Date - 8/18/2023 - for the oneSafe6 SDK.
 * Last modified 18/08/2023 16:02
 */

package studio.lunabee.onesafe.messaging.writemessage.model

import androidx.annotation.DrawableRes
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.R

sealed class MessageAction(
    @DrawableRes val icon: Int,
    val text: LbcTextSpec,
    val isCritical: Boolean,
) {
    abstract val onClick: () -> Unit

    class Resend(override val onClick: () -> Unit) : MessageAction(
        icon = R.drawable.ic_send,
        text = LbcTextSpec.StringResource(R.string.bubbles_writeMessageScreen_resend),
        isCritical = false,
    )

    class Copy(override val onClick: () -> Unit) : MessageAction(
        icon = R.drawable.ic_content_copy,
        text = LbcTextSpec.StringResource(R.string.common_copy),
        isCritical = false,
    )

    class Delete(override val onClick: () -> Unit) : MessageAction(
        icon = R.drawable.ic_delete,
        text = LbcTextSpec.StringResource(R.string.bubbles_writeMessageScreen_deleteMessage),
        isCritical = true,
    )
}
