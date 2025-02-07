package studio.lunabee.onesafe.help.debug.model

import studio.lunabee.onesafe.domain.model.camera.CameraSystem

internal data class HelpDebugUiState(
    val isMaterialYouEnabled: Boolean,
    val cameraSystem: CameraSystem,
    val isSignUp: Boolean,
    val databaseEncryptionSettings: DebugDatabaseEncryptionSettings,
    val mainDatabaseSize: Long,
) {
    companion object {
        val default: HelpDebugUiState
            get() = HelpDebugUiState(
                isMaterialYouEnabled = false,
                cameraSystem = CameraSystem.InApp,
                isSignUp = false,
                databaseEncryptionSettings = DebugDatabaseEncryptionSettings.Disabled,
                mainDatabaseSize = 0L,
            )
    }
}
