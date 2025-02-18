package studio.lunabee.onesafe.feature.itemform.model.uifield

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityCustomAction
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.domain.model.safeitem.FieldMask
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.bottomsheet.identifier.IdentifierInfoBottomSheet
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.screen.DeleteFieldConfirmationDialogState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import java.util.UUID

abstract class UiField {
    abstract val id: UUID
    abstract var placeholder: LbcTextSpec
    abstract val safeItemFieldKind: SafeItemFieldKind
    abstract var fieldDescription: MutableState<LbcTextSpec>
    abstract val isSecured: Boolean
    open val options: List<UiFieldOption> = emptyList()
    open val errorFieldLabel: LbcTextSpec? = null

    var tipsUiField: TipsUiField? by mutableStateOf(null)

    var isErrorDisplayed: Boolean by mutableStateOf(false)
    open fun isInError(): Boolean = false

    var isIdentifier: Boolean by mutableStateOf(false)

    abstract fun getDisplayedValue(): String
    fun displayErrorOnFieldIfNeeded() {
        if (isInError()) isErrorDisplayed = true
    }

    open fun getItemFieldAction(
        toggleIdentifier: () -> Unit,
        renameField: () -> Unit,
        tryToDeleteField: () -> Unit,
        useThumbnailAsIcon: (OSImageSpec) -> Unit,
    ): List<ItemFieldActions> {
        return if (isIdentifier) {
            listOf(
                ItemFieldActions.RenameLabel(onClick = renameField),
                ItemFieldActions.RemoveIdentifier(onClick = toggleIdentifier),
                ItemFieldActions.DeleteField(onClick = tryToDeleteField),
            )
        } else {
            listOf(
                ItemFieldActions.RenameLabel(onClick = renameField),
                ItemFieldActions.UseAsIdentifier(onClick = toggleIdentifier),
                ItemFieldActions.DeleteField(onClick = tryToDeleteField),
            )
        }
    }

    // Get value which will be saved in database
    open fun getSavedValue(): String = getDisplayedValue()

    abstract fun initValues(isIdentifier: Boolean, initialValue: String?)

    @Composable
    fun getItemFieldData(index: Int): ItemFieldData {
        return ItemFieldData(
            id = id,
            name = fieldDescription.value.string,
            position = index.toDouble(),
            placeholder = placeholder.string,
            value = getSavedValue().takeIf { it.isNotBlank() },
            kind = safeItemFieldKind,
            showPrediction = false,
            isItemIdentifier = isIdentifier,
            formattingMask = FieldMask.getMatchingMask(safeItemFieldKind.maskList, getDisplayedValue())?.formattingMask,
            secureDisplayMask = FieldMask.getMatchingMask(safeItemFieldKind.maskList, getDisplayedValue())?.securedDisplayingMask,
            isSecured = isSecured,
        )
    }

