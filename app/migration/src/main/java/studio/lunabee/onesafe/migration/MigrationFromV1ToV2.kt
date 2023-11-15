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

package studio.lunabee.onesafe.migration

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.usecase.verifypassword.SetLastPasswordVerificationUseCase
import studio.lunabee.onesafe.domain.usecase.verifypassword.SetVerifyPasswordIntervalUseCase
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSMigrationError
import javax.inject.Inject

/**
 * Add lastPasswordVerification and passwordVerificationInterval.
 */
class MigrationFromV1ToV2 @Inject constructor(
    private val setLastPasswordVerificationUseCase: SetLastPasswordVerificationUseCase,
    private val setVerifyPasswordIntervalUseCase: SetVerifyPasswordIntervalUseCase,
) {
    operator fun invoke(): LBResult<Unit> = OSError.runCatching(
        mapErr = {
            OSMigrationError(OSMigrationError.Code.SET_PASSWORD_VERIFICATION_FAIL, cause = it)
        },
    ) {
        setLastPasswordVerificationUseCase()
        setVerifyPasswordIntervalUseCase(VerifyPasswordInterval.EVERY_TWO_MONTHS) // Default value.
    }
}
