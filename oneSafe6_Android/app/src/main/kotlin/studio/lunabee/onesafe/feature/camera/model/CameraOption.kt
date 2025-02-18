package studio.lunabee.onesafe.feature.camera.model

import androidx.annotation.DrawableRes
import androidx.camera.extensions.ExtensionMode
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString

enum class CameraOption(
    @DrawableRes val drawableRes: Int,
    val title: LbcTextSpec,
    val extension: Int,
) {
    NormalMode(
        OSDrawable.ic_settings,
        LbcTextSpec.StringResource(OSString.camera_mode_normal),
        ExtensionMode.NONE,
    ),
    NightMode(
        OSDrawable.ic_moon,
        LbcTextSpec.StringResource(OSString.camera_mode_night),
        ExtensionMode.NIGHT,
    ),
    HDRMode(
        OSDrawable.ic_hdr,
        LbcTextSpec.StringResource(OSString.camera_mode_hdr),
        ExtensionMode.HDR,
    ),
    PortraitMode(
        OSDrawable.ic_person,
        LbcTextSpec.StringResource(OSString.camera_mode_portrait),
        ExtensionMode.BOKEH,
    ),
}
