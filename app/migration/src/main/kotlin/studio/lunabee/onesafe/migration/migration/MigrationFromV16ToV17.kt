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
 * Created by Lunabee Studio / Date - 10/7/2024 - for the oneSafe6 SDK.
 * Last modified 10/7/24, 10:25 AM
 */

package studio.lunabee.onesafe.migration.migration

import android.content.Context
import androidx.datastore.dataStoreFile
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.migration.MigrationSafeData15
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV15ToV16>()

/**
 * Remove recent search datastore (moved to Room)
 */
class MigrationFromV16ToV17 @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppMigration15(16, 17) {
    override suspend fun migrate(migrationSafeData: MigrationSafeData15): LBResult<Unit> = OSError.runCatching(logger) {
        context.dataStoreFile(LegacyDatastoreFileName).delete()
    }
}

private const val LegacyDatastoreFileName: String = "201c4654-39bd-400e-a3ef-3408e0729273"
