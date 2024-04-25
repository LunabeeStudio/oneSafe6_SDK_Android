package studio.lunabee.onesafe.help.debug.model

import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.ItemsLayoutSettings
import studio.lunabee.onesafe.importexport.model.AutoBackupError

internal data class HelpDebugUiState(
    val isMaterialYouEnabled: Boolean,
    val autoBackupError: AutoBackupError?,
    val itemOrder: ItemOrder,
    val itemsLayoutSetting: ItemsLayoutSettings,
    val cameraSystem: CameraSystem,
    val isSignUp: Boolean,
    val databaseEncryptionSettings: DebugDatabaseEncryptionSettings,
    val mainDatabaseSize: Long,
) {
    companion object {
        val default: HelpDebugUiState
            get() = HelpDebugUiState(
                isMaterialYouEnabled = false,
                autoBackupError = null,
                itemOrder = ItemOrder.Alphabetic,
                itemsLayoutSetting = ItemsLayoutSettings.Grid,
                cameraSystem = CameraSystem.InApp,
                isSignUp = false,
                databaseEncryptionSettings = DebugDatabaseEncryptionSettings.Disabled,
                mainDatabaseSize = 0L,
            )
    }
}
