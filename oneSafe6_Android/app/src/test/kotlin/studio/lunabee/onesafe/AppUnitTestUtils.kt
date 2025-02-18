package studio.lunabee.onesafe

import androidx.compose.ui.graphics.Color
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.item.ItemsLayout
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.test.OSTestConfig
import java.util.UUID

internal object AppUnitTestUtils {
    // copy of app/src/androidTest/kotlin/studio/lunabee/onesafe/AppAndroidTestUtils.kt for Unit tests
    fun createPlainItemData(
        id: UUID = UUID.randomUUID(),
        itemNameProvider: OSNameProvider = DefaultNameProvider(null),
        icon: ByteArray? = null,
        color: Color? = null,
        identifier: LbcTextSpec? = null,
        actions: (suspend () -> List<SafeItemAction>)? = null,
        itemsLayout: ItemsLayout = ItemsLayout.from(OSTestConfig.itemLayouts),
    ): PlainItemData {
        return when (itemsLayout) {
            ItemsLayout.Grid -> PlainItemDataDefault(id, itemNameProvider, icon, color, actions)
            ItemsLayout.List -> PlainItemDataRow(id, itemNameProvider, icon, color, identifier, actions)
        }
    }

    fun createPlainItemDataRow(
        id: UUID = UUID.randomUUID(),
        itemNameProvider: OSNameProvider = DefaultNameProvider(null),
        icon: ByteArray? = null,
        color: Color? = null,
        identifier: String? = null,
        actions: (suspend () -> List<SafeItemAction>)? = null,
    ): PlainItemDataRow {
        return PlainItemDataRow(id, itemNameProvider, icon, color, identifier?.let(LbcTextSpec::Raw), actions)
    }

    fun createPlainItemDataDefault(
        id: UUID = UUID.randomUUID(),
        itemNameProvider: OSNameProvider = DefaultNameProvider(null),
        icon: ByteArray? = null,
        color: Color? = null,
        actions: (suspend () -> List<SafeItemAction>)? = null,
    ): PlainItemDataDefault {
        return PlainItemDataDefault(id, itemNameProvider, icon, color, actions)
    }
}
