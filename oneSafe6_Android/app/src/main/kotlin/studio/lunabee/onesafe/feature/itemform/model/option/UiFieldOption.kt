package studio.lunabee.onesafe.feature.itemform.model.option

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec

interface UiFieldOption {
    val clickLabel: LbcTextSpec?

    fun onClick()

    @Composable
    fun ComposableLayout(modifier: Modifier)
}
