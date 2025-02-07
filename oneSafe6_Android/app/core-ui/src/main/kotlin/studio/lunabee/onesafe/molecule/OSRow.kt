package studio.lunabee.onesafe.molecule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.text.OSRowLabel
import studio.lunabee.onesafe.atom.text.OSRowSecondaryText
import studio.lunabee.onesafe.atom.text.OSRowText
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

/**
 * @see <a href="https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System?node-id=225-9231&t=KPS0asR7DsNc40nc-4"
 * >List row component</a>
 */
@Composable
fun OSRow(
    label: @Composable ((modifier: Modifier) -> Unit),
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(OSDimens.Row.InnerSpacing),
    startContent: @Composable (() -> Unit)? = null,
    endContent: @Composable (RowScope.() -> Unit)? = null,
    state: OSActionState = OSActionState.Enabled,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (state == OSActionState.Enabled) {
                LocalContentColor.current
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = UiConstants.GoogleInternalApi.DisabledContainerAlpha)
            },
        ) {
            startContent?.let { startContent -> startContent() }
            label(Modifier.weight(1f))
            endContent?.let { endContent -> endContent() }
        }
    }
}

@Composable
fun OSRow(
    text: LbcTextSpec,
    modifier: Modifier = Modifier,
    label: LbcTextSpec? = null,
    secondaryText: LbcTextSpec? = null,
    contentDescription: String? = null,
    fontText: FontFamily? = null,
    textMaxLines: Int = UiConstants.Text.MaxLineSize,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(OSDimens.Row.InnerSpacing),
    state: OSActionState = OSActionState.Enabled,
    startContent: @Composable (() -> Unit)? = null,
    endContent: @Composable (RowScope.() -> Unit)? = null,
) {
    OSRow(
        label = {
            OSRowLabel(text, it, contentDescription, label, secondaryText, fontText, textMaxLines, state)
        },
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        startContent = startContent,
        endContent = endContent,
    )
}

@Composable
fun OSRowLabel(
    text: LbcTextSpec,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    label: LbcTextSpec? = null,
    secondaryText: LbcTextSpec? = null,
    fontText: FontFamily? = null,
    textMaxLines: Int = UiConstants.Text.MaxLineSize,
    state: OSActionState = OSActionState.Enabled,
) {
    val textsModifier = if (contentDescription != null) {
        modifier.clearAndSetSemantics { this.text = AnnotatedString(contentDescription) }
    } else {
        modifier.accessibilityMergeDescendants()
    }

    fun Color.withState(): Color = if (state != OSActionState.Enabled) {
        this.copy(alpha = UiConstants.GoogleInternalApi.DisabledContentAlpha)
    } else {
        this
    }

    Column(
        modifier = textsModifier,
    ) {
        label?.let { label ->
            OSRowLabel(
                text = label,
                color = LocalDesignSystem.current.rowLabelColor.withState(),
            )
        }
        OSRowText(
            fontFamily = fontText,
            text = text,
            maxLines = textMaxLines,
            color = LocalDesignSystem.current.rowTextColor.withState(),
        )
        secondaryText?.let { secondaryText ->
            OSRowSecondaryText(
                text = secondaryText,
                color = LocalDesignSystem.current.rowSecondaryColor.withState(),
                maxLines = textMaxLines,
            )
        }
    }
}

@Composable
@OsDefaultPreview
private fun OSRowPreview() {
    OSPreviewOnSurfaceTheme {
        Column {
            OSRow(
                text = loremIpsumSpec(3),
                modifier = Modifier.clickable { },
                label = loremIpsumSpec(2),
                secondaryText = loremIpsumSpec(4),
                startContent = { Icon(painterResource(id = R.drawable.os_ic_sample), contentDescription = null) },
                endContent = {
                    Icon(painterResource(id = R.drawable.os_ic_sample), contentDescription = null)
                    IconButton(onClick = {}) {
                        Icon(painterResource(id = R.drawable.os_ic_sample), contentDescription = "key")
                    }
                },
            )
            OSRow(
                text = loremIpsumSpec(3),
                modifier = Modifier.clickable { },
                label = loremIpsumSpec(2),
                secondaryText = loremIpsumSpec(4),
                state = OSActionState.DisabledWithAction,
                startContent = { Icon(painterResource(id = R.drawable.os_ic_sample), contentDescription = null) },
                endContent = {
                    Icon(painterResource(id = R.drawable.os_ic_sample), contentDescription = null)
                    IconButton(onClick = {}) {
                        Icon(painterResource(id = R.drawable.os_ic_sample), contentDescription = "key")
                    }
                },
            )
        }
    }
}

@Composable
@OsDefaultPreview
private fun OSRowTextOnlyPreview() {
    OSPreviewOnSurfaceTheme {
        OSRow(
            text = loremIpsumSpec(3),
            modifier = Modifier.clickable { },
        )
    }
}
