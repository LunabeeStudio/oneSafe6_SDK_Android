package studio.lunabee.onesafe.common.model.item

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.EmojiNameProvider
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.startEmojiOrNull
import studio.lunabee.onesafe.commonui.localprovider.LocalItemStyle
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.itemactions.OSSafeItemWithAction
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.extensions.toColor
import studio.lunabee.onesafe.ui.res.OSDimens
import java.util.UUID

@Stable
class PlainItemDataDefault(
    override val id: UUID,
    override val itemNameProvider: OSNameProvider,
    override val icon: ByteArray?,
    override val color: Color?,
    override val actions: (suspend () -> List<SafeItemAction>)?,
) : PlainItemData {
    @Composable
    fun Composable(
        modifier: Modifier,
        itemStyle: OSSafeItemStyle = LocalItemStyle.current.standardStyle,
        onItemClick: (PlainItemData) -> Unit,
    ) {
        val itemName = itemNameProvider.name
        OSSafeItemWithAction(
            illustration = safeIllustration,
            style = itemStyle,
            label = itemName,
            modifier = modifier
                .padding(horizontal = OSDimens.SystemSpacing.Regular - OSDimens.SystemSpacing.Small)
                .clip(shape = MaterialTheme.shapes.medium),
            getActions = actions,
            onClick = { onItemClick(this) },
            clickLabel = LbcTextSpec.StringResource(id = OSString.accessibility_home_itemClicked, itemName.string),
            paddingValues = PaddingValues(all = OSDimens.SystemSpacing.Small),
        )
    }

    override val safeIllustration: OSItemIllustration
        get() = if (icon != null) {
            OSItemIllustration.Image(OSImageSpec.Data(icon))
        } else if (itemNameProvider is EmojiNameProvider) {
            OSItemIllustration.Emoji(itemNameProvider.placeholderName, color)
        } else {
            OSItemIllustration.Text(itemNameProvider.placeholderName, color)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlainItemData

        if (id != other.id) return false
        if (itemNameProvider != other.itemNameProvider) return false
        if (icon != null) {
            if (other.icon == null) return false
            if (!icon.contentEquals(other.icon)) return false
        } else if (other.icon != null) return false
        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + itemNameProvider.hashCode()
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        result = 31 * result + (color?.hashCode() ?: 0)
        return result
    }

    companion object {
        private fun equalsForUi(old: SafeItem, new: SafeItem?): Boolean {
            return old.id == new?.id &&
                old.iconId == new.iconId &&
                old.encName contentEquals new.encName &&
                old.encColor contentEquals new.encColor
        }

        private fun equalsForUi(olds: List<SafeItem>, news: List<SafeItem>): Boolean {
            if (olds.size != news.size) return false
            return olds.asSequence()
                .zip(news.asSequence()) { old, new -> equalsForUi(old, new) }
                .all { it }
        }

        fun Flow<List<SafeItem>>.mapForUi(
            decryptUseCase: ItemDecryptUseCase,
            getIconUseCase: GetIconUseCase,
            getQuickAction: suspend (item: UUID) -> suspend () -> List<SafeItemAction>,
        ): Flow<List<PlainItemData>> {
            return distinctUntilChanged { old, new ->
                equalsForUi(old, new)
            }.map { safeItems ->
                safeItems.map { safeItem ->
                    safeItem.toPlainItemDataDefault(decryptUseCase, getIconUseCase, getQuickAction(safeItem.id))
                }
            }
        }
    }
}

suspend fun SafeItem.toPlainItemDataDefault(
    decryptUseCase: ItemDecryptUseCase,
    getIconUseCase: GetIconUseCase,
    actions: (suspend () -> List<SafeItemAction>)?,
): PlainItemDataDefault {
    var error = false
    val itemName = encName?.let {
        val result = decryptUseCase(it, id, String::class)
        if (result is LBResult.Failure) error = true
        result.data
    }

    val color = if (error) {
        AppConstants.Ui.Item.ErrorColor
    } else {
        encColor?.let { bytes -> decryptUseCase(bytes, id, String::class).data }?.toColor()
    }

    val icon = iconId?.let { bytes -> getIconUseCase(bytes, id).data }
    val itemNameProvider = if (error) {
        ErrorNameProvider
    } else if (icon == null && itemName?.startEmojiOrNull() != null) {
        EmojiNameProvider(itemName)
    } else {
        DefaultNameProvider(itemName)
    }
    return PlainItemDataDefault(
        id = id,
        itemNameProvider = itemNameProvider,
        icon = icon,
        color = color,
        actions = actions,
    )
}
