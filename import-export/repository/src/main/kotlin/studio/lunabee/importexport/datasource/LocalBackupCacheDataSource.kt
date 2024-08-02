/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 10/11/2023 - for the oneSafe6 SDK.
 * Last modified 10/11/23, 2:38 PM
 */

package studio.lunabee.importexport.datasource

import studio.lunabee.onesafe.importexport.model.LocalBackup
import java.io.File
import java.io.InputStream
import java.time.Instant

interface LocalBackupCacheDataSource {
    suspend fun addBackup(inputStream: InputStream, date: Instant): File
    suspend fun removeBackup(localBackup: LocalBackup): Boolean
}
