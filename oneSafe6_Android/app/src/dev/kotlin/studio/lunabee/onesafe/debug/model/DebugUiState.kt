package studio.lunabee.onesafe.debug.model

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.model.AppIcon

data class DebugUiState(
    val isMaterialYouEnabled: Boolean,
    val autoBackupError: AutoBackupError?,
    val plainItem: DevPlainItem?,
    val itemsText: LbcTextSpec?,
    val itemOrder: ItemOrder,
    val itemLayout: ItemLayout,
    val cameraSystem: CameraSystem,
    val isSignUp: Boolean,
    val databaseEncryptionSettings: DebugDatabaseEncryptionSettings,
    val itemCount: Int,
    val mainDatabaseSize: Long,
    val appIcon: AppIcon,
    val safeInfoData: DebugSafeInfoData?,
) {
    companion object {
        val default: DebugUiState
            get() = DebugUiState(
                isMaterialYouEnabled = false,
                autoBackupError = null,
                plainItem = null,
                itemsText = null,
                itemOrder = ItemOrder.Alphabetic,
                itemLayout = ItemLayout.Grid,
                cameraSystem = CameraSystem.InApp,
                isSignUp = false,
                databaseEncryptionSettings = DebugDatabaseEncryptionSettings.Disabled,
                itemCount = 0,
                mainDatabaseSize = 0L,
                appIcon = AppIcon.Default,
                safeInfoData = null,
            )
    }
}
