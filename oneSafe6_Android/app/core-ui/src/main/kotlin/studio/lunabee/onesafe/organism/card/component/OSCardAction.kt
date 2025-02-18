package studio.lunabee.onesafe.organism.card.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.organism.card.scope.OSCardActionScope
import studio.lunabee.onesafe.organism.card.scope.impl.OSCardActionScopeInstance
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
internal fun OSCardAction(
    contentAlignment: Alignment,
    action: @Composable (OSCardActionScope.(padding: PaddingValues) -> Unit),
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = contentAlignment,
        content = {
            OSCardActionScopeInstance.action(PaddingValues(horizontal = OSDimens.SystemSpacing.Regular))
        },
    )
}
