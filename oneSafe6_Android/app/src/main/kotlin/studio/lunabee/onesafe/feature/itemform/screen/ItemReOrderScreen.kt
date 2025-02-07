package studio.lunabee.onesafe.feature.itemform.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbloading.LoadingBackHandler
import com.valentinilk.shimmer.shimmer
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.common.composable.draganddrop.LazyColumnDragDrop
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.feature.dialog.ExitConfirmationDialogState
import studio.lunabee.onesafe.feature.itemform.composable.ItemFormTopAppBar
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.FileUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.ThumbnailState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID

@Composable
fun ItemReOrderScreen(
    isSaveEnabled: (newList: List<UiField>) -> Boolean,
    initialList: List<UiField>,
    exitReOrderMode: () -> Unit,
    updateOrder: (newList: List<UiField>) -> Unit,
) {
    var dialogState: DialogState? by remember { mutableStateOf(value = null) }
    var currentList: List<UiField> by remember { mutableStateOf(initialList) }

    val onBackClick: () -> Unit = {
        if (isSaveEnabled(currentList)) {
            dialogState = ExitConfirmationDialogState.getState(
                dismiss = { dialogState = null },
                navigateBack = exitReOrderMode,
            )
        } else {
            exitReOrderMode()
        }
    }
    dialogState?.DefaultAlertDialog()
    LoadingBackHandler(onBack = onBackClick)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.ItemReOrderScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .imePadding(),
    ) {
        val context = LocalContext.current
        val localView = LocalView.current

        Column {
            ItemFormTopAppBar(
                title = LbcTextSpec.StringResource(OSString.safeItemDetail_reorder),
                saveButtonState = if (isSaveEnabled(currentList)) OSActionState.Enabled else OSActionState.Disabled,
                navigateBack = onBackClick,
                validateForm = {
                    updateOrder(currentList)
                    exitReOrderMode()
                },
                enabledSaveContentDescription = LbcTextSpec.StringResource(OSString.safeItemDetail_reorder_save_accessibility),
                disabledSaveContentDescription = LbcTextSpec.StringResource(OSString.safeItemDetail_reorder_save_accessibility),
                saveClickLabel = LbcTextSpec.StringResource(id = OSString.common_accessibility_save),
                addClickLabel = null,
                addField = null,
            )

            OSCard(
                modifier = Modifier
                    .padding(all = OSDimens.SystemSpacing.Regular),
            ) {
                LazyColumnDragDrop(
                    elements = currentList,
                    onListChange = { newList, toIndex ->
                        localView.announceForAccessibility(
                            context.getString(
                                OSString.safeItemDetail_reorder_accessibility_newPosition,
                                toIndex + 1,
                            ),
                        )
                        currentList = newList
                    },
                    modifier = Modifier
                        .semantics {
                            collectionInfo = CollectionInfo(currentList.size, 1)
                        }
                        .padding(vertical = OSDimens.SystemSpacing.Small),
                    elementsKeys = currentList.map { it.id },
                ) { index, uiField, _ ->
                    when (uiField) {
                        is FileUiField -> {
                            OSRow(
                                text = uiField.fieldDescription.value,
                                textMaxLines = 1,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(
                                        vertical = OSDimens.SystemSpacing.Small,
                                        horizontal = OSDimens.SystemSpacing.Regular,
                                    ),
                                startContent = {
                                    val thumbnail by uiField.thumbnailFlow.collectAsStateWithLifecycle()
                                    val shimmerColor = MaterialTheme.colorScheme.primary
                                    when (val safeThumbnail = (thumbnail as? ThumbnailState.Finished)?.thumbnail) {
                                        null -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(AppConstants.Ui.Item.ThumbnailFileDpSize)
                                                    .clip(RoundedCornerShape(AppConstants.Ui.Item.ThumbnailFileCornerRadius))
                                                    .shimmer()
                                                    .drawBehind { drawRect(shimmerColor) },
                                            )
                                        }
                                        else -> {
                                            OSImage(
                                                image = safeThumbnail,
                                                modifier = Modifier
                                                    .size(AppConstants.Ui.Item.ThumbnailFileDpSize)
                                                    .clip(RoundedCornerShape(AppConstants.Ui.Item.ThumbnailFileCornerRadius)),
                                                contentScale = ContentScale.Crop,
                                            )
                                        }
                                    }
                                },
                                endContent = {
                                    Icon(
                                        painter = painterResource(id = OSDrawable.ic_reorder),
                                        contentDescription = null,
                                        tint = LocalColorPalette.current.Neutral30,
                                    )
                                },
                            )
                        }
                        else -> {
                            key(index) {
                                OSRow(
                                    text = when {
                                        // TODO FIXME use mask instead of hardcoded string
                                        uiField.isSecured && uiField.getDisplayedValue().isNotEmpty() ->
                                            LbcTextSpec.StringResource(OSString.safeItemDetail_contentCard_informations_securedValue)
                                        else -> LbcTextSpec.Raw(uiField.getDisplayedValue())
                                    },
                                    label = uiField.fieldDescription.value,
                                    textMaxLines = 1, // avoid blinking when dragging elements with different sizes
                                    endContent = {
                                        Icon(
                                            painter = painterResource(id = OSDrawable.ic_reorder),
                                            contentDescription = null,
                                            tint = LocalColorPalette.current.Neutral30,
                                        )
                                    },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clearAndSetSemantics {
                                            collectionItemInfo = CollectionItemInfo(
                                                rowIndex = index,
                                                rowSpan = 1,
                                                columnIndex = 1,
                                                columnSpan = 1,
                                            )
                                            text = AnnotatedString(
                                                context.getString(
                                                    OSString.safeItemDetail_reorder_accessibility_rowText,
                                                    uiField.fieldDescription.value.string(context),
                                                    uiField
                                                        .takeIf { !it.isSecured }
                                                        ?.getDisplayedValue()
                                                        .orEmpty(),
                                                    index + 1,
                                                    currentList.size,
                                                ),
                                            )
                                        }
                                        .padding(
                                            vertical = OSDimens.SystemSpacing.Small,
                                            horizontal = OSDimens.SystemSpacing.Regular,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OsDefaultPreview
@Composable
private fun ItemReOrderScreenPreview() {
    OSPreviewBackgroundTheme {
        ItemReOrderScreen(
            isSaveEnabled = { true },
            initialList = List(10) {
                object : UiField() {
                    override val id: UUID = UUID.randomUUID()
                    override var placeholder: LbcTextSpec = loremIpsumSpec(1)
                    override val safeItemFieldKind: SafeItemFieldKind = SafeItemFieldKind.Text
                    override var fieldDescription: MutableState<LbcTextSpec> = mutableStateOf(LbcTextSpec.Raw("Description $it"))
                    override val isSecured: Boolean = false
                    override fun getDisplayedValue(): String = "Value $it"
                    override fun initValues(isIdentifier: Boolean, initialValue: String?) {}

                    @Composable
                    override fun InnerComposable(modifier: Modifier, hasNext: Boolean) {
                    }
                }
            },
            exitReOrderMode = {},
            updateOrder = {},
        )
    }
}
