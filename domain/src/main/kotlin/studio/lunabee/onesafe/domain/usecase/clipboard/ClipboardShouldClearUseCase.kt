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

package studio.lunabee.onesafe.domain.usecase.clipboard

import kotlin.time.Duration

/**
 * Check if a clipboard clear request should be enqueue depending of user parameters and value present in the clipboard.
 * Should be call both to enqueue the clearing job and to verify the state of the clipboard before actually cleaning
 */
interface ClipboardShouldClearUseCase {
    /**
     * @return The user setting delay for clipboard auto-clear (returned by [ClipboardGetClearDelayUseCase]) or null if the clipboard
     * should not be clear
     */
    suspend operator fun invoke(): Duration?
}
