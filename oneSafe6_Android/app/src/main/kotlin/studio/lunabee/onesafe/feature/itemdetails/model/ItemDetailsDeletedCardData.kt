package studio.lunabee.onesafe.feature.itemdetails.model

import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec

@Stable
data class ItemDetailsDeletedCardData(
    val message: LbcTextSpec,
    val action: LbcTextSpec,
    val onClick: () -> Unit,
)
