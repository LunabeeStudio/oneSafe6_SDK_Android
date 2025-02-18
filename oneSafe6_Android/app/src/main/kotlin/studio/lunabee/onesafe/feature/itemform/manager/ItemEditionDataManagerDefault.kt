package studio.lunabee.onesafe.feature.itemform.manager

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.extension.startEmojiOrNull
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.common.UrlMetadata
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.settings.GetAppSettingUseCase
import studio.lunabee.onesafe.domain.utils.FileHelper.clearExtension
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.camera.model.OSMediaType
import studio.lunabee.onesafe.feature.dialog.ColorConfirmationDialogState
import studio.lunabee.onesafe.feature.itemform.model.ItemDataForm
import studio.lunabee.onesafe.feature.itemform.model.canAutoOverride
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NameTextUiField
import studio.lunabee.onesafe.feature.itemform.screen.RenameFieldDialogState
import studio.lunabee.onesafe.feature.itemform.viewmodel.ItemEditionDataManager
import studio.lunabee.onesafe.ui.extensions.getFirstColorGenerated
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@ViewModelScoped
class ItemEditionDataManagerDefault @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAppSettingUseCase: GetAppSettingUseCase,
    private val imageHelper: ImageHelper,
) : CloseableCoroutineScope by CloseableMainCoroutineScope(), ItemEditionDataManager {

    override val nameField: NameTextUiField = NameTextUiField(
        id = UUID.randomUUID(),
        fieldDescription = mutableStateOf(LbcTextSpec.StringResource(OSString.fieldName_elementName)),
        placeholder = LbcTextSpec.StringResource(OSString.safeItemDetail_title_placeholder),
        safeItemFieldKind = SafeItemFieldKind.Text,
        onValueChange = ::checkFirstEmojiAndUseItAsImage,
    )

    private val _dialogState = MutableStateFlow<DialogState?>(value = null)
    val dialogState: StateFlow<DialogState?> = _dialogState.asStateFlow()

    private var initialColor: Color? = null
    private var userConfirmImageColorApplication: Boolean? = null

    private val _emojiAsItemIcon: MutableStateFlow<String?> = MutableStateFlow(null)
    override val emojiAsItemIcon: StateFlow<String?> = _emojiAsItemIcon.asStateFlow()

    private val _itemIcon: MutableStateFlow<ItemDataForm<OSImageSpec>?> = MutableStateFlow(null)
    val itemIconData: StateFlow<ItemDataForm<OSImageSpec>?> = _itemIcon.asStateFlow()
    override val itemIcon: StateFlow<OSImageSpec?> = _itemIcon
        .map { it?.data }
        .stateIn(coroutineScope, CommonUiConstants.Flow.DefaultSharingStarted, null)

    /**
     * @see [colorCandidate]
     */
    private val _colorCandidate: MutableStateFlow<ItemDataForm<Color>?> = MutableStateFlow(null)

    /**
     * Color candidate for saving on form validation
     */
    val colorCandidate: StateFlow<Color?> = _colorCandidate
        .map { it?.data }
        .stateIn(coroutineScope, CommonUiConstants.Flow.DefaultSharingStarted, null)

    /**
     * Temporary user chosen color while using the picker or before validating color extracted from the item icon
     */
    private val _colorPreview: MutableStateFlow<Color?> = MutableStateFlow(null)

    /**
     * Color use for theme of the screen during edition. If the user is previewing a color (i.e using the picker), show this color. Else,
     * fallback to the candidate color.
     */
    override val colorPreview: StateFlow<Color?> = _colorPreview
        .transform {
            emit(it)
            delay(ColorChangeThrottleMs)
        }
        .transformLatest { colorPreview ->
            if (colorPreview == null) {
                emitAll(colorCandidate)
            } else {
                emit(colorPreview)
                delay(ColorChangeThrottleMs)
            }
        }.stateIn(coroutineScope, CommonUiConstants.Flow.DefaultSharingStarted, null)

    private val iconCacheDir: File = File(context.cacheDir, IconDir).apply {
        deleteOnExit()
    }

    /**
     * Handle image picked from external source (i.e gallery picker)
     */
    override fun onItemIconPickedByUser(image: OSImageSpec?) {
        if (image != null) { // check nullability (i.e user goes back from intent without selecting an image).
            _itemIcon.value = ItemDataForm(image, true)
            extractColorFromImage(image)
        }
    }

    private fun shouldImageFromEmojiBeGenerated(): Boolean = this.itemIcon.value == null

    private fun checkFirstEmojiAndUseItAsImage(string: String) {
        val emoji = string.startEmojiOrNull()
        if (emoji != null) {
            if (emojiAsItemIcon.value != emoji && shouldImageFromEmojiBeGenerated()) {
                coroutineScope.launch {
                    // Do nothing if unable to create the bitmap
                    imageHelper.createBitmapWithText(emoji)?.let { bitmapWithText ->
                        val image = OSImageSpec.Data(imageHelper.convertBitmapToByteArray(bitmapWithText))
                        _emojiAsItemIcon.value = emoji
                        extractColorFromImage(image)
                    }
                }
            }
        } else if (_emojiAsItemIcon.value != null) {
            _emojiAsItemIcon.value = null // We had an emoji but the user removed it => we need to remove the icon
        }
    }

    fun onImageCaptureFromSystemCamera(image: OSImageSpec.Uri) {
        _itemIcon.value = ItemDataForm(image, true)
        extractColorFromImage(image)
    }

    fun setNameAndIconFromFileField(thumbnail: OSImageSpec?, name: LbcTextSpec) {
        val nameString = name.string(context).clearExtension()
        if (nameField.getDisplayedValue().isEmpty() && nameString.isNotEmpty()) {
            nameField.onValueChanged(nameString)
        }
        if (this.itemIcon.value == null && thumbnail != null) {
            onThumbnailSelected(thumbnail)
        }
    }

    override fun onThumbnailSelected(thumbnail: OSImageSpec) {
        _itemIcon.value = ItemDataForm(thumbnail, true)
        extractColorFromImage(thumbnail)
    }

    fun onImageCaptureFromInAppCamera(thumbnailFile: File): LBResult<Unit> {
        val icon = OSImageSpec.Uri(Uri.fromFile(thumbnailFile), Random.nextInt().toString())
        _itemIcon.value = ItemDataForm(icon, true)
        extractColorFromImage(icon)
        return LBResult.Success(Unit)
    }

    override fun removeItemIconSelected() {
        runCatching { _itemIcon.value?.data?.getAs<Uri>()?.toFile() }.getOrNull()?.delete()
        _itemIcon.value = null
        checkFirstEmojiAndUseItAsImage(nameField.getDisplayedValue())
    }

    fun setItemIconFromMetadataIfNeeded(urlMetadata: UrlMetadata?) {
        val icon = urlMetadata?.iconFile?.let { file -> OSImageSpec.Uri(uri = Uri.fromFile(file)) }
        urlMetadata?.title?.let(nameField::onValueChangedFromUrlMetadata)
        if (icon != null && (_itemIcon.value.canAutoOverride || urlMetadata.force)) {
            removeItemIconSelected()
            extractColorFromImage(icon)
            _itemIcon.value = ItemDataForm(icon, urlMetadata.force)
        }
    }

    fun setInitialIconNameAndColor(icon: OSImageSpec?, color: ItemDataForm<Color>?, name: String?) {
        initialColor = if (color?.isUserPicked == true) color.data else null
        _colorCandidate.value = color
        _itemIcon.value = icon?.let { ItemDataForm(icon, true) }
        _emojiAsItemIcon.value = name?.startEmojiOrNull()
    }

    private fun extractColorFromImage(
        image: OSImageSpec,
    ) {
        coroutineScope.launch {
            val extractedColor = getColorFromImage(image) ?: return@launch
            if (
                _colorCandidate.value != null &&
                extractedColor != colorCandidate.value &&
                userConfirmImageColorApplication == null
            ) {
                if (_colorCandidate.value.canAutoOverride) {
                    _colorCandidate.value = ItemDataForm(extractedColor, false)
                } else {
                    _colorPreview.value = extractedColor
                    // Asking confirmation if a color has already been picked by user.
                    _dialogState.value = ColorConfirmationDialogState { hasConfirm ->
                        if (hasConfirm) {
                            _colorCandidate.value = ItemDataForm(extractedColor, false)
                        }
                        _colorPreview.value = null
                        _dialogState.value = null
                        userConfirmImageColorApplication = hasConfirm
                    }
                }
            } else if (userConfirmImageColorApplication != false || _colorCandidate.value.canAutoOverride) {
                _colorCandidate.value = ItemDataForm(extractedColor, false)
            }
        }
    }

    suspend fun getColorFromImage(image: OSImageSpec): Color? {
        return imageHelper
            .osImageDataToBitmap(context = context, image = image)
            ?.let { bitmap ->
                imageHelper.extractColorPaletteFromBitmap(bitmap)
            }
            ?.getFirstColorGenerated()
    }

    override fun setColorSelected(color: Color?) {
        _colorPreview.value = color
    }

    override fun saveSelectedColor() {
        // fallback to current value
        _colorCandidate.value = _colorPreview.value?.let { color -> ItemDataForm(color, true) }
        // reset user choice
        userConfirmImageColorApplication = null
    }

    fun showRenameFieldDialog(
        onConfirm: (String) -> Unit,
        currentFieldName: String,
    ) {
        var currentEditValue = ""
        _dialogState.value = RenameFieldDialogState(
            forceDismiss = { _dialogState.value = null },
            onConfirm = {
                onConfirm(currentEditValue)
                _dialogState.value = null
            },
            onValueChange = {
                currentEditValue = it
            },
            currentName = currentFieldName,
        )
    }

    fun prepareDataForItemIconCapture(): Flow<CameraData> {
        return getAppSettingUseCase.cameraSystemFlow().map { cameraSystem ->
            when (cameraSystem) {
                CameraSystem.InApp -> CameraData.InApp(prepareDataForItemIconCaptureInApp())
                CameraSystem.External -> CameraData.External(lazy(::prepareDataForItemIconCaptureExternal))
            }
        }
    }

    /**
     * File for item icon capture via ExternalCamera
     */
    private fun prepareDataForItemIconCaptureExternal(): ExternalPhotoCapture {
        iconCacheDir.deleteRecursively()
        val file = File(iconCacheDir, "captured_icon.${OSMediaType.PHOTO.extension}").also {
            it.parentFile?.mkdirs()
        }
        val authority = "${context.packageName}.${AppConstants.FileProvider.FileProviderAuthoritySuffix}"
        val publicUri = FileProvider.getUriForFile(context, authority, file)
        return ExternalPhotoCapture(file, publicUri)
    }

    /**
     * File for item icon capture via the CameraActivity
     */
    private fun prepareDataForItemIconCaptureInApp(): InAppMediaCapture {
        return InAppMediaCapture(
            plainThumbnailFile = File(iconCacheDir, "thumbnail"),
            encryptedFile = null, // We only use the thumbnail,
            mediaType = OSMediaType.PHOTO,
        )
    }

    override fun close() {
        iconCacheDir.deleteRecursively()
    }

    companion object {
        private const val IconDir: String = "edition_item_icon"
    }
}

private const val ColorChangeThrottleMs: Long = 500
