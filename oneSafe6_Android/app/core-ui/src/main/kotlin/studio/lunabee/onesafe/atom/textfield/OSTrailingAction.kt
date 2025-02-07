package studio.lunabee.onesafe.atom.textfield

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.ui.UiConstants

@Composable
fun OSTrailingAction(
    image: OSImageSpec,
    onClick: () -> Unit,
    contentDescription: LbcTextSpec?,
    modifier: Modifier = Modifier,
    testTag: String = UiConstants.TestTag.OSTrailingIcon,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .testTag(tag = testTag),
    ) {
        OSImage(
            image = image,
            contentDescription = contentDescription,
            modifier = Modifier
                .testTag(tag = image.hashCode().toString()), // when used in test, `useUnmergedTree` should be set to true
        )
    }
}
