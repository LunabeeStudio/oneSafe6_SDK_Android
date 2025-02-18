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
 * Created by Lunabee Studio / Date - 9/11/2024 - for the oneSafe6 SDK.
 * Last modified 9/11/24, 10:01 AM
 */

package studio.lunabee.onesafe.feature.breadcrumb

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.feature.camera.model.CameraData

@Stable
sealed interface BreadcrumbUiState {
    data object Initializing : BreadcrumbUiState
    data class Idle(
        val breadcrumbItems: ImmutableList<BreadcrumbUiDataSpec>,
        val userColor: Color?,
        val cameraForField: CameraData,
        val dialogState: DialogState?,
    ) : BreadcrumbUiState
}
