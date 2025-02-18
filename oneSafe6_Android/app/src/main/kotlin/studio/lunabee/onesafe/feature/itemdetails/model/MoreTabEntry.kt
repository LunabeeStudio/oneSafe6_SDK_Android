package studio.lunabee.onesafe.feature.itemdetails.model

import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

@Stable
sealed class MoreTabEntry(
    open val value: LbcTextSpec,
) {
    abstract val id: String

    abstract val label: LbcTextSpec

    @Stable
    data class CreatedAt(override val value: LbcTextSpec) : MoreTabEntry(value) {
        override val id: String = "created_at"
        override val label: LbcTextSpec = LbcTextSpec.StringResource(
            OSString.safeItemDetail_contentCard_more_label_creationDate,
        )
    }

    @Stable
    data class UpdatedAt(override val value: LbcTextSpec) : MoreTabEntry(value) {
        override val id: String = "updated_at"
        override val label: LbcTextSpec = LbcTextSpec.StringResource(
            OSString.safeItemDetail_contentCard_more_label_updateAt,
        )
    }

    @Stable
    data class DeletedAt(override val value: LbcTextSpec) : MoreTabEntry(value) {
        override val id: String = "deleted_at"
        override val label: LbcTextSpec = LbcTextSpec.StringResource(
            OSString.safeItemDetail_contentCard_more_label_deletionDate,
        )
    }
}
