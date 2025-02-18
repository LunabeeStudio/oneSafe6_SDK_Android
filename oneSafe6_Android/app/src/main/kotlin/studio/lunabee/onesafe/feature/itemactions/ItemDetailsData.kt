package studio.lunabee.onesafe.feature.itemactions

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.itemdetails.model.MoreTabEntry
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

sealed class ItemDetailsData(
    val nameResult: LBResult<String>?,
    val iconResult: LBResult<ByteArray>?,
    private val updateAt: Instant,
    val isFavorite: Boolean,
    val colorResult: LBResult<String>?,
    private val createdAt: Instant,
) {
    protected val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)

    val hasCorruptedData: Boolean
        get() = listOf(nameResult, iconResult, colorResult).any { it is LBResult.Failure }

    open val moreTabEntries: List<MoreTabEntry>
        get() {
            val zoneId = ZoneId.systemDefault()
            val formattedUpdateAt = dateTimeFormatter.format(ZonedDateTime.ofInstant(updateAt, zoneId))
            val formattedCreatedAt = dateTimeFormatter.format(ZonedDateTime.ofInstant(createdAt, zoneId))
            return listOf(
                MoreTabEntry.CreatedAt(LbcTextSpec.Raw(formattedCreatedAt)),
                MoreTabEntry.UpdatedAt(LbcTextSpec.Raw(formattedUpdateAt)),
            )
        }

    class Default(
        nameResult: LBResult<String>?,
        iconResult: LBResult<ByteArray>?,
        updateAt: Instant,
        isFavorite: Boolean,
        colorResult: LBResult<String>?,
        createdAt: Instant,
    ) : ItemDetailsData(nameResult, iconResult, updateAt, isFavorite, colorResult, createdAt)

    @Suppress("LongParameterList")
    class Deleted(
        nameResult: LBResult<String>?,
        iconResult: LBResult<ByteArray>?,
        updateAt: Instant,
        isFavorite: Boolean,
        colorResult: LBResult<String>?,
        private val deletedAt: Instant,
        val daysBeforeRemove: Int,
        createdAt: Instant,
    ) : ItemDetailsData(nameResult, iconResult, updateAt, isFavorite, colorResult, createdAt) {
        override val moreTabEntries: List<MoreTabEntry>
            get() = super.moreTabEntries + dateTimeFormatter.format(
                ZonedDateTime.ofInstant(deletedAt, ZoneId.systemDefault()),
            ).let {
                MoreTabEntry.DeletedAt(LbcTextSpec.Raw(it))
            }
    }

    companion object {
        suspend fun fromSafeItem(
            item: SafeItem,
            decryptUseCase: ItemDecryptUseCase,
            getIconUseCase: GetIconUseCase,
        ): ItemDetailsData {
            val name = item.encName?.let { decryptUseCase(it, item.id, String::class) }
            val icon = item.iconId?.let { getIconUseCase(it, item.id) }
            val color = item.encColor?.let { decryptUseCase(it, item.id, String::class) }

            return if (item.isDeleted) {
                Deleted(
                    nameResult = name,
                    iconResult = icon,
                    updateAt = item.updatedAt,
                    isFavorite = item.isFavorite,
                    colorResult = color,
                    deletedAt = item.deletedAt!!,
                    daysBeforeRemove = item.daysBeforeRemove()!!,
                    createdAt = item.createdAt,
                )
            } else {
                Default(
                    nameResult = name,
                    iconResult = icon,
                    updateAt = item.updatedAt,
                    isFavorite = item.isFavorite,
                    colorResult = color,
                    createdAt = item.createdAt,
                )
            }
        }
    }
}
