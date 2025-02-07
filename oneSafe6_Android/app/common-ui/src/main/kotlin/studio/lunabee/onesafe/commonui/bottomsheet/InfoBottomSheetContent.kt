package studio.lunabee.onesafe.commonui.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.OSRegularDivider
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun InfoBottomSheetContent(
    paddingValues: PaddingValues,
    title: LbcTextSpec,
    description: LbcTextSpec,
    primaryAction: Pair<LbcTextSpec, () -> Unit>? = null,
    secondaryAction: Pair<LbcTextSpec, () -> Unit>? = null,
    attributes: InfoBottomSheetContentAttributes = InfoBottomSheetContentAttributes(),
) {
    val actionContent: @Composable (BoxScope.() -> Unit)? = if (primaryAction != null || secondaryAction != null) {
        {
            Row(
                horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.ExtraLarge),
            ) {
                secondaryAction?.let { action ->
                    OSFilledButton(
                        text = action.first,
                        onClick = action.second,
                        buttonColors = OSFilledButtonDefaults.secondaryButtonColors(),
                    )
                }
                primaryAction?.let { action ->
                    OSFilledButton(
                        text = action.first,
                        onClick = action.second,
                    )
                }
            }
        }
    } else {
        null
    }
    InfoBottomSheetContent(
        paddingValues = paddingValues,
        title = title,
        description = description,
        attributes = attributes,
        actionContent = actionContent,
    )
}

@Composable
fun InfoBottomSheetContent(
    paddingValues: PaddingValues,
    title: LbcTextSpec,
    description: LbcTextSpec,
    attributes: InfoBottomSheetContentAttributes = InfoBottomSheetContentAttributes(),
    actionContent: @Composable (BoxScope.() -> Unit)?,
) {
    val verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular)
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = Modifier
            .testTag(attributes.testTag)
            .background(MaterialTheme.colorScheme.surface)
            .padding(OSDimens.SystemSpacing.Regular),
        verticalArrangement = verticalArrangement,
    ) {
        Column(
            verticalArrangement = verticalArrangement,
            modifier = Modifier.accessibilityMergeDescendants(),
        ) {
            OSText(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = if (attributes.isTitleCentred) {
                    TextAlign.Center
                } else {
                    TextAlign.Left
                },
            )

            OSText(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        actionContent?.let {
            OSRegularDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(top = OSDimens.SystemSpacing.Small),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                actionContent()
            }
        }
    }
}

class InfoBottomSheetContentAttributes {
    var isTitleCentred: Boolean = false
    var testTag: String = ""

    fun titleCenter(): InfoBottomSheetContentAttributes =
        apply {
            isTitleCentred = true
        }

    fun testTag(tag: String): InfoBottomSheetContentAttributes =
        apply {
            testTag = tag
        }
}

@OsDefaultPreview
@Composable
fun InfoBottomSheetContentPreview() {
    OSPreviewBackgroundTheme {
        InfoBottomSheetContent(
            paddingValues = PaddingValues(0.dp),
            title = LbcTextSpec.Raw("Title"),
            description = LbcTextSpec.Raw("Description"),
            primaryAction = LbcTextSpec.Raw("Primary action") to {},
            secondaryAction = LbcTextSpec.Raw("Secondary action") to {},
        )
    }
}

@OsDefaultPreview
@Composable
fun InfoBottomSheetContentNoActionPreview() {
    OSPreviewBackgroundTheme {
        InfoBottomSheetContent(
            paddingValues = PaddingValues(0.dp),
            title = LbcTextSpec.Raw("Title"),
            description = LbcTextSpec.Raw("Description"),
        )
    }
}
