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

package studio.lunabee.onesafe.storage

internal object DaoUtils {
    const val FIELD_ORDER_BY_POSITION = "ORDER BY position ASC"
    const val IS_NOT_DELETED = "deleted_at IS NULL"
    const val IS_DELETED = "deleted_at IS NOT NULL"
    const val IS_FAVORITE = "is_favorite = 1"

    const val ITEM_ORDER_BY_POSITION = "ORDER BY SafeItem.position ASC, SafeItem.index_alpha ASC"
    const val ITEM_ORDER_BY_INDEX_ALPHA = "ORDER BY SafeItem.index_alpha ASC, SafeItem.position ASC"
    const val ITEM_ORDER_BY_UPDATED_AT = "ORDER BY SafeItem.updated_at DESC, SafeItem.index_alpha ASC, SafeItem.position ASC"
    const val ITEM_ORDER_BY_DELETED_AT = "ORDER BY SafeItem.deleted_at ASC, SafeItem.index_alpha ASC, SafeItem.position ASC"
    const val ITEM_ORDER_BY_CONSULTED_AT = "ORDER BY SafeItem.consulted_at DESC, SafeItem.index_alpha ASC, SafeItem.position ASC"
}