    @Composable
    fun MainComposable(
        modifier: Modifier,
        hasNext: Boolean,
        toggleIdentifier: () -> Unit,
        renameField: (String) -> Unit,
        removeField: () -> Unit,
        useThumbnailAsIcon: (OSImageSpec) -> Unit,
    ) {
        var dialogState: DialogState? by rememberSaveable { mutableStateOf(value = null) }
        var isIdentifierBottomSheetDisplayed: Boolean by rememberSaveable { mutableStateOf(false) }
        var isActionMenuExpanded: Boolean by rememberSaveable { mutableStateOf(false) }
        val context = LocalContext.current

        val tryToDeleteField: () -> Unit = {
            if (getDisplayedValue().isNotEmpty()) {
                dialogState = DeleteFieldConfirmationDialogState(
                    dismiss = { dialogState = null },
                    confirm = removeField,
                )
            } else {
                removeField()
            }
            isActionMenuExpanded = false
        }

        val currentFieldName = fieldDescription.value.string
        dialogState?.DefaultAlertDialog()
        key(id) {
            Box(modifier = modifier) {
                IdentifierInfoBottomSheet(
                    isVisible = isIdentifierBottomSheetDisplayed,
                    onBottomSheetClosed = { isIdentifierBottomSheetDisplayed = false },
                )

                Column(
                    Modifier
                        .semantics {
                            this.isTraversalGroup = true
                            this.heading()
                            val customActionDescriptions = customAccessibilityActions(
                                deleteField = tryToDeleteField,
                                toggleIdentifier = toggleIdentifier,
                                renameField = { renameField(currentFieldName) },
                                context = context,
                            )
                            customActions = buildList {
                                this += customActionDescriptions.map { action ->
                                    accessibilityCustomAction(label = action.first, action = action.second)
                                }
                                if (isIdentifier) {
                                    this += accessibilityCustomAction(
                                        label = context.getString(OSString.itemForm_identifier_accessibility_action_knownMore),
                                    ) { isIdentifierBottomSheetDisplayed = true }
                                }
                            }
                        },
                ) {
                    Row(
                        modifier = Modifier.padding(start = OSDimens.SystemSpacing.Small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val innerComposable: @Composable () -> Unit = {
                            InnerComposable(
                                modifier = Modifier,
                                hasNext = hasNext,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f),
                        ) {
                            tipsUiField?.let {
                                FormUiFieldTooltip(
                                    tipsUiField = it,
                                    content = { innerComposable() },
                                )
                            } ?: innerComposable()
                        }

                        Box {
                            OSIconButton(
                                image = OSImageSpec.Drawable(OSDrawable.ic_menu),
                                onClick = { isActionMenuExpanded = true },
                                modifier = Modifier
                                    .testTag(UiConstants.TestTag.Item.fieldActionButton(fieldDescription.value.string)),
                                colors = OSIconButtonDefaults.transparentIconButtonColors(state = OSActionState.Enabled),
                                buttonSize = OSDimens.SystemButtonDimension.Regular,
                                contentDescription = LbcTextSpec.StringResource(
                                    OSString.itemForm_action_showFieldAction_accessibility,
                                    currentFieldName,
                                ),
                            )
                            FieldActionMenu(
                                isMenuExpended = isActionMenuExpanded,
                                onDismiss = { isActionMenuExpanded = false },
                                actions = getItemFieldAction(
                                    tryToDeleteField = tryToDeleteField,
                                    toggleIdentifier = {
                                        toggleIdentifier()
                                        isActionMenuExpanded = false
                                    },
                                    renameField = {
                                        renameField(currentFieldName)
                                        isActionMenuExpanded = false
                                    },
                                    useThumbnailAsIcon = {
                                        useThumbnailAsIcon(it)
                                        isActionMenuExpanded = false
                                    },
                                ),
                                modifier = Modifier.testTag(UiConstants.TestTag.Menu.FieldActionMenu),
                            )
                        }
                    }

                    if (isIdentifier) {
                        OSText(
                            text = LbcTextSpec.StringResource(id = OSString.itemForm_identifierLabel),
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalColorPalette.current.Neutral60,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .semantics {
                                    text = LbcTextSpec
                                        .StringResource(
                                            OSString.itemForm_identifierLabel_accessibility,
                                            fieldDescription.value,
                                            getDisplayedValue(),
                                        )
                                        .annotated(context)
                                }
                                .clickable(
                                    onClickLabel = context.getString(OSString.itemForm_identifier_knownMore_accessibility_clickLabel),
                                ) {
                                    isIdentifierBottomSheetDisplayed = true
                                }
                                .padding(vertical = OSDimens.SystemSpacing.ExtraSmall, horizontal = OSDimens.SystemSpacing.Small)
                                .testTag(UiConstants.TestTag.Item.IdentifierLabelText),
                        )
                    }
                }
            }
        }
    }

    @Composable
    abstract fun InnerComposable(modifier: Modifier, hasNext: Boolean)
}

fun UiField.customAccessibilityActions(
    deleteField: () -> Unit,
    toggleIdentifier: () -> Unit,
    renameField: () -> Unit,
    context: Context,
): MutableList<Pair<String, () -> Unit>> {
    val customActionDescriptions = options.map { option ->
        // if click label is empty, custom action will not be in the list
        option.clickLabel?.string(context).orEmpty() to { option.onClick() }
    }.toMutableList()
    customActionDescriptions += (
        context.getString(
            OSString.accessibility_field_delete,
            fieldDescription.value.string(context),
        ) to deleteField
        )
    customActionDescriptions += if (isIdentifier) {
        context.getString(OSString.itemForm_action_dontUseAsIdentifier) to toggleIdentifier
    } else {
        context.getString(OSString.itemForm_action_useAsIdentifier) to toggleIdentifier
    }
    customActionDescriptions += (context.getString(OSString.itemForm_action_rename) to renameField)
    return customActionDescriptions
}

fun List<UiField>.textFields(): List<UiField>? {
    return filter {
        !SafeItemFieldKind.isKindFile(it.safeItemFieldKind)
    }.takeIf { it.isNotEmpty() }
}

fun List<UiField>.mediaFields(): List<UiField>? {
    return filter {
        (it.safeItemFieldKind == SafeItemFieldKind.Video) || (it.safeItemFieldKind == SafeItemFieldKind.Photo)
    }.takeIf { it.isNotEmpty() }
}

fun List<UiField>.fileFields(): List<UiField>? {
    return filter {
        it.safeItemFieldKind == SafeItemFieldKind.File
    }.takeIf { it.isNotEmpty() }
}
