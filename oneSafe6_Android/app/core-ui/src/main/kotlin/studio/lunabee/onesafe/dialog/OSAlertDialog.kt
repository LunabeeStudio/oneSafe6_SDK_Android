package studio.lunabee.onesafe.dialog

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSDefaultAlertDialog(
    onDismissRequest: () -> Unit,
    title: LbcTextSpec,
    message: LbcTextSpec?,
    modifier: Modifier = Modifier,
    actionContent: (@Composable () -> Unit)? = null,
    isCancellable: Boolean = true,
    additionalContent: (@Composable () -> Unit)? = null,
) {
    OSAlertDialogBase(
        onDismissRequest = onDismissRequest,
        title = title,
        modifier = modifier,
        actions = actionContent,
        isCancellable = isCancellable,
    ) {
        message?.let {
            OSText(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        additionalContent?.invoke()
    }
}

@Composable
fun OSAlertDialogBase(
    onDismissRequest: () -> Unit,
    title: LbcTextSpec,
    modifier: Modifier = Modifier,
    actions: (@Composable () -> Unit)? = null,
    isCancellable: Boolean = true,
    content: (@Composable ColumnScope.() -> Unit),
) {
    OSDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = isCancellable,
            dismissOnClickOutside = isCancellable,
        ),
    ) {
        OSDialogContent(title, content, modifier, actions)
    }
}

@OsDefaultPreview
@Composable
private fun OSAlertDialogThreeActionsPreview() {
    OSTheme {
        OSDefaultAlertDialog(
            onDismissRequest = { },
            title = loremIpsumSpec(4),
            message = loremIpsumSpec(10),
            actionContent = {
                OSTextButton(
                    text = loremIpsumSpec(4),
                    onClick = { },
                )

                OSTextButton(
                    text = loremIpsumSpec(4),
                    onClick = { },
                )

                OSTextButton(
                    text = loremIpsumSpec(4),
                    onClick = { },
                )
            },
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSAlertDialogDefaultPreview() {
    OSTheme {
        OSDefaultAlertDialog(
            onDismissRequest = { },
            title = loremIpsumSpec(4),
            message = loremIpsumSpec(10),
            actionContent = {
                OSTextButton(
                    text = loremIpsumSpec(4),
                    onClick = { },
                )
                OSFilledButton(
                    text = loremIpsumSpec(4),
                    onClick = { },
                )
            },
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSAlertDialogNoActionPreview() {
    OSTheme {
        OSDefaultAlertDialog(
            onDismissRequest = { },
            title = loremIpsumSpec(4),
            message = loremIpsumSpec(10),
        )
    }
}
