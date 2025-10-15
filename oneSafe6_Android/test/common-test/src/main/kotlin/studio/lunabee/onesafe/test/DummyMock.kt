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
 * Created by Lunabee Studio / Date - 7/2/2024 - for the oneSafe6 SDK.
 * Last modified 7/2/24, 10:35 AM
 */

package studio.lunabee.onesafe.test

import io.mockk.mockk
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityGetRemainingTimeUseCase
import studio.lunabee.onesafe.importexport.repository.AutoBackupSettingsRepository

// FIXME https://github.com/mockk/mockk/issues/1073
//  cannot mockk suspend functions which returns a value class (-> Duration)
open class MockAutoBackupSettingsRepository(mock: AutoBackupSettingsRepository = mockk()) : AutoBackupSettingsRepository by mock

open class MockSecuritySettingsRepository(mock: SecuritySettingsRepository = mockk()) : SecuritySettingsRepository by mock

open class MockAutoLockInactivityGetRemainingTimeUseCase(
    mock: AutoLockInactivityGetRemainingTimeUseCase = mockk(),
) : AutoLockInactivityGetRemainingTimeUseCase by mock
