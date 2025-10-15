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
 * Created by Lunabee Studio / Date - 8/30/2023 - for the oneSafe6 SDK.
 * Last modified 8/30/23, 6:27 PM
 */

package studio.lunabee.onesafe.ime

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import studio.lunabee.onesafe.bubbles.ui.home.BubblesHomeDestination
import studio.lunabee.onesafe.bubbles.ui.home.BubblesHomeTab
import studio.lunabee.onesafe.bubbles.ui.welcome.OnBoardingBubblesDestination
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import java.util.UUID

object ImeDeeplinkHelper {
    fun deeplinkBubblesHomeContact(context: Context) {
        val packageManager = context.packageManager
        val contactIntent = packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            val stringUri = CommonUiConstants.Deeplink.MainNavScheme + "://" + BubblesHomeDestination.getRoute(BubblesHomeTab.Contacts)
            data = stringUri.toUri()
        }
        context.startActivity(contactIntent)
    }

    fun deeplinkBubblesWriteMessage(context: Context, contactId: UUID) {
        val packageManager = context.packageManager
        val contactIntent = packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            val stringUri = CommonUiConstants.Deeplink.MainNavScheme + "://" + WriteMessageDestination.getRouteFromContactId(contactId)
            data = stringUri.toUri()
        }
        context.startActivity(contactIntent)
    }

    fun deeplinkBubblesOnboarding(context: Context) {
        val packageManager = context.packageManager
        val contactIntent = packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            data = Uri
                .Builder()
                .scheme(CommonUiConstants.Deeplink.MainNavScheme)
                .authority(OnBoardingBubblesDestination.route)
                .build()
        }
        context.startActivity(contactIntent)
    }
}
