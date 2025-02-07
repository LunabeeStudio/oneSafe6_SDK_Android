package studio.lunabee.onesafe.organism.card.scope

import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@LayoutScopeMarker
@Immutable
interface OSCardActionScope {
    @Stable
    fun Modifier.minTouchVerticalButtonOffset(buttonHeight: Dp = ButtonDefaults.MinHeight): Modifier
}
