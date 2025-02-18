package studio.lunabee.onesafe.feature.itemform.bottomsheet.newfield

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.atom.OSChipStyle
import studio.lunabee.onesafe.atom.OSChipType
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSClickableRowText
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSInputChip
import studio.lunabee.onesafe.atom.OSRegularDivider
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.feature.itemform.model.ItemFieldType
import studio.lunabee.onesafe.feature.itemform.model.NotImplementedItemFieldType
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun ItemFormNewFieldBottomSheetContent(
    onNewFieldRequested: (itemFieldType: ItemFieldType) -> Unit,
    paddingValues: PaddingValues,
    itemFieldTypeSections: List<List<ItemFieldType>> = listOf(
        ItemFieldType.getMainItemFieldTypes(),
        ItemFieldType.getTimeItemFieldTypes(),
        ItemFieldType.getSensitiveItemFieldType(),
    ),
    context: Context = LocalContext.current,
) {
    BottomSheetHolderColumnContent(
        modifier = Modifier
            .testTag(UiConstants.TestTag.BottomSheet.ItemFormNewFieldBottomSheet),
        paddingValues = paddingValues,
    ) {
        itemFieldTypeSections.forEachIndexed { sectionIndex, itemSection ->
            itemSection
                .sortedBy { it.titleText.string(context) }
                .forEachIndexed { itemFieldIndex, itemFieldType ->
                    key("$sectionIndex" + "$itemFieldIndex") {
                        Box(
                            modifier = Modifier
                                .height(IntrinsicSize.Max)
                                .fillMaxWidth(),
                        ) {
                            val notImplemented = itemFieldType is NotImplementedItemFieldType
                            val state = if (notImplemented) {
                                OSActionState.DisabledWithAction
                            } else {
                                OSActionState.Enabled
                            }
                            OSClickableRow(
                                modifier = Modifier
                                    .clearAndSetSemantics { },
                                onClick = {
                                    // handled by sibling box
                                },
                                state = state,
                                label = { modifier ->
                                    if (notImplemented) {
                                        NotImplementedRowText(modifier, itemFieldType)
                                    } else {
                                        OSClickableRowText(text = itemFieldType.titleText, modifier = modifier)
                                    }
                                },
                                contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                                    index = itemFieldIndex,
                                    elementsCount = itemSection.size,
                                ),
                                leadingIcon = {
                                    OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = itemFieldType.iconRes))
                                },
                            )
                            // Use sibling box to handle interaction so the whole row is clickable, including the chip
                            // https://issuetracker.google.com/issues/289087869#comment7
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onNewFieldRequested(itemFieldType) }
                                    .composed {
                                        val templateText: AnnotatedString
                                        val clickLabel: String?
                                        if (notImplemented) {
                                            templateText = AnnotatedString(
                                                "${itemFieldType.titleText.string}.${stringResource(id = OSString.common_coming)}",
                                            )
                                            clickLabel = null
                                        } else {
                                            templateText = itemFieldType.titleText.annotated
                                            clickLabel = stringResource(
                                                OSString.safeItem_form_accessibility_addNewField,
                                                itemFieldType.titleText.string,
                                            )
                                        }
                                        semantics {
                                            text = templateText
                                            role = Role.Button
                                            if (notImplemented) {
                                                this.disabled()
                                            }
                                            accessibilityClick(label = clickLabel) {
                                                onNewFieldRequested(itemFieldType)
                                            }
                                        }
                                    },
                            )
                        }
                    }
                }

            if (sectionIndex < itemFieldTypeSections.count() - 1) {
                key(sectionIndex) {
                    OSRegularDivider(
                        modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                    )
                }
            }
        }
    }
}

@Composable
private fun NotImplementedRowText(modifier: Modifier, itemFieldType: ItemFieldType) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
    ) {
        OSClickableRowText(text = itemFieldType.titleText, modifier = Modifier.weight(1f))
        OSInputChip(
            selected = true,
            onClick = null,
            type = OSChipType.Progress,
            style = OSChipStyle.Small,
            label = {
                OSText(
                    text = LbcTextSpec.StringResource(OSString.common_coming),
                )
            },
        )
    }
}
