package studio.lunabee.onesafe.feature.onboarding.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.extension.rtl
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun PresentationStepLayout(
    presentationStep: PresentationStep,
    modifier: Modifier = Modifier,
) {
    val localDensity = LocalDensity.current
    var isImageDisplayed by remember(localDensity) { mutableStateOf(true) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val scrollState: ScrollState = rememberScrollState()

    Column(
        modifier =
        modifier
            .fillMaxSize()
            .padding(horizontal = OSDimens.SystemSpacing.Regular)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isImageDisplayed) {
            Image(
                painter = painterResource(id = presentationStep.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .rtl(LocalLayoutDirection.current)
                    .weight(1f)
                    .padding(bottom = OSDimens.SystemSpacing.Regular)
                    .onPlaced {
                        with(localDensity) {
                            isImageDisplayed =
                                it.size.height.toDp() > screenHeight * AppConstants.Ui.AppPresentation.MinHeightRatioImageEmptyScreen
                        }
                    },
            )
        }

        if (presentationStep.title != null || presentationStep.description != null) {
            Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
                presentationStep.title?.let {
                    OSText(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (presentationStep.title != null && presentationStep.description != null) {
                    Spacer(modifier = Modifier.height(OSDimens.SystemSpacing.Regular))
                }

                presentationStep.description?.let {
                    OSText(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }

        if (presentationStep.actions.isEmpty()) {
            Spacer(
                modifier = Modifier
                    .height(ButtonDefaults.MinHeight)
                    .padding(ButtonDefaults.ContentPadding)
                    .padding(top = OSDimens.SystemSpacing.Regular),
            )
        } else {
            presentationStep.actions.forEach { action ->
                val stepModifier = Modifier
                    .padding(top = OSDimens.SystemSpacing.Regular)
                    .run {
                        if (action.attributes.fillMaxWidth) {
                            fillMaxWidth()
                        } else {
                            this
                        }
                    }
                if (action.attributes.filled) {
                    OSFilledButton(
                        text = action.label,
                        onClick = action.action,
                        modifier = stepModifier,
                        state = action.attributes.state,
                        leadingIcon = action.attributes.leadingIcon,
                    )
                } else {
                    OSTextButton(
                        text = action.label,
                        onClick = action.action,
                        modifier = stepModifier,
                        state = action.attributes.state,
                        leadingIcon = action.attributes.leadingIcon,
                    )
                }
            }
        }
    }
}

@Composable
@OsDefaultPreview
fun PresentationStepLayoutPreview() {
    OSPreviewOnSurfaceTheme {
        PresentationStepLayout(
            PresentationStep(
                title = loremIpsumSpec(2),
                description = loremIpsumSpec(30),
                imageRes = OSDrawable.colored_illustration_type_coffre,
                actions = listOf(PresentationAction(label = loremIpsumSpec(1), action = { })),
            ),
        )
    }
}
