package studio.lunabee.onesafe.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSDialogContent(
    title: LbcTextSpec,
    content: @Composable (ColumnScope.() -> Unit),
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null,
) {
    OSCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .padding(all = OSDimens.SystemDialog.DefaultPadding),
        ) {
            OSText(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
            OSRegularSpacer()
            content(this)
            OSRegularSpacer()
            Box(modifier = Modifier.align(Alignment.End)) {
                actions?.let {
                    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                    androidx.compose.material3.AlertDialogFlowRow(
                        mainAxisSpacing = ButtonsMainAxisSpacing,
                        crossAxisSpacing = ButtonsCrossAxisSpacing,
                        content = it,
                    )
                }
            }
        }
    }
}

// Copied from androidx/compose/material3/AndroidAlertDialog.android.kt
private val ButtonsMainAxisSpacing = 8.dp
private val ButtonsCrossAxisSpacing = 12.dp

@Composable
@OsDefaultPreview
private fun OSDialogContentPreview() {
    OSTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OSDialogContent(
                title = loremIpsumSpec(2),
                content = {
                    OSText(text = loremIpsumSpec(20))
                },
            ) {
                OSTextButton(text = LbcTextSpec.Raw("Cancel"), onClick = {})
                OSTextButton(text = LbcTextSpec.Raw("OK"), onClick = {})
            }

            OSDialogContent(
                title = loremIpsumSpec(2),
                content = {
                    OSText(text = loremIpsumSpec(20))
                },
            ) {
                OSTextButton(text = LbcTextSpec.Raw("Cancel ${loremIpsum(3)}"), onClick = {})
                OSTextButton(text = LbcTextSpec.Raw("OK ${loremIpsum(3)}"), onClick = {})
            }
        }
    }
}
