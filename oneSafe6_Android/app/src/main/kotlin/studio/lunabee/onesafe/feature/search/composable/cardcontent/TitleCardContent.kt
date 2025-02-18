package studio.lunabee.onesafe.feature.search.composable.cardcontent

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.ui.res.OSDimens

class TitleCardContent(
    private val title: LbcTextSpec,
    override val key: Any?,
) : OSLazyCardContent.Item {
    override val contentType: Any = "title"

    @Composable
    override fun Content(padding: PaddingValues, modifier: Modifier) {
        OSText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
        )
    }
}
