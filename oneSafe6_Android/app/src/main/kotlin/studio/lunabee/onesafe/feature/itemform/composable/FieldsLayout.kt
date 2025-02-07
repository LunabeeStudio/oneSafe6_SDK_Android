package studio.lunabee.onesafe.feature.itemform.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import com.lunabee.lbextensions.content.getQuantityString
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityCustomAction
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.itemform.model.ItemFormActionsHolder
import studio.lunabee.onesafe.feature.itemform.model.ReorderOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.FormUiFieldTooltip
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.fileFields
import studio.lunabee.onesafe.feature.itemform.model.uifield.mediaFields
import studio.lunabee.onesafe.feature.itemform.model.uifield.textFields
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import java.util.UUID

@Composable
fun FieldsLayout(
    nameField: UiField,
    uiFields: List<UiField>,
    toggleIdentifier: (uiField: UiField) -> Unit,
    useThumbnailAsIcon: (OSImageSpec) -> Unit,
    renameField: (id: UUID, currentName: String) -> Unit,
    removeField: (uiField: UiField) -> Unit,
    reorderField: (ReorderOption) -> Unit,
    itemFormActionsHolder: ItemFormActionsHolder,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(top = OSDimens.SystemSpacing.Regular)
            .padding(horizontal = OSDimens.SystemSpacing.Regular),
        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
    ) {
        OSCard(
            modifier = Modifier.semantics(mergeDescendants = true) {
                this.text = AnnotatedString(context.getString(OSString.itemForm_itemNameCard_accessibility))
                this.isTraversalGroup = true
                this.heading()
            },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = OSDimens.SystemSpacing.Regular,
                        end = OSDimens.SystemSpacing.Regular,
                        top = OSDimens.SystemSpacing.Regular - OSDimens.External.OutlinedTextFieldTopPadding,
                        bottom = OSDimens.SystemSpacing.Regular,
                    ),
            ) {
                val innerComposable: @Composable () -> Unit = {
                    nameField.InnerComposable(
                        modifier = Modifier
                            .fillMaxWidth(),
                        hasNext = uiFields.isNotEmpty(),
                    )
                }
                val tipsUiField = nameField.tipsUiField
                if (tipsUiField != null) {
                    FormUiFieldTooltip(
                        tipsUiField = tipsUiField,
                        content = { innerComposable() },
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                } else {
                    innerComposable()
                }
            }
        }

        uiFields.textFields()?.let { textFields ->
            UiFieldCard(
                title = LbcTextSpec.StringResource(OSString.fieldName_informations),
                onReorderClick = { reorderField(ReorderOption.TextFields) },
                uiFields = textFields,
                toggleIdentifier = toggleIdentifier,
                useThumbnailAsIcon = useThumbnailAsIcon,
                renameField = renameField,
                removeField = removeField,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    this.text = AnnotatedString(
                        context.getQuantityString(
                            OSPlurals.itemForm_fieldCard_accessibility,
                            textFields.size,
                            textFields.size,
                        ),
                    )
                    testTag = UiConstants.TestTag.Item.InformationFieldsCard
                    customActions = listOf(
                        accessibilityCustomAction(
                            itemFormActionsHolder.addNewField.text.string(context),
                            itemFormActionsHolder.addNewField.onClick,
                        ),
                    )
                },
            )
        }
        uiFields.mediaFields()?.let { mediaFields ->
            UiFieldCard(
                title = LbcTextSpec.StringResource(OSString.fieldName_photosAndVideos),
                onReorderClick = { reorderField(ReorderOption.MediaFields) },
                uiFields = mediaFields,
                toggleIdentifier = toggleIdentifier,
                useThumbnailAsIcon = useThumbnailAsIcon,
                renameField = renameField,
                removeField = removeField,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    this.text = AnnotatedString(
                        context.getQuantityString(
                            OSPlurals.itemForm_mediaCard_accessibility,
                            mediaFields.size,
                            mediaFields.size,
                        ),
                    )
                    testTag = UiConstants.TestTag.Item.MediaFieldsCard
                    customActions = listOf(
                        accessibilityCustomAction(
                            itemFormActionsHolder.addNewFile.text.string(context),
                            itemFormActionsHolder.addNewFile.onClick,
                        ),
                    )
                },
            )
        }
        uiFields.fileFields()?.let { fileFields ->
            UiFieldCard(
                title = LbcTextSpec.StringResource(OSString.fieldName_file_plural),
                onReorderClick = { reorderField(ReorderOption.FileField) },
                uiFields = fileFields,
                toggleIdentifier = toggleIdentifier,
                useThumbnailAsIcon = useThumbnailAsIcon,
                renameField = renameField,
                removeField = removeField,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    testTag = UiConstants.TestTag.Item.FileFieldsCard
                    this.text = AnnotatedString(
                        context.getQuantityString(
                            OSPlurals.itemForm_fileCard_accessibility,
                            fileFields.size,
                            fileFields.size,
                        ),
                    )
                    customActions = listOf(
                        accessibilityCustomAction(
                            itemFormActionsHolder.addNewFile.text.string(context),
                            itemFormActionsHolder.addNewFile.onClick,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
fun UiFieldCard(
    title: LbcTextSpec,
    onReorderClick: () -> Unit,
    uiFields: List<UiField>,
    toggleIdentifier: (uiField: UiField) -> Unit,
    useThumbnailAsIcon: (OSImageSpec) -> Unit,
    renameField: (id: UUID, currentName: String) -> Unit,
    removeField: (uiField: UiField) -> Unit,
    modifier: Modifier = Modifier,
) {
    OSCard(
        modifier = modifier.semantics {
            this.isTraversalGroup = true
            this.heading()
        },
    ) {
        OSRegularSpacer()
        Column(
            verticalArrangement = Arrangement.spacedBy(OSDimens.Card.ItemSpacing - OSDimens.External.OutlinedTextFieldTopPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, // Align little button with bigger ones
                modifier = Modifier.padding(
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.AlternativeSpacing.ReorderButtonSpacing,
                    bottom = OSDimens.SystemSpacing.Small,
                ),
            ) {
                OSText(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalDesignSystem.current.rowLabelColor,
                    modifier = Modifier
                        .clearAndSetSemantics { }
                        .weight(1f),
                )
                OSRegularSpacer()
                if (uiFields.size > 1) {
                    OSIconButton(
                        image = OSImageSpec.Drawable(OSDrawable.ic_reorder),
                        onClick = onReorderClick,
                        colors = OSIconButtonDefaults.secondaryIconButtonColors(state = OSActionState.Enabled),
                        buttonSize = OSDimens.SystemButtonDimension.Small,
                        contentDescription = LbcTextSpec.StringResource(OSString.safeItemDetail_reorder),
                    )
                }
            }
            uiFields.forEachIndexed { index, field ->
                field.MainComposable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = OSDimens.SystemSpacing.Small, end = OSDimens.SystemSpacing.ExtraSmall),
                    hasNext = uiFields.lastIndex != index,
                    toggleIdentifier = { toggleIdentifier(field) },
                    renameField = { renameField(field.id, it) },
                    removeField = { removeField(field) },
                    useThumbnailAsIcon = useThumbnailAsIcon,
                )
            }
        }
        OSRegularSpacer()
    }
}
