package studio.lunabee.onesafe.debug.model

import studio.lunabee.compose.core.LbcTextSpec

class Feedback(val text: LbcTextSpec, val reset: () -> Unit)
