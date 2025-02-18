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
 * Created by Lunabee Studio / Date - 4/4/2024 - for the oneSafe6 SDK.
 * Last modified 04/04/2024 10:21
 */

package studio.lunabee.onesafe.feature.settings.personalization

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.common.extensions.alias
import studio.lunabee.onesafe.model.AppIcon
import javax.inject.Inject

class GetCurrentAliasUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    operator fun invoke(): AppIcon {
        val packageManager = context.packageManager
        val packageName = MainActivity::class.java.`package`!!.name
        return AppIcon.entries.first { alias ->
            val componentName = ComponentName(context, "$packageName.${alias.alias}")
            val enableComponentValue = listOf(
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            )
            enableComponentValue.contains(packageManager.getComponentEnabledSetting(componentName))
        }
    }
}
