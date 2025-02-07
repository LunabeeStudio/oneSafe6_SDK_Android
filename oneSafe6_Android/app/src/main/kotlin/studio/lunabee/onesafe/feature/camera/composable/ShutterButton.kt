package studio.lunabee.onesafe.feature.camera.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun ShutterButton(
    modifier: Modifier,
    onClick: () -> Unit,
    isLoading: Boolean,
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .testTag(UiConstants.TestTag.Item.ShutterButton)
                .clip(CircleShape)
                .size(OSDimens.Camera.shutterButtonSize)
                .background(MaterialTheme.colorScheme.background)
                .padding(OSDimens.SystemSpacing.Medium),
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    } else {
        Box(
            modifier = modifier
                .testTag(UiConstants.TestTag.Item.ShutterButton)
                .clip(CircleShape)
                .size(OSDimens.Camera.shutterButtonSize)
                .clickable(
                    role = Role.Button,
                    onClick = onClick,
                    onClickLabel = LbcTextSpec.StringResource(OSString.accessibility_camera_takePhoto).string,
                )
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(OSDimens.SystemSpacing.ExtraSmall)
                .clip(CircleShape)
                .background(Color.Black)
                .padding(OSDimens.SystemSpacing.Small)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
        )
    }
}
