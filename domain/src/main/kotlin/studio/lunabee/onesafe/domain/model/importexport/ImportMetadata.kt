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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.domain.model.importexport

import java.time.Instant

data class ImportMetadata(
    val archiveKind: OSArchiveKind,
    val isFromOldOneSafe: Boolean, // archive built with old version of OneSafe (only used for UI purpose at this time)
    val itemCount: Int, // number of item to import
    val fromPlatform: String, // Platform Android or iOs
    val archiveVersion: Int, // Spec version (common between Android and iOs)
    val createdAt: Instant, // Using ISO8601 format like this: 2022-11-29T06:18:08.095Z
)
