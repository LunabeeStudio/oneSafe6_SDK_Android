package studio.lunabee.onesafe.organism.card.scope.impl

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import studio.lunabee.onesafe.organism.card.scope.OSCardActionScope
import studio.lunabee.onesafe.ui.res.OSDimens

object OSCardActionScopeInstance : OSCardActionScope {
    override fun Modifier.minTouchVerticalButtonOffset(buttonHeight: Dp): Modifier = composed {
        val minTouchPaddingOffset = OSDimens.SystemButton.minTouchPaddingOffset(
            targetSpacing = OSDimens.SystemSpacing.Regular,
            buttonHeight = buttonHeight,
        )
        padding(vertical = minTouchPaddingOffset)
    }
}
