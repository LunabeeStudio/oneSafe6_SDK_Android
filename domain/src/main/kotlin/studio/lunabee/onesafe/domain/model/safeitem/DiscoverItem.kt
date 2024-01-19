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
 * Created by Lunabee Studio / Date - 7/5/2023 - for the oneSafe6 SDK.
 * Last modified 7/5/23, 11:45 AM
 */

package studio.lunabee.onesafe.domain.model.safeitem

import kotlinx.serialization.Serializable

@Serializable
data class DiscoveryData(
    val labels: Map<String, Map<String, String>>,
    val data: List<DiscoveryItem>,
)

@Serializable
data class DiscoveryItem(
    val title: String,
    val isFavorite: Boolean = false,
    val color: String? = null,
    val items: List<DiscoveryItem> = listOf(),
    val fields: List<DiscoveryField> = listOf(),
)

@Serializable
data class DiscoveryField(
    val isItemIdentifier: Boolean,
    val isSecured: Boolean,
    val kind: String,
    val position: Int,
    val showPrediction: Boolean,
    val name: String,
    val value: String,
    val placeholder: String,
    val formattingMask: String? = null,
    val secureDisplayMask: String? = null,
)
