package studio.lunabee.onesafe.organism.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSCardTitle
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.organism.card.component.OSCardAction
import studio.lunabee.onesafe.organism.card.component.OSCardDescription
import studio.lunabee.onesafe.organism.card.component.OSCardGlobalAction
import studio.lunabee.onesafe.organism.card.scope.OSCardActionScope
import studio.lunabee.onesafe.ui.res.OSColorValue
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSMessageCard(
    description: LbcTextSpec,
    modifier: Modifier = Modifier,
    title: LbcTextSpec? = null,
    contentAlignment: Alignment = Alignment.Center,
    attributes: OSMessageCardAttributes = OSMessageCardAttributes(),
    action: (@Composable OSCardActionScope.(padding: PaddingValues) -> Unit)? = null,
) {
    val (contentColor, color) = when (attributes.style) {
        OSMessageCardStyle.Default -> Color.Unspecified to MaterialTheme.colorScheme.surface
        OSMessageCardStyle.Alert -> Color.Unspecified to MaterialTheme.colorScheme.errorContainer
        OSMessageCardStyle.Feedback -> LocalDesignSystem.current.feedbackWarningBackgroundGradient().first to Color.Transparent
    }
    val shape = CardDefaults.shape
    var cardModifier: Modifier = Modifier
    if (attributes.style == OSMessageCardStyle.Feedback) {
        cardModifier = cardModifier
            .clip(shape)
            .background(LocalDesignSystem.current.feedbackWarningBackgroundGradient().second)
    }

    cardModifier = cardModifier.then(modifier)

    attributes.globalAction?.let { globalAction ->
        cardModifier = cardModifier.clickable(onClick = globalAction.onClick)
    }

    OSCard(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(color),
        shape = shape,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (contentColor.isSpecified) {
                contentColor
            } else {
                LocalContentColor.current
            },
        ) {
            OSRegularSpacer()
            val showDismiss = attributes.dismissAction != null && attributes.dismissIcon != null
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    Modifier
                        .weight(1f),
                ) {
                    title?.let {
                        OSCardTitle(it)
                    }
                }
                if (showDismiss) {
                    val colors = when (attributes.style) {
                        OSMessageCardStyle.Default -> OSIconButtonDefaults.secondaryIconButtonColors()
                        OSMessageCardStyle.Alert -> ButtonDefaults.buttonColors(MaterialTheme.colorScheme.onErrorContainer)
                        OSMessageCardStyle.Feedback -> ButtonDefaults.buttonColors(
                            containerColor = contentColor,
                            contentColor = OSColorValue.Red35,
                        )
                    }
                    OSIconButton(
                        image = attributes.dismissIcon!!,
                        onClick = attributes.dismissAction!!,
                        buttonSize = OSDimens.SystemButtonDimension.FloatingAction,
                        colors = colors,
                        modifier = Modifier
                            .padding(horizontal = OSDimens.SystemSpacing.Regular),
                        contentDescription = attributes.dismissContentDescription,
                    )
                }
            }
            if (title != null || showDismiss) {
                OSRegularSpacer()
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OSCardDescription(
                    description = description,
                    modifier = Modifier.weight(1f),
                )
                attributes.globalAction?.icon?.invoke(
                    Modifier
                        .padding(end = OSDimens.SystemSpacing.Regular),
                )
            }
            if (action != null) {
                OSCardAction(
                    contentAlignment = contentAlignment,
                    action = action,
                )
            } else {
                OSRegularSpacer()
            }
        }
    }
}

class OSMessageCardAttributes {
    var dismissAction: (() -> Unit)? = null
        private set
    var dismissIcon: OSImageSpec? = null
        private set
    var dismissContentDescription: LbcTextSpec? = null
        private set
    var style: OSMessageCardStyle = OSMessageCardStyle.Default
        private set
    var globalAction: OSCardGlobalAction? = null
        private set

    fun dismissible(icon: OSImageSpec, contentDescription: LbcTextSpec, onDismiss: () -> Unit): OSMessageCardAttributes = apply {
        this.dismissAction = onDismiss
        this.dismissIcon = icon
        this.dismissContentDescription = contentDescription
    }

    fun style(style: OSMessageCardStyle): OSMessageCardAttributes = apply {
        this.style = style
    }

    fun clickable(action: OSCardGlobalAction?): OSMessageCardAttributes = apply {
        this.globalAction = action
    }
}

enum class OSMessageCardStyle {
    Default,
    Alert,
    Feedback,
}

@OsDefaultPreview
@Composable
private fun OSMessageCardWithPrimaryActionPreview() {
    OSTheme {
        OSMessageCard(
            title = loremIpsumSpec(1),
            description = loremIpsumSpec(10),
        ) {
            OSFilledButton(
                text = loremIpsumSpec(1),
                onClick = { },
                modifier = Modifier.minTouchVerticalButtonOffset(),
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun OSMessageCardPreview() {
    OSTheme {
        OSMessageCard(
            description = loremIpsumSpec(10),
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSMessageCardWithSecondaryActionCardPreview() {
    OSTheme {
        OSMessageCard(
            description = loremIpsumSpec(10),
        ) {
            OSTextButton(
                text = loremIpsumSpec(4),
                onClick = { },
                modifier = Modifier.minTouchVerticalButtonOffset(),
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun OSMessageCardWithSecondaryActionAlertCardPreview() {
    OSTheme {
        OSMessageCard(
            description = loremIpsumSpec(10),
            attributes = OSMessageCardAttributes()
                .dismissible(OSImageSpec.Drawable(R.drawable.os_ic_check), loremIpsumSpec(2)) {}
                .style(OSMessageCardStyle.Alert),
        ) {
            OSTextButton(
                text = loremIpsumSpec(4),
                onClick = { },
                modifier = Modifier.minTouchVerticalButtonOffset(),
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun OSMessageCardWithTitlePreview() {
    OSTheme {
        OSMessageCard(
            title = loremIpsumSpec(1),
            description = loremIpsumSpec(10),
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSMessageCardWithTitleAndDismissPreview() {
    OSTheme {
        OSMessageCard(
            title = loremIpsumSpec(1),
            description = loremIpsumSpec(10),
            attributes = OSMessageCardAttributes()
                .dismissible(OSImageSpec.Drawable(R.drawable.os_ic_check), loremIpsumSpec(2)) {},
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSMessageCardDismissPreview() {
    OSTheme {
        OSMessageCard(
            description = loremIpsumSpec(10),
            attributes = OSMessageCardAttributes()
                .dismissible(OSImageSpec.Drawable(R.drawable.os_ic_check), loremIpsumSpec(2)) {}
                .clickable(OSCardGlobalAction.Navigation {}),
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSMessageCardFeedbackPreview() {
    OSTheme {
        OSMessageCard(
            title = loremIpsumSpec(2),
            description = loremIpsumSpec(10),
            attributes = OSMessageCardAttributes()
                .dismissible(OSImageSpec.Drawable(R.drawable.os_ic_check), loremIpsumSpec(2)) {}
                .style(OSMessageCardStyle.Feedback)
                .clickable(OSCardGlobalAction.Navigation {}),
        )
    }
}
