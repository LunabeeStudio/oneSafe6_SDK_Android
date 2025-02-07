package studio.lunabee.onesafe.feature.search.composable.cardcontent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.model.OSLazyCardContent
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

class RecentSearchCardContent(
    private val value: LbcTextSpec,
    private val onClick: () -> Unit,
    override val key: Any,
) : OSLazyCardContent.Item {
    override val contentType: Any = "recentSearch"

    @Composable
    override fun Content(padding: PaddingValues, modifier: Modifier) {
        OSRow(
            text = value,
            modifier = Modifier
                .testTag(UiConstants.TestTag.Item.RecentSearchItem)
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(padding)
                .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.ExtraSmall),
        )
    }
}
