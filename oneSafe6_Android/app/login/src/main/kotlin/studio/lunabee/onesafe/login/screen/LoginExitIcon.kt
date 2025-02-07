package studio.lunabee.onesafe.login.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

interface LoginExitIcon {
    @Composable
    fun Content(
        modifier: Modifier,
    )

    object None : LoginExitIcon {
        @Composable
        override fun Content(modifier: Modifier) {
            /* no-op */
        }
    }

    class Close(private val onClick: () -> Unit) : LoginExitIcon {
        @Composable
        override fun Content(modifier: Modifier) {
            OSIconButton(
                image = OSImageSpec.Drawable(OSDrawable.ic_close),
                onClick = onClick,
                buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
                colors = OSIconButtonDefaults.iconButtonColors(
                    containerColor = LocalDesignSystem.current.bubblesSecondaryContainer(),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    state = OSActionState.Enabled,
                ),
                modifier = modifier,
            )
        }
    }
}
