package studio.lunabee.onesafe.organism.card.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
internal fun OSCardDescription(
    description: LbcTextSpec,
    modifier: Modifier = Modifier,
) {
    OSText(
        text = description,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .padding(horizontal = OSDimens.SystemSpacing.Regular),
    )
}
