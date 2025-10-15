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
 * Created by Lunabee Studio / Date - 8/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/08/2024 14:32
 */

package studio.lunabee.onesafe.messaging.senditem.model

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

sealed interface SharedItemInfo {
    val name: LbcTextSpec

    fun getDescription(): LbcTextSpec

    data class NoChildren(
        override val name: LbcTextSpec,
    ) : SharedItemInfo {
        override fun getDescription(): LbcTextSpec = LbcTextSpec.StringResource(
            OSString.bubbles_shareItem_itemDescription_noChildren,
            name,
        )
    }

    data class WithChildren(
        override val name: LbcTextSpec,
        val numberOfChild: Int,
    ) : SharedItemInfo {
        override fun getDescription(): LbcTextSpec = LbcTextSpec.StringResource(
            OSString.bubbles_shareItem_itemDescription_withChildren,
            name,
            numberOfChild,
        )
    }
}
