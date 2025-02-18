package studio.lunabee.onesafe.feature.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.PlatformParagraphStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette

@Composable
fun OthersCard(
    actions: List<OthersAction>,
    modifier: Modifier = Modifier,
) {
    OSCard(
        modifier = modifier,
    ) {
        actions.forEach { action ->

            val description = action.clickLabel?.annotated
            OSClickableRow(
                text = action.text,
                onClick = action.onClick,
                contentPadding = PaddingValues(
                    top = OSDimens.SystemSpacing.Regular,
                    bottom = OSDimens.SystemSpacing.Regular,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.ExtraSmall,
                ),
                modifier = Modifier.then(
                    if (description != null) {
                        Modifier.clearAndSetSemantics { text = description }
                    } else {
                        Modifier
                    },
                ),
                buttonColors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = action.icon),
                        contentDescription = null,
                    )
                },
                trailingText = action.count?.let { count ->
                    {
                        val countDescription =
                            pluralStringResource(id = OSPlurals.common_accessibility_elementCount, count = count, count)
                        @Suppress("DEPRECATION")
                        OSText(
                            text = LbcTextSpec.Raw(count.toString()),
                            style = MaterialTheme.typography.labelLarge.copy(
                                platformStyle = PlatformTextStyle(
                                    paragraphStyle = PlatformParagraphStyle(includeFontPadding = false),
                                    spanStyle = null,
                                ),
                            ),
                            modifier = Modifier
                                .semantics { contentDescription = countDescription },
                            color = LocalColorPalette.current.Neutral60,
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = OSDrawable.ic_navigate_next),
                        contentDescription = null,
                        tint = LocalColorPalette.current.Neutral30,
                    )
                },
                style = MaterialTheme.typography.bodyLarge,
                maxLines = MaxLines,
            )
        }
    }
}

private const val MaxLines: Int = 2
