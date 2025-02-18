package studio.lunabee.onesafe.feature.itemfielddetail.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import studio.lunabee.compose.core.LbcTextSpec

@Stable
sealed interface ItemFieldDetailsState {
    object Initializing : ItemFieldDetailsState

    data class Data(
        val color: Color?,
        val fieldName: LbcTextSpec,
        val fieldValue: LbcTextSpec,
        val screenType: ScreenType,
    ) : ItemFieldDetailsState {
        enum class ScreenType {
            PAGER, TEXT
        }
    }

    data class Error(
        val error: Throwable?,
    ) : ItemFieldDetailsState
}
