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
 * Created by Lunabee Studio / Date - 6/21/2024 - for the oneSafe6 SDK.
 * Last modified 6/21/24, 4:49 PM
 */

package studio.lunabee.onesafe.messaging.extension

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import studio.lunabee.onesafe.messaging.MessagingConstants
import java.io.File

fun Context.getFileSharingIntent(
    fileToShare: File,
    mimeType: String,
    fileProviderAuthority: String = MessagingConstants.getAuthority(packageName),
): Intent {
    val uri = FileProvider.getUriForFile(this@getFileSharingIntent, fileProviderAuthority, fileToShare)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = mimeType
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    return Intent.createChooser(sendIntent, null)
}
