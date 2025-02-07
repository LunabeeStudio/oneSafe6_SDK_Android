package studio.lunabee.onesafe.debug.model

import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId

data class DebugSafeInfoData(
    val safeCryptos: List<SafeCrypto>,
    val currentSafeId: SafeId?,
    val allSafeId: List<SafeId>,
    val fileCount: Pair<Int, Int>,
    val iconCount: Pair<Int, Int>,
    val switchSafe: (SafeId?) -> Unit,
    val createSafe: () -> Unit,
    val deleteSafe: (() -> Unit)?,
    val deleteAllItems: (() -> Unit)?,
)
