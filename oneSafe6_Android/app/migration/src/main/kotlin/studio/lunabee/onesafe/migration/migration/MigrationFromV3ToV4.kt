/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/9/24, 9:41 AM
 */

package studio.lunabee.onesafe.migration.migration

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.migration.MigrationSafeData0
import javax.inject.Inject

/**
 * Delete backup notification channel to re-create it with IMPORTANCE_HIGH flag
 */
class MigrationFromV3ToV4 @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppMigration0(3, 4) {
    override suspend fun migrate(migrationSafeData: MigrationSafeData0): LBResult<Unit> {
        val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        manager.deleteNotificationChannel("cca60cd3-876b-4279-8ce4-b7c3936fb3bc")
        return LBResult.Success(Unit)
    }
}
