package studio.lunabee.onesafe.molecule

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityInvisibleToUser
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun OSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    stepsNumber: Int = 0,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    Column(
        modifier = modifier,
    ) {
        val sliderLabel = label.string
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .testTag(UiConstants.TestTag.Item.Slider)
                .semantics {
                    stateDescription = sliderLabel
                },
            steps = stepsNumber,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                activeTickColor = MaterialTheme.colorScheme.primary,
                inactiveTickColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        )

        OSText(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .padding(top = OSDimens.SystemSpacing.ExtraSmall)
                .fillMaxWidth()
                .accessibilityInvisibleToUser(),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun OSSliderPreview() {
    OSTheme {
        OSSlider(
            value = 0.5f,
            onValueChange = {},
            label = LbcTextSpec.Raw("Label of the slider"),
        )
    }
}
