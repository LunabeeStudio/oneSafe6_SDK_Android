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
 * Created by Lunabee Studio / Date - 7/7/2023 - for the oneSafe6 SDK.
 * Last modified 7/7/23, 2:49 PM
 */

package studio.lunabee.onesafe.commonui

import android.net.Uri
import kotlinx.coroutines.flow.SharingStarted

object CommonUiConstants {
    object Deeplink {
        const val MAIN_NAV_SCHEME: String = "mainnav"
        const val CONTENT_NAV_SCHEME: String = "content"
        const val FILE_NAV_SCHEME: String = "file"
        val BubblesDeeplinkUrl: Uri = Uri.Builder()
            .scheme("https")
            .authority("www.onesafe-apps.com")
            .path("bubbles")
            .build()
    }

    object Flow {
        val DefaultSharingStarted: SharingStarted
            get() = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000, replayExpirationMillis = 5_000)
    }

    object ExternalLink {
        const val Youtube: String = "https://www.youtube.com/channel/UC9zBsI-tjUIRKC-wN0miV3Q"
        const val Facebook: String = "https://www.facebook.com/oneSafe"
        const val Twitter: String = "https://twitter.com/onesafe_lunabee"
        const val Tiktok: String = "https://www.tiktok.com/@onesafe.6"
        const val Playstore: String = "market://details?id=studio.lunabee.onesafe"
        const val Discord: String = "https://discord.gg/PMmdpVkSQt"
    }

    object AppLaunch {
        const val DeleteOnImportExtraKey: String = "3f819387-1a0e-4895-8604-37d9558194d6"
    }
}
