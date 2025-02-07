package studio.lunabee.onesafe.help.lostkey

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.CardSettingsButtonAction

internal class CardSettingsAccessLocalBackup(onClick: () -> Unit) : CardSettingsButtonAction(
    onClick = onClick,
    icon = OSDrawable.ic_phone,
    text = LbcTextSpec.StringResource(OSString.lostKey_accessBackupCard_localSaves),
)

internal class CardSettingsAccessRemoteBackup(onClick: () -> Unit) : CardSettingsButtonAction(
    onClick = onClick,
    icon = OSDrawable.ic_cloud,
    text = LbcTextSpec.StringResource(OSString.lostKey_accessBackupCard_googleDriveSaves),
)

internal class CardSettingsAccessImportFile(onClick: () -> Unit) : CardSettingsButtonAction(
    onClick = onClick,
    icon = OSDrawable.ic_upload,
    text = LbcTextSpec.StringResource(OSString.lostKey_accessBackupCard_importFile),
)
