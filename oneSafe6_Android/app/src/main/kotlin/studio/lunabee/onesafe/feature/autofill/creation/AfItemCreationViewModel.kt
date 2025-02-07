package studio.lunabee.onesafe.feature.autofill.creation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lunabee.lbloading.LoadingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.autofill.delegate.AfGetClientDataDelegate
import studio.lunabee.onesafe.feature.autofill.delegate.AfGetClientDataDelegateImpl
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionDataManagerDefault
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionFileFieldManager
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataFieldObserver
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataManager
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.creation.PopulateScreenFromTemplateDelegate
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.creation.SaveItemAndFieldCreationDelegate
import studio.lunabee.onesafe.feature.itemform.viewmodel.impl.ItemCreationViewModel
import studio.lunabee.onesafe.usecase.GetItemFormTipsToSeeUseCase
import studio.lunabee.onesafe.usecase.SaveItemFormTipsSeenUseCase
import javax.inject.Inject

@HiltViewModel
class AfItemCreationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    saveItemAndFieldDelegate: SaveItemAndFieldCreationDelegate,
    populateScreenDelegate: PopulateScreenFromTemplateDelegate,
    urlMetadataManager: UrlMetadataManager,
    itemEditionDataManager: ItemEditionDataManagerDefault,
    afGetClientDataDelegate: AfGetClientDataDelegateImpl,
    itemEditionFileManager: ItemEditionFileFieldManager,
    fieldIdProvider: FieldIdProvider,
    urlMetadataFieldObserver: UrlMetadataFieldObserver,
    loadingManager: LoadingManager,
    getItemFormTipsToSeeUseCase: GetItemFormTipsToSeeUseCase,
    saveItemFormTipsSeenUseCase: SaveItemFormTipsSeenUseCase,
) : ItemCreationViewModel(
    saveItemAndFieldDelegate = saveItemAndFieldDelegate,
    populateScreenFromTemplateDelegate = populateScreenDelegate,
    urlMetadataManager = urlMetadataManager,
    itemEditionDataManager = itemEditionDataManager,
    itemEditionFileManager = itemEditionFileManager,
    fieldIdProvider = fieldIdProvider,
    urlMetadataFieldObserver = urlMetadataFieldObserver,
    loadingManager = loadingManager,
    getItemFormTipsToSeeUseCase = getItemFormTipsToSeeUseCase,
    saveItemFormTipsSeenUseCase = saveItemFormTipsSeenUseCase,
),
    AfGetClientDataDelegate by afGetClientDataDelegate {

    private val identifier: String? = savedStateHandle.get<String>(AfItemCreationDestination.IdentiferArgs)
    private val password: String? = savedStateHandle.get<String>(AfItemCreationDestination.PasswordArgs)
    private val clientDomain: String? = savedStateHandle.get<String>(AfItemCreationDestination.ClientDomainArgs)
    private val clientPackage: String? = savedStateHandle.get<String>(AfItemCreationDestination.ClientPackageArgs)

    init {
        viewModelScope.launch {
            val fields = uiFields.value.toMutableList()
            val client = getClientData(clientDomain.orEmpty(), clientPackage.orEmpty())
            if (identifier != null) {
                fields.firstOrNull { it.isIdentifier }?.initValues(true, identifier)
            }
            if (password != null) {
                fields.firstOrNull { it.safeItemFieldKind is SafeItemFieldKind.Password }?.initValues(false, password)
            }

            if (client?.name != null) {
                itemEditionDataManager.nameField.onValueChanged(client.name)
            }

            val domain = client?.domains?.firstOrNull()
            if (domain != null) {
                fields.firstOrNull { it.safeItemFieldKind is SafeItemFieldKind.Url }?.initValues(
                    false,
                    domain.toString(),
                )
            }

            setUiFields(fields)

            if (domain != null) {
                forceFetchDataFromUrl()
            }
        }
    }
}
