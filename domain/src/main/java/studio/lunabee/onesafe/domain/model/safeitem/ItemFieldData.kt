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

package studio.lunabee.onesafe.domain.model.safeitem

/**
 * Describe how a field should be built.
 */
data class ItemFieldData(
    val name: String?, // Name that will be used as placeholder.
    val position: Double,
    val placeholder: String?, // currently we use only name
    val value: String?,
    val kind: SafeItemFieldKind?,
    val showPrediction: Boolean,
    val isItemIdentifier: Boolean,
    val formattingMask: String?,
    val secureDisplayMask: String?,
    val isSecured: Boolean,
)
