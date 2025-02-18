package studio.lunabee.onesafe.organism.card.param

import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.Dp

data class OSCardImageParam(
    @DrawableRes val imageRes: Int,
    val offset: Dp? = null,
)
