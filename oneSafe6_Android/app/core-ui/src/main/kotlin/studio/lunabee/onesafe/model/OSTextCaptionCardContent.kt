package studio.lunabee.onesafe.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.res.OSDimens

class OSTextCaptionCardContent(
    private val text: LbcTextSpec,
    override val key: Any,
) : OSLazyCardContent.Item {
    override val contentType: Any = "ContactItem"

    @Composable
    override fun Content(padding: PaddingValues, modifier: Modifier) {
        OSText(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
        )
    }
}
