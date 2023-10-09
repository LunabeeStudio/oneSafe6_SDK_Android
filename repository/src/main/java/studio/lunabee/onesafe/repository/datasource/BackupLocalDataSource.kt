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
 * Created by Lunabee Studio / Date - 9/28/2023 - for the oneSafe6 SDK.
 * Last modified 9/28/23, 6:44 PM
 */

package studio.lunabee.onesafe.repository.datasource

import kotlinx.coroutines.flow.Flow
import java.io.File

interface BackupLocalDataSource {
    fun addBackup(backupFile: File)
    fun getBackups(): List<File>
    fun getBackupsFlow(): Flow<List<File>>
}
