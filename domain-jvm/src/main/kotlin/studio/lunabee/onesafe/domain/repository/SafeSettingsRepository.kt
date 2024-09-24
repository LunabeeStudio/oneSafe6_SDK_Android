/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 11:06 AM
 */

package studio.lunabee.onesafe.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.SafeId

interface SafeSettingsRepository {
    fun materialYou(safeId: SafeId): Flow<Boolean>
    fun automationFlow(safeId: SafeId): Flow<Boolean>
    fun displayShareWarningFlow(safeId: SafeId): Flow<Boolean>
    fun cameraSystemFlow(safeId: SafeId): Flow<CameraSystem>
    fun allowScreenshotFlow(safeId: SafeId): Flow<Boolean> // TODO <multisafe> -> security settings (?)
    fun bubblesPreview(safeId: SafeId): Flow<Boolean> // TODO <multisafe> -> new bubbles settings (?)
    fun bubblesHomeCardCtaState(safeId: SafeId): Flow<CtaState> // TODO <multisafe> -> new bubbles settings (?)
    fun independentSafeInfoCtaState(safeId: SafeId): Flow<CtaState>

    suspend fun cameraSystem(safeId: SafeId): CameraSystem
    suspend fun allowScreenshot(safeId: SafeId): Boolean
    suspend fun automation(safeId: SafeId): Boolean

    suspend fun toggleMaterialYou(safeId: SafeId)
    suspend fun toggleAutomation(safeId: SafeId)
    suspend fun disableShareWarningDisplay(safeId: SafeId)
    suspend fun toggleAllowScreenshot(safeId: SafeId)
    suspend fun toggleShakeToLock(safeId: SafeId)
    suspend fun setBubblesPreview(safeId: SafeId, value: Boolean)
    suspend fun setCameraSystem(safeId: SafeId, value: CameraSystem)
    suspend fun setBubblesHomeCardCtaState(safeId: SafeId, ctaState: CtaState)
    suspend fun displayShareWarning(safeId: SafeId): Boolean
    suspend fun setIndependentSafeInfoCtaState(safeId: SafeId, ctaState: CtaState)
}
