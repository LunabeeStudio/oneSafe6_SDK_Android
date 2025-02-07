package studio.lunabee.onesafe.molecule.tabs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTypography

open class TabsData(
    private val title: LbcTextSpec,
    private val contentDescription: LbcTextSpec?,
) {

    @Composable
    open fun Composable(
        index: Int,
        selectedTabIndex: Int,
        isEnabled: Boolean,
    ) {
        val titleDescription = contentDescription?.string
        val textModifier = if (titleDescription != null) {
            Modifier.semantics { contentDescription = titleDescription }
        } else {
            Modifier
        }

        OSText(
            modifier = textModifier
                .padding(OSDimens.SystemSpacing.Regular),
            text = title,
            style = OSTypography.Typography.labelLarge,
            color = when {
                isEnabled -> Color.Unspecified
                index == selectedTabIndex -> LocalDesignSystem.current.tabPrimaryDisabledColor
                else -> LocalColorPalette.current.Neutral30
            },
        )
    }
}
