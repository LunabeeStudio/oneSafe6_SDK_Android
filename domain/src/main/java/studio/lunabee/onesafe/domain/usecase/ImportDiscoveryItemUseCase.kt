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
 * Last modified 7/5/23, 2:36 PM
 */

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.withTimeoutOrNull
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.DiscoveryData
import studio.lunabee.onesafe.domain.model.safeitem.DiscoveryItem
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.error.OSError
import java.util.UUID
import javax.inject.Inject

class ImportDiscoveryItemUseCase @Inject constructor(
    private val createItemUseCase: CreateItemUseCase,
    private val addFieldUseCase: AddFieldUseCase,
    private val getIconAndColorFromUrlUseCase: GetIconAndColorFromUrlUseCase,
    private val fieldIdProvider: FieldIdProvider,
) {

    suspend operator fun invoke(
        data: DiscoveryData,
        locale: String,
    ): LBResult<Unit> {
        return OSError.runCatching {
            val labels: Map<String, String>? = data.labels[locale] ?: data.labels[DefaultLocale]
            data.data.forEach { plainItem ->
                importItem(
                    parentId = null,
                    discoveryItem = plainItem,
                    labels = labels,
                )
            }
        }
    }

    private suspend fun importItem(
        parentId: UUID? = null,
        discoveryItem: DiscoveryItem,
        labels: Map<String, String>?,
    ) {
        val itemUrl: String? = labels?.get(discoveryItem.fields.firstOrNull { it.kind == SafeItemFieldKind.Url.id }?.value)
        var iconColorPair: Pair<ByteArray?, String?>? = null
        if (itemUrl != null) {
            iconColorPair = withTimeoutOrNull(FetchingIconTimeOut) { getIconAndColorFromUrlUseCase(itemUrl) }
        }

        val safeItem = createItemUseCase(
            name = labels?.get(discoveryItem.title),
            parentId = parentId,
            isFavorite = discoveryItem.isFavorite,
            icon = iconColorPair?.first,
            color = discoveryItem.color ?: iconColorPair?.second,
            position = null,
        )

        if (safeItem is LBResult.Success) {
            val itemId: UUID = safeItem.successData.id
            addFieldUseCase(
                itemId = itemId,
                itemFieldsData = discoveryItem.fields.map { discoveryField ->
                    ItemFieldData(
                        id = fieldIdProvider(),
                        name = labels?.get(discoveryField.name),
                        position = discoveryField.position.toDouble(),
                        placeholder = labels?.get(discoveryField.placeholder),
                        value = labels?.get(discoveryField.value),
                        kind = SafeItemFieldKind.fromString(discoveryField.kind),
                        showPrediction = discoveryField.showPrediction,
                        isItemIdentifier = discoveryField.isItemIdentifier,
                        formattingMask = discoveryField.formattingMask,
                        secureDisplayMask = discoveryField.secureDisplayMask,
                        isSecured = discoveryField.isSecured,
                    )
                },
            )

            discoveryItem.items.forEach { child ->
                importItem(
                    parentId = itemId,
                    discoveryItem = child,
                    labels = labels,
                )
            }
        }
    }
}

private const val DefaultLocale: String = "en"
private const val FetchingIconTimeOut: Long = 3_000
