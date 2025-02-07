package studio.lunabee.onesafe.feature.itemform.viewmodel.impl

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.common.utils.OSTipsUtils
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionDataManagerDefault
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionFileFieldManager
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataFieldObserver
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataManager
import studio.lunabee.onesafe.feature.itemform.model.uifield.TipsUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.FileUiField
import studio.lunabee.onesafe.feature.itemform.viewmodel.ItemFormViewModel
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemDataForSaving
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.creation.PopulateScreenFromTemplateDelegate
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.creation.SaveItemAndFieldCreationDelegate
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.tooltip.OSTooltipContent
import studio.lunabee.onesafe.usecase.GetItemFormTipsToSeeUseCase
import studio.lunabee.onesafe.usecase.ItemFormTips
import studio.lunabee.onesafe.usecase.SaveItemFormTipsSeenUseCase
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
open class ItemCreationViewModel @Inject constructor(
    saveItemAndFieldDelegate: SaveItemAndFieldCreationDelegate,
    populateScreenFromTemplateDelegate: PopulateScreenFromTemplateDelegate,
    urlMetadataManager: UrlMetadataManager,
    itemEditionDataManager: ItemEditionDataManagerDefault,
    itemEditionFileManager: ItemEditionFileFieldManager,
    fieldIdProvider: FieldIdProvider,
    urlMetadataFieldObserver: UrlMetadataFieldObserver,
    loadingManager: LoadingManager,
    private val getItemFormTipsToSeeUseCase: GetItemFormTipsToSeeUseCase,
    private val saveItemFormTipsSeenUseCase: SaveItemFormTipsSeenUseCase,
) : ItemFormViewModel(
    saveItemAndFieldDelegate = saveItemAndFieldDelegate,
    populateScreenDelegate = populateScreenFromTemplateDelegate,
    urlMetadataManager = urlMetadataManager,
    itemEditionDataManager = itemEditionDataManager,
    itemEditionFileManager = itemEditionFileManager,
    fieldIdProvider = fieldIdProvider,
    urlMetadataFieldObserver = urlMetadataFieldObserver,
    loadingManager = loadingManager,
) {
    override fun isSaveEnabled(): OSActionState = if (isAnyFieldInError()) {
        OSActionState.DisabledWithAction
    } else {
        OSActionState.Enabled
    }

    override suspend fun save(
        name: String,
        icon: OSImageSpec?,
        color: Color?,
        itemFieldsData: List<ItemFieldData>,
    ): LBResult<UUID> {
        return save(
            data = ItemDataForSaving(
                name = name,
                icon = icon,
                color = color,
                itemFieldsData = itemFieldsData,
                initialInfo = null,
                fileSavingData = getFileSavingData(),
            ),
        )
    }

    override fun checkTipsToDisplay(fields: List<UiField>) {
        viewModelScope.launch {
            val tipsResult = getItemFormTipsToSeeUseCase()
            when (tipsResult.data) {
                ItemFormTips.Url -> {
                    fields
                        .firstOrNull { it.safeItemFieldKind == SafeItemFieldKind.Url }
                        ?.takeIf { it.getDisplayedValue().isEmpty() }?.let { field ->
                            field.tipsUiField = TipsUiField(
                                tooltipContent = OSTooltipContent(
                                    title = OSTipsUtils.CommonTipsTitle,
                                    description = LbcTextSpec.StringResource(OSString.itemForm_tips_url_autoFetch),
                                    actions = listOf(OSTipsUtils.getGotItAction()),
                                ),
                                onDismiss = {
                                    viewModelScope.launch {
                                        saveItemFormTipsSeenUseCase(itemFormTips = ItemFormTips.Url)
                                        field.tipsUiField = null
                                    }
                                },
                            )
                        }
                }
                ItemFormTips.Emoji -> {
                    nameField.tipsUiField = TipsUiField(
                        tooltipContent = OSTooltipContent(
                            title = OSTipsUtils.CommonTipsTitle,
                            description = LbcTextSpec.StringResource(OSString.itemForm_tips_title_emoji),
                            actions = listOf(OSTipsUtils.getGotItAction()),
                        ),
                        onDismiss = {
                            viewModelScope.launch {
                                saveItemFormTipsSeenUseCase(itemFormTips = ItemFormTips.Emoji)
                                nameField.tipsUiField = null
                            }
                        },
                    )
                }
                null -> Unit // no-op
            }
        }
    }

    private fun getFileSavingData(): List<FileSavingData> = uiFields.value
        .filterIsInstance<FileUiField>()
        .mapNotNull(FileUiField::getFileSavingData)
}
