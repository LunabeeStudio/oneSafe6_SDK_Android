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

package studio.lunabee.onesafe.domain.usecase

import studio.lunabee.onesafe.domain.model.common.UpdateState
import javax.inject.Inject

/**
 * Compare two value to see what type of changed should be applied.
 * Values should not be encrypted at this point.
 *
 * Examples:
 * value = "test", previousValue = "test" -> Unchanged
 * value = null, previousValue = "test" -> Removed
 * value = "test", previousValue = null -> Modified
 */
class CheckValueChangeUseCase @Inject constructor() {
    operator fun <T> invoke(
        value: T,
        previousValue: T,
    ): UpdateState<T> = when {
        previousValue != null && value == null -> UpdateState.Removed()
        previousValue != value -> UpdateState.ModifiedTo(newValue = value)
        else -> UpdateState.Unchanged(value)
    }
}
