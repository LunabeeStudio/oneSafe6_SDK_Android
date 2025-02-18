package studio.lunabee.onesafe.feature.settings.personalization

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.camera.CameraSystem

enum class UiCameraSystem(
    val title: LbcTextSpec,
    val description: LbcTextSpec,
    val cameraSystem: CameraSystem,
    val imageSpec: OSImageSpec,
) {
    InApp(
        title = LbcTextSpec.StringResource(OSString.settings_personalization_cameraSystem_inApp_title),
        description = LbcTextSpec.StringResource(OSString.settings_personalization_cameraSystem_inApp_description),
        cameraSystem = CameraSystem.InApp,
        imageSpec = OSImageSpec.Drawable(drawable = OSDrawable.ic_onesafe_logo, isIcon = false),
    ),
    External(
        title = LbcTextSpec.StringResource(OSString.settings_personalization_cameraSystem_system_title),
        description = LbcTextSpec.StringResource(OSString.settings_personalization_cameraSystem_system_description),
        cameraSystem = CameraSystem.External,
        imageSpec = OSImageSpec.Drawable(drawable = OSDrawable.ic_phone, isIcon = true),
    ),
}
