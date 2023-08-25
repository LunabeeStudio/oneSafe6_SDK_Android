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

sealed class MessageAction(@DrawableRes val icon: Int, val text: LbcTextSpec) {
    abstract val onClick: () -> Unit

    class Resend(override val onClick: () -> Unit) : MessageAction(
        studio.lunabee.onesafe.messaging.R.drawable.ic_send,
        LbcTextSpec.StringResource(R.string.bubbles_writeMessageScreen_resend),
    )

    class Copy(override val onClick: () -> Unit) : MessageAction(
        R.drawable.ic_content_copy,
        LbcTextSpec.StringResource(R.string.common_copy),
    )
}
