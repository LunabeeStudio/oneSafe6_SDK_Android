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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/24/24, 11:39 AM
 */

package studio.lunabee.onesafe.importexport.utils

import studio.lunabee.onesafe.domain.common.UuidProvider
import java.util.UUID
import javax.inject.Inject

class AutoBackupErrorIdProvider @Inject constructor(
    private val provider: UuidProvider,
) {
    operator fun invoke(): UUID = provider()
}
