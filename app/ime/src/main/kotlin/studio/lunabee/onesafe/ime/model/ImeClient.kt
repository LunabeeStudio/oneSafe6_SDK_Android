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
 * Created by Lunabee Studio / Date - 6/13/2023 - for the oneSafe6 SDK.
 * Last modified 6/13/23, 11:01 AM
 */

package studio.lunabee.onesafe.ime.model

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil.compose.AsyncImage
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.extension.getPackageInfoCompat
import studio.lunabee.onesafe.ime.ui.res.ImeDimens

private val logger = LBLogger.get<ImeClient>()

data class ImeClient(
    val packageName: String,
    val applicationName: String?,
    val icon: Drawable?,
) {
    @Composable
    fun Name(modifier: Modifier = Modifier) {
        OSText(
            modifier = modifier,
            text = LbcTextSpec.Raw(applicationName ?: packageName),
            style = MaterialTheme.typography.labelMedium,
        )
    }

    @Composable
    fun Logo(modifier: Modifier = Modifier) {
        icon?.let {
            AsyncImage(
                modifier = modifier
                    .height(ImeDimens.ClientAppLogoSize),
                model = icon,
                contentDescription = null,
            )
        }
    }

    companion object {
        fun fromUid(appContext: Context, uid: Int, notFoundFallbackClient: ImeClient?): ImeClient? {
            val pm = appContext.packageManager
            return pm.getNameForUid(uid)?.let { packageName ->
                val info = pm.getPackageInfoCompat(packageName, 0)
                try {
                    ImeClient(
                        packageName,
                        info?.applicationInfo?.let { pm.getApplicationLabel(it).toString() },
                        pm.getApplicationIcon(packageName),
                    )
                } catch (e: PackageManager.NameNotFoundException) {
                    notFoundFallbackClient
                } catch (e: Throwable) {
                    logger.e(e)
                    null
                }
            }
        }
    }
}
