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
 * Created by Lunabee Studio / Date - 11/7/2023 - for the oneSafe6 SDK.
 * Last modified 11/7/23, 4:56 PM
 */

package studio.lunabee.onesafe.importexport.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.qualifier.VersionName
import java.time.Clock
import java.time.ZonedDateTime
import javax.inject.Inject

class AndroidCloudBackupDescriptionProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:VersionName private val versionName: String,
    @param:BuildNumber private val versionCode: Int,
    private val clock: Clock,
) : CloudBackupDescriptionProvider {
    override operator fun invoke(): String = context.getString(
        OSString.googleDrive_backup_description,
        "${context.getString(OSString.application_name)} Android - $versionName#$versionCode",
        ZonedDateTime.now(clock).toString(),
    )
}
