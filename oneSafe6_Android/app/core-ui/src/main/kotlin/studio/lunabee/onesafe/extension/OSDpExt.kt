package studio.lunabee.onesafe.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val Dp.nonScaledSp: TextUnit
    @Composable
    get() = (this.value / LocalDensity.current.fontScale).sp
