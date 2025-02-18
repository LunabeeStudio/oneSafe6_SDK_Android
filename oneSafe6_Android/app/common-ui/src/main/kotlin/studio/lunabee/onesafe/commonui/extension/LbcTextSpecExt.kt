package studio.lunabee.onesafe.commonui.extension

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.ui.UiConstants

@Composable
fun LbcTextSpec.getNameForBreadcrumb(isAtLastPosition: Boolean): LbcTextSpec =
    if (isAtLastPosition && string.length > UiConstants.Text.MaxLetterNavigationItem) {
        LbcTextSpec.Raw("${string.take(UiConstants.Text.MaxLetterTruncatedNavigationItem)}…")
    } else {
        this
    }

@Composable
fun LbcTextSpec.markdown(): LbcTextSpec = LbcTextSpec.Annotated(string.markdownToAnnotatedString())
