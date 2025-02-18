package studio.lunabee.onesafe.commonui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.molecule.OSRowLabel
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette

abstract class CardSettingsNavAction(
    @DrawableRes private val icon: Int?,
    protected val text: LbcTextSpec,
    private val clickLabel: LbcTextSpec = text,
    private val onClickLabel: LbcTextSpec? = LbcTextSpec.StringResource(OSString.common_navigate),
    private val secondaryText: LbcTextSpec? = null,
) : SettingsAction {
    abstract val onClick: () -> Unit
    open val state: OSActionState = OSActionState.Enabled

    @Composable
    fun Texts(state: OSActionState, modifier: Modifier = Modifier) {
        OSRowLabel(
            text = text,
            secondaryText = secondaryText,
            fontText = MaterialTheme.typography.bodyLarge.fontFamily, // TODO useless?
            state = state,
            modifier = modifier,
        )
    }

    @Composable
    open fun Label(modifier: Modifier) {
        Texts(state = OSActionState.Enabled, modifier = modifier)
    }

    @Composable
    override fun Composable() {
        val contentDescription = StringBuilder(clickLabel.annotated)
        secondaryText?.annotated?.let {
            contentDescription.append(" ")
            contentDescription.append(it)
        }

        OSRow(
            label = { Label(it) },
            modifier = Modifier
                .clickable(onClick = onClick, onClickLabel = onClickLabel?.string)
                .clearAndSetSemantics {
                    this.text = AnnotatedString(contentDescription.toString())
                }
                .padding(
                    PaddingValues(
                        top = OSDimens.SystemSpacing.Regular,
                        bottom = OSDimens.SystemSpacing.Regular,
                        start = OSDimens.SystemSpacing.Regular,
                        end = OSDimens.SystemSpacing.ExtraSmall,
                    ),
                ),
            startContent = if (icon != null) {
                {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                    )
                }
            } else {
                null
            },
            endContent = {
                Icon(
                    painter = painterResource(id = OSDrawable.ic_navigate_next),
                    tint = LocalColorPalette.current.Neutral30,
                    contentDescription = null,
                )
            },
            state = state,
        )
    }
}
