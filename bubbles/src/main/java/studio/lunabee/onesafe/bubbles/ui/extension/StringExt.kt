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
 * Last modified 20/07/2023 11:06
 */

package studio.lunabee.onesafe.bubbles.ui.extension

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import studio.lunabee.onesafe.bubbles.ui.BubbleUiConstants
import studio.lunabee.onesafe.commonui.CommonUiConstants

fun String.toBarcodeBitmap(): ImageBitmap? {
    return try {
        val barcodeEncoder = BarcodeEncoder()
        val bitmap = barcodeEncoder.encodeBitmap(
            this,
            BarcodeFormat.QR_CODE,
            BubbleUiConstants.BarcodeSize,
            BubbleUiConstants.BarcodeSize,
        )
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

fun String.getDeepLinkFromMessage(isUsingDeppLink: Boolean = true): String {
    return if (isUsingDeppLink) {
        "${CommonUiConstants.Deeplink.BubbleDeeplinkUrl}${Uri.encode(this)}"
    } else {
        Uri.decode(this)
    }
}

fun String.getBase64FromMessage(): String {
    return this.substringAfter(CommonUiConstants.Deeplink.BubbleDeeplinkUrl).let(Uri::decode)
}
