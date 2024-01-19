/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/23/2023 - for the oneSafe6 SDK.
 * Last modified 23/08/2023 09:38
 */

package studio.lunabee.onesafe.bubbles.ui.contact.form.common

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.R

enum class ContactFormType(
    val title: LbcTextSpec,
    val buttonString: LbcTextSpec,
) {
    FromScratch(
        title = LbcTextSpec.StringResource(R.string.bubbles_createContactScreen_fromScratch_title),
        buttonString = LbcTextSpec.StringResource(R.string.bubbles_createContactScreen_fromScratch_invite),
    ),
    FromInvitation(
        title = LbcTextSpec.StringResource(R.string.bubbles_createContactScreen_fromInvitation_title),
        buttonString = LbcTextSpec.StringResource(R.string.common_finish),
    ),
    Edit(
        title = LbcTextSpec.StringResource(R.string.bubbles_contactDetail_editAction),
        buttonString = LbcTextSpec.StringResource(R.string.common_finish),
    ),
}
