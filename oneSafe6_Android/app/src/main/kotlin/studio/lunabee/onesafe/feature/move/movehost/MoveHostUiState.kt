package studio.lunabee.onesafe.feature.move.movehost

import androidx.compose.runtime.Stable
import java.util.UUID

@Stable
data class MoveHostUiState(
    val itemToMoveId: UUID? = null,
    val itemToMoveName: String = "",
    val initialParentId: UUID? = null,
)
