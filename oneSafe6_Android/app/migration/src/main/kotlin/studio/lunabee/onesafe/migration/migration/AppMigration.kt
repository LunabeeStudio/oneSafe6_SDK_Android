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
 * Created by Lunabee Studio / Date - 10/3/2024 - for the oneSafe6 SDK.
 * Last modified 10/3/24, 12:04 PM
 */

package studio.lunabee.onesafe.migration.migration

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.migration.MigrationSafeData0
import studio.lunabee.onesafe.migration.MigrationSafeData15

sealed interface AppMigration {
    val startVersion: Int
    val endVersion: Int
}

/**
 * Migration param used from v0 until v14
 */
abstract class AppMigration0(
    override val startVersion: Int,
    override val endVersion: Int,
) : AppMigration {
    abstract suspend fun migrate(migrationSafeData: MigrationSafeData0): LBResult<Unit>
}

/**
 * Migration param used since v15
 */
abstract class AppMigration15(
    override val startVersion: Int,
    override val endVersion: Int,
) : AppMigration {
    abstract suspend fun migrate(migrationSafeData: MigrationSafeData15): LBResult<Unit>
}
