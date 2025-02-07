package studio.lunabee.onesafe.feature.move

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import java.util.UUID

data class MoveDestinationUiData(
    val id: UUID?,
    val label: LbcTextSpec,
) {
    companion object {
        fun home(): MoveDestinationUiData = MoveDestinationUiData(
            id = null,
            label = LbcTextSpec.StringResource(OSString.common_home),
        )
    }
}
