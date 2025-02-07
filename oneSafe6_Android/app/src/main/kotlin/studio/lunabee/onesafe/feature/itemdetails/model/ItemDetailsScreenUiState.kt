package studio.lunabee.onesafe.feature.itemdetails.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry.InformationTabEntry
import studio.lunabee.onesafe.model.OSItemIllustration

@Stable
sealed interface ItemDetailsScreenUiState {
    data object Initializing : ItemDetailsScreenUiState

    @Stable
    sealed interface Data : ItemDetailsScreenUiState {
        val itemNameProvider: OSNameProvider
        val icon: OSItemIllustration
        val tabs: LinkedHashSet<ItemDetailsTab>
        val informationTab: List<InformationTabEntry>
        val moreTab: List<MoreTabEntry>
        val children: Flow<PagingData<PlainItemData>>
        val childrenCount: Int
        val actions: List<SafeItemAction>
        val color: Color?
        val initialTab: ItemDetailsTab
        val isCorrupted: Boolean
        val notSupportedKindsList: List<SafeItemFieldKind.Unknown>?
        val shouldShowEditTips: Boolean

        val emptyElementsText: LbcTextSpec
        val emptyFieldsText: LbcTextSpec
        val corruptedCardData: ItemDetailsCorruptedCardData?

        data class Default(
            override val itemNameProvider: OSNameProvider,
            override val icon: OSItemIllustration,
            override val tabs: LinkedHashSet<ItemDetailsTab>,
            override val informationTab: List<InformationTabEntry>,
            override val moreTab: List<MoreTabEntry>,
            override val children: Flow<PagingData<PlainItemData>>,
            override val childrenCount: Int,
            override val actions: List<SafeItemAction>,
            override val color: Color?,
            override val initialTab: ItemDetailsTab,
            override val isCorrupted: Boolean,
            override val notSupportedKindsList: List<SafeItemFieldKind.Unknown>?,
            override val shouldShowEditTips: Boolean,
        ) : Data {

            override val emptyElementsText: LbcTextSpec
                get() = if (isCorrupted) {
                    LbcTextSpec.StringResource(id = OSString.safeItemDetail_corrupted_addItemMessage)
                } else {
                    LbcTextSpec.StringResource(id = OSString.safeItemDetail_contentCard_elements_empty, itemNameProvider.name)
                }

            override val emptyFieldsText: LbcTextSpec
                get() = if (isCorrupted) {
                    LbcTextSpec.StringResource(id = OSString.safeItemDetail_contentCard_informations_corrupted)
                } else {
                    LbcTextSpec.StringResource(id = OSString.safeItemDetail_contentCard_informations_empty)
                }

            override val corruptedCardData: ItemDetailsCorruptedCardData?
                get() = if (isCorrupted) {
                    ItemDetailsCorruptedCardData
                } else {
                    null
                }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Default

                if (itemNameProvider != other.itemNameProvider) return false
                if (icon != other.icon) return false
                if (tabs != other.tabs) return false
                if (informationTab != other.informationTab) return false
                if (moreTab != other.moreTab) return false
                if (children != other.children) return false
                if (actions != other.actions) return false
                if (color != other.color) return false
                if (initialTab != other.initialTab) return false
                if (isCorrupted != other.isCorrupted) return false
                if (notSupportedKindsList != other.notSupportedKindsList) return false
                if (shouldShowEditTips != other.shouldShowEditTips) return false

                return true
            }

            override fun hashCode(): Int {
                var result = itemNameProvider.hashCode()
                result = 31 * result + icon.hashCode()
                result = 31 * result + tabs.hashCode()
                result = 31 * result + informationTab.hashCode()
                result = 31 * result + moreTab.hashCode()
                result = 31 * result + children.hashCode()
                result = 31 * result + actions.hashCode()
                result = 31 * result + (color?.hashCode() ?: 0)
                result = 31 * result + initialTab.hashCode()
                result = 31 * result + isCorrupted.hashCode()
                result = 31 * result + notSupportedKindsList.hashCode()
                result = 31 * result + shouldShowEditTips.hashCode()
                return result
            }
        }

        data class Deleted(
            val defaultData: Default,
            val deletedCardData: ItemDetailsDeletedCardData,
        ) : Data by defaultData {
            override val emptyElementsText: LbcTextSpec = if (isCorrupted) {
                LbcTextSpec.StringResource(id = OSString.safeItemDetail_contentCard_elements_emptyCorruptedDeleted)
            } else {
                LbcTextSpec.StringResource(id = OSString.safeItemDetail_contentCard_elements_emptyDeleted, itemNameProvider.name)
            }

            override val emptyFieldsText: LbcTextSpec
                get() = if (isCorrupted) {
                    LbcTextSpec.StringResource(id = OSString.safeItemDetail_contentCard_informations_corrupted)
                } else {
                    LbcTextSpec.StringResource(id = OSString.safeItemDetail_contentCard_informations_emptyDeleted)
                }
        }
    }
}
