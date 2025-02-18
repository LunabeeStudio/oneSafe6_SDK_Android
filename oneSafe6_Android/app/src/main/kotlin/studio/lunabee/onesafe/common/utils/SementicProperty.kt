package studio.lunabee.onesafe.common.utils

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

val DrawableIdProperty: SemanticsPropertyKey<Int> = SemanticsPropertyKey("DrawableResId")
var SemanticsPropertyReceiver.drawableId: Int by DrawableIdProperty
