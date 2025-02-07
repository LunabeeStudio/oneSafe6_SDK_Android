package studio.lunabee.onesafe.common.model.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lunabee.lbcore.model.LBResult
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSLazyCard
import studio.lunabee.onesafe.common.utils.FormattingHelper
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.itemactions.OSItemRowWithAction
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.molecule.OSShimmerItemRow
import studio.lunabee.onesafe.ui.extensions.toColor
import java.util.UUID

class PlainItemDataRow(
    private val default: PlainItemDataDefault,
    val identifier: LbcTextSpec?,
) : PlainItemData by default {

    constructor(
        id: UUID,
        itemNameProvider: OSNameProvider,
        icon: ByteArray?,
        color: Color?,
        identifier: LbcTextSpec?,
        actions: (suspend () -> List<SafeItemAction>)?,
    ) : this(
        default = PlainItemDataDefault(
            id = id,
            itemNameProvider = itemNameProvider,
            icon = icon,
            color = color,
            actions = actions,
        ),
        identifier = identifier,
    )

    @Composable
    fun Composable(
        modifier: Modifier,
        onItemClick: (PlainItemData) -> Unit,
        position: OSLazyCardContent.Position,
    ) {
        OSLazyCard(
            position = position,
            modifier = modifier,
        ) { padding ->
            OSItemRowWithAction(
                osItemIllustration = safeIllustration,
                label = itemNameProvider.name,
                onClick = { onItemClick(this@PlainItemDataRow) },
                paddingValues = padding,
                subtitle = identifier,
                getActions = actions,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlainItemDataRow

        if (default != other.default) return false
        if (identifier != other.identifier) return false

        return true
    }

    override fun hashCode(): Int {
        var result = default.hashCode()
        result = 31 * result + (identifier?.hashCode() ?: 0)
        return result
    }

    companion object {
        @Composable
        fun Shimmer(
            modifier: Modifier,
            position: OSLazyCardContent.Position,
        ) {
            OSLazyCard(
                position = position,
                modifier = modifier,
            ) { padding ->
                OSShimmerItemRow(
                    paddingValues = padding,
                )
            }
        }
    }
}

suspend fun SafeItemWithIdentifier.toPlainItemDataRow(
    decryptUseCase: ItemDecryptUseCase,
    getIconUseCase: GetIconUseCase,
    getQuickAction: suspend (item: UUID) -> (suspend () -> List<SafeItemAction>)?,
): PlainItemDataRow {
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

    val identifierKind: SafeItemFieldKind? = encIdentifierKind?.let {
        decryptUseCase(it, id, SafeItemFieldKind::class).data
    }

    val identifier: String? = encIdentifier?.let {
        FormattingHelper.getVisibleValue(
            mask = encSecuredDisplayMask,
            encValue = encIdentifier,
            itemId = id,
            safeItemFieldKind = identifierKind,
            decryptUseCase = decryptUseCase,
        )
    }

    val icon = iconId?.let { bytes -> getIconUseCase(bytes, id).data }

    val itemNameProvider = if (error) {
        ErrorNameProvider
    } else {
        OSNameProvider.fromName(itemName, icon != null)
    }

    return PlainItemDataRow(
        id = id,
        itemNameProvider = itemNameProvider,
        icon = icon,
        color = color,
        identifier = identifier?.let { LbcTextSpec.Raw(it) },
        actions = getQuickAction(id),
    )
}
