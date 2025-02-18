package studio.lunabee.onesafe.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.drawableSample
import studio.lunabee.onesafe.model.OSGeneratedPasswordOption
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSTypography
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSGeneratedPassword(
    password: String,
    options: List<OSGeneratedPasswordOption>,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(OSDimens.SystemSpacing.Large))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OSText(
            text = LbcTextSpec.Annotated(visualTransformation.filter(AnnotatedString(password)).text),
            fontFamily = OSTypography.Legibility,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .weight(1f)
                .padding(
                    horizontal = OSDimens.SystemSpacing.Regular,
                    vertical = OSDimens.SystemSpacing.Small,
                )
                .testTag(UiConstants.TestTag.Item.GeneratedPasswordText)
                .semantics {
                    text = AnnotatedString(password)
                    liveRegion = LiveRegionMode.Assertive
                },
        )

        options.forEach { option ->
            option.Content(
                paddingValues = PaddingValues(OSDimens.SystemSpacing.ExtraSmall),
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun OSGeneratedPasswordNoOptionPreview() {
    OSPreviewBackgroundTheme {
        OSGeneratedPassword(
            password = "MyPassword",
            options = listOf(),
        )
    }
}

@OsDefaultPreview
@Composable
fun OSGeneratedPasswordOptionsPreview() {
    OSPreviewBackgroundTheme {
        OSGeneratedPassword(
            password = "MyPassword",
            options = listOf(
                OSGeneratedPasswordOption.Primary(
                    icon = drawableSample,
                    onClick = { },
                    contentDescription = null,
                ),
            ),
        )
    }
}

@OsDefaultPreview
@Composable
fun OSGeneratedPasswordOptionsMultiplePreview() {
    OSPreviewBackgroundTheme {
        OSGeneratedPassword(
            password = "MyPassword",
            options = listOf(
                OSGeneratedPasswordOption.Primary(
                    icon = drawableSample,
                    onClick = { },
                    contentDescription = null,
                ),
                OSGeneratedPasswordOption.Primary(
                    icon = drawableSample,
                    onClick = { },
                    contentDescription = null,
                ),
            ),
        )
    }
}

@OsDefaultPreview
@Composable
fun OSGeneratedPasswordOptionsWithVisualTransformationPreview() {
    OSPreviewBackgroundTheme {
        OSGeneratedPassword(
            password = "MyPassword",
            options = listOf(
                OSGeneratedPasswordOption.Primary(
                    icon = drawableSample,
                    onClick = { },
                    contentDescription = null,
                ),
            ),
            visualTransformation = PasswordVisualTransformation(),
        )
    }
}
