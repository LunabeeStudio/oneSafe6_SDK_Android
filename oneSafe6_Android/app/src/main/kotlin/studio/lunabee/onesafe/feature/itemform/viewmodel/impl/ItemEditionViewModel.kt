package studio.lunabee.onesafe.feature.itemform.viewmodel.impl

import androidx.compose.ui.graphics.Color
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionDataManagerDefault
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionFileFieldManager
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataFieldObserver
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataManager
import studio.lunabee.onesafe.feature.itemform.model.ExistingFileInitialUiField
import studio.lunabee.onesafe.feature.itemform.model.InitialUiField
import studio.lunabee.onesafe.feature.itemform.model.ItemInitialData
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.FileUiField
import studio.lunabee.onesafe.feature.itemform.viewmodel.ItemFormViewModel
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemDataForSaving
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemFormInitialInfo
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.edition.PopulateScreenFromItemDelegate
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.edition.SaveItemAndFieldEditionDelegate
import studio.lunabee.onesafe.model.OSActionState
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ItemEditionViewModel @Inject constructor(
    saveItemAndFieldDelegate: SaveItemAndFieldEditionDelegate,
    populateScreenDelegate: PopulateScreenFromItemDelegate,
    urlMetadataManager: UrlMetadataManager,
    private val itemEditionDataManager: ItemEditionDataManagerDefault,
    itemEditionFileManager: ItemEditionFileFieldManager,
    fieldIdProvider: FieldIdProvider,
    urlMetadataFieldObserver: UrlMetadataFieldObserver,
    loadingManager: LoadingManager,
) : ItemFormViewModel(
    saveItemAndFieldDelegate = saveItemAndFieldDelegate,
    populateScreenDelegate = populateScreenDelegate,
    urlMetadataManager = urlMetadataManager,
    itemEditionDataManager = itemEditionDataManager,
    itemEditionFileManager = itemEditionFileManager,
    fieldIdProvider = fieldIdProvider,
    urlMetadataFieldObserver = urlMetadataFieldObserver,
    loadingManager = loadingManager,
) {
    private var initialValues: ItemInitialData? = null

    /**
     * Save is accessible if:
     * - name changed
     * - icon changed
     * - color changed
     * - if field containing data is removed or changed
     */
    private fun isCandidateVersionSameThanInitial(): Boolean {
        var result = initialValues?.name == itemEditionDataManager.nameField.getDisplayedValue()
        // Null colorCandidate is unexpected as is should be set when calling ItemEditionDataManager::setInitialIconNameAndColor
        val colorCandidate = itemEditionDataManager.colorCandidate.value
        result = result && (initialValues?.color == colorCandidate || colorCandidate == null)
        result = result && (initialValues?.icon == itemEditionDataManager.itemIcon.value?.data)
        result = result && initialValues?.fieldValues == uiFields.value.map(InitialUiField::fromUiField)
        return result
    }

    override fun isSaveEnabled(): OSActionState = when {
        isAnyFieldInError() -> OSActionState.Disabled
        isCandidateVersionSameThanInitial() -> OSActionState.DisabledWithAction
        else -> OSActionState.Enabled
    }

    override fun setInitialInfo(initialInfoResult: LBResult<ItemFormInitialInfo>) {
        super.setInitialInfo(initialInfoResult)
        initialInfoResult.data?.let { initialInfo ->
            initialValues = ItemInitialData(
                name = initialInfo.name,
                color = initialInfo.color,
                icon = initialInfo.icon,
                fieldValues = initialInfo.fields.map(InitialUiField::fromUiField),
            )
        }
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
                initialInfo = initialValues,
                fileSavingData = getFileSavingData(),
            ),
        )
    }

    override fun checkTipsToDisplay(fields: List<UiField>) {
        // No-op for now.
    }

    private fun getFileSavingData(): List<FileSavingData> {
        val initialFileFieldsValue = initialValues?.fieldValues?.filterIsInstance<ExistingFileInitialUiField>() ?: emptyList()
        val initialFileFieldsId = initialFileFieldsValue.map { it.id }

        val fileUiFields = uiFields.value.filterIsInstance<FileUiField>()
        val fileUiFieldsId = fileUiFields.map { it.id }

        val newFilesToSave = fileUiFields
            .filter { field -> field.id !in initialFileFieldsId }
            .mapNotNull(FileUiField::getFileSavingData)

        val initialFilesToRemove = initialFileFieldsValue
            .filter { it.id !in fileUiFieldsId }
            .map { it.savingDataRemove }

        return newFilesToSave + initialFilesToRemove
    }
}
