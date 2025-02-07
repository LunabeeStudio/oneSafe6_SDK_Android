package studio.lunabee.onesafe.model

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.ui.res.OSDimens

sealed interface OSGeneratedPasswordOption {
    @Composable
    fun Content(paddingValues: PaddingValues)

    class Primary(
        val icon: OSImageSpec,
        val contentDescription: LbcTextSpec?,
        val onClick: () -> Unit,
    ) : OSGeneratedPasswordOption {

        @Composable
        override fun Content(paddingValues: PaddingValues) {
            OSIconButton(
                image = icon,
                onClick = onClick,
                contentDescription = contentDescription,
                buttonSize = OSDimens.SystemButtonDimension.Action,
                colors = OSIconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}
