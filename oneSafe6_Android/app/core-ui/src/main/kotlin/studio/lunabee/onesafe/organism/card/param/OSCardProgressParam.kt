package studio.lunabee.onesafe.organism.card.param

import studio.lunabee.compose.core.LbcTextSpec

sealed class OSCardProgressParam(
    val progress: Float?,
    val progressDescription: LbcTextSpec? = null,
) {
    class DeterminedProgress(
        progress: Float,
        progressDescription: LbcTextSpec? = null,
    ) : OSCardProgressParam(progress = progress, progressDescription = progressDescription)

    class UndeterminedProgress(
        progressDescription: LbcTextSpec? = null,
    ) : OSCardProgressParam(progress = null, progressDescription = progressDescription)
}
