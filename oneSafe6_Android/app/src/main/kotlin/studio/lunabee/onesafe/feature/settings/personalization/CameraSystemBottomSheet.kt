package studio.lunabee.onesafe.feature.settings.personalization

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraSystemBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onSelect: (CameraSystem) -> Unit,
    selectedCameraSystem: CameraSystem,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        BottomSheetHolderColumnContent(
            paddingValues = paddingValues,
            modifier = Modifier.wrapContentHeight(),
        ) {
            OSText(
                text = LbcTextSpec.StringResource(id = OSString.settings_personalization_cameraSystem_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(all = OSDimens.SystemSpacing.Regular),
            )
            UiCameraSystem.entries.forEachIndexed { index, entry ->
                OSRow(
                    text = entry.title,
                    secondaryText = entry.description,
                    textMaxLines = Int.MAX_VALUE,
                    startContent = {
                        OSImage(
                            image = entry.imageSpec,
                            modifier = Modifier.size(OSDimens.SystemButton.Small),
                        )
                    },
                    endContent = {
                        if (entry.cameraSystem == selectedCameraSystem) {
                            Icon(
                                painter = painterResource(id = OSDrawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                onSelect(entry.cameraSystem)
                                closeBottomSheet()
                            },
                        )
                        .padding(
                            LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                                index = index,
                                elementsCount = UiCameraSystem.entries.size,
                            ),
                        ),
                )
            }
        }
    }
}
