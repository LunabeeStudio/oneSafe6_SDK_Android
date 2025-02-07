package studio.lunabee.onesafe.feature.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogAction
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState

/**
 * Implementation of [ErrorDialogState] which shows an open Discord action and an exit button to kill the app
 */
class FatalErrorDialogState(context: Context, error: Throwable?, private val closeDialog: () -> Unit) : ErrorDialogState(
    error = error,
    actions = listOf(
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.finishSetupDatabase_canceledDialog_button_discord),
            onClick = {
                closeDialog()
                val discordIntent = Intent(Intent.ACTION_VIEW, Uri.parse(CommonUiConstants.ExternalLink.Discord))
                context.startActivity(discordIntent)
            },
        ),
        DialogAction(
            text = LbcTextSpec.StringResource(OSString.common_exit),
            onClick = {
                closeDialog()
                (context as? Activity)?.finish()
                Runtime.getRuntime().exit(0)
            },
        ),
    ),
)
