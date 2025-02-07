package studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.edition

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetItemEditionInfoUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.fileviewer.loadfile.LoadFileUseCase
import studio.lunabee.onesafe.feature.itemform.destination.ItemEditionDestination
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionDataManagerDefault
import studio.lunabee.onesafe.feature.itemform.model.ItemDataForm
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.ExistingFileUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NormalTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.PasswordTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.UrlTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.YearMonthDateTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl.DateAndTimeUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl.DateUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl.TimeUiField
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemFormInitialInfo
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.PopulateScreenDelegate
import studio.lunabee.onesafe.ui.extensions.toColor
import studio.lunabee.onesafe.usecase.AndroidGetThumbnailFromFileUseCase
import java.util.UUID
import javax.inject.Inject

class PopulateScreenFromItemDelegate @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getItemEditionInfoUseCase: GetItemEditionInfoUseCase,
    private val decryptUseCase: ItemDecryptUseCase,
    private val getIconUseCase: GetIconUseCase,
    private val itemEditionDataManager: ItemEditionDataManagerDefault,
    private val loadFileUseCase: LoadFileUseCase,
    private val getThumbnailFromFileUseCase: AndroidGetThumbnailFromFileUseCase,
) : PopulateScreenDelegate {

    val itemId: UUID = savedStateHandle.get<String>(ItemEditionDestination.ItemIdArg)?.let(UUID::fromString)
        ?: error("Item id not provided")

    override suspend fun getInitialInfo(): LBResult<ItemFormInitialInfo> {
        val encInfoResult = getItemEditionInfoUseCase(itemId = itemId)
        val plainName = encInfoResult.data?.encName?.let {
            decryptUseCase(data = it, itemId = itemId, clazz = String::class)
        }?.data ?: ""

        val icon = encInfoResult.data?.iconId?.let { getIconUseCase(iconId = it, itemId = itemId).data }
        val color = encInfoResult.data?.encColor?.let {
            decryptUseCase(data = it, itemId = itemId, clazz = String::class).data?.toColor()
        }

        itemEditionDataManager.setInitialIconNameAndColor(
            icon = icon?.let { OSImageSpec.Data(it) },
            color = color?.let { ItemDataForm(color, true) },
            name = plainName,
        )
        return LBResult.Success(
            ItemFormInitialInfo(
                name = plainName,
                color = color,
                icon = icon,
                fields = getInitialFields(
                    encSafeItemFields = encInfoResult.data?.encSafeItemFields ?: listOf(),
                ),
                isFromCamera = false,
            ),
        )
    }

    override fun CoroutineScope.loadInitialInfo(info: ItemFormInitialInfo): Job {
        val supervisorJob = SupervisorJob()
        val scope = this + supervisorJob
        info.fields.filterIsInstance<ExistingFileUiField>().forEach {
            scope.launch { it.loadFile() }
        }
        return supervisorJob
    }

    private suspend fun getInitialFields(encSafeItemFields: List<SafeItemField>): List<UiField> {
        return encSafeItemFields.mapNotNull { safeItemField ->
            val kindString = safeItemField.encKind?.let { decryptUseCase(it, safeItemField.itemId, String::class).data }
            val itemKind = kindString?.let(SafeItemFieldKind::fromString)
                ?: SafeItemFieldKind.Unknown(id = AppConstants.DefaultValue.NoSafeItemKindId)
            val value = safeItemField.encValue?.let { decryptUseCase(it, safeItemField.itemId, String::class).data }
            val name = safeItemField.encName?.let { decryptUseCase(it, safeItemField.itemId, String::class).data }
            val fieldDescription: MutableState<LbcTextSpec> = mutableStateOf(LbcTextSpec.Raw(name.orEmpty()))
            val placeholder = safeItemField.encPlaceholder?.let { decryptUseCase(it, safeItemField.itemId, String::class).data }
            val fieldPlaceholder = LbcTextSpec.Raw(placeholder.orEmpty())
            val uiField = when (itemKind) {
                SafeItemFieldKind.DateAndHour -> DateAndTimeUiField(
                    id = safeItemField.id,
                    safeItemFieldKind = itemKind,
                    fieldDescription = fieldDescription,
                    placeholder = fieldPlaceholder,
                )
                SafeItemFieldKind.Date -> DateUiField(
                    id = safeItemField.id,
                    safeItemFieldKind = itemKind,
                    fieldDescription = fieldDescription,
                    placeholder = fieldPlaceholder,
                )
                SafeItemFieldKind.Hour -> TimeUiField(
                    id = safeItemField.id,
                    safeItemFieldKind = itemKind,
                    fieldDescription = fieldDescription,
                    placeholder = fieldPlaceholder,
                )
                SafeItemFieldKind.Url -> UrlTextUiField(
                    id = safeItemField.id,
                    safeItemFieldKind = itemKind,
                    fieldDescription = fieldDescription,
                    placeholder = fieldPlaceholder,
                )
                SafeItemFieldKind.YearMonth -> YearMonthDateTextUiField(
                    id = safeItemField.id,
                    safeItemFieldKind = itemKind,
                    fieldDescription = fieldDescription,
                    placeholder = fieldPlaceholder,
                )
                SafeItemFieldKind.Email,
                SafeItemFieldKind.Note,
                SafeItemFieldKind.Number,
                SafeItemFieldKind.Phone,
                SafeItemFieldKind.Text,
                SafeItemFieldKind.CreditCardNumber,
                SafeItemFieldKind.Iban,
                SafeItemFieldKind.SocialSecurityNumber,
                SafeItemFieldKind.Password,
                -> if (safeItemField.isSecured) {
                    PasswordTextUiField(
                        id = safeItemField.id,
                        safeItemFieldKind = itemKind,
                        fieldDescription = fieldDescription,
                        placeholder = fieldPlaceholder,
                    )
                } else {
                    NormalTextUiField(
                        id = safeItemField.id,
                        safeItemFieldKind = itemKind,
                        fieldDescription = fieldDescription,
                        placeholder = fieldPlaceholder,
                    )
                }
                is SafeItemFieldKind.Unknown -> NormalTextUiField(
                    id = safeItemField.id,
                    safeItemFieldKind = itemKind,
                    fieldDescription = fieldDescription,
                    placeholder = fieldPlaceholder,
                )
                SafeItemFieldKind.Video, SafeItemFieldKind.Photo, SafeItemFieldKind.File ->
                    value?.let {
                        ExistingFileUiField(
                            id = safeItemField.id,
                            safeItemFieldKind = itemKind,
                            safeItemField = safeItemField,
                            fieldDescription = fieldDescription,
                            loadFileUseCase = loadFileUseCase,
                            value = value,
                            getThumbnailFromFileUseCase = getThumbnailFromFileUseCase,
                        )
                    }
            }
            uiField.also {
                it?.initValues(
                    isIdentifier = safeItemField.isItemIdentifier,
                    initialValue = value,
                )
            }
        }
    }
}
