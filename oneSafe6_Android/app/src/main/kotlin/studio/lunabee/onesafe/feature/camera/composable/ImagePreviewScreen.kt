package studio.lunabee.onesafe.feature.camera.composable

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.lunabee.lbloading.LoadingBackHandler
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityClearForInvisibilityToUser
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.feature.camera.CameraActivity
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionNav
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ImagePreviewScreen(
    bitmap: Bitmap,
    onNavigateBack: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean,
    captureConfig: CaptureConfig,
) {
    val accessibilityState = rememberOSAccessibilityState()
    val context = LocalContext.current

    LaunchedEffect(bitmap) {
        if (accessibilityState.isAccessibilityEnabled) {
            Toast.makeText(
                context,
                LbcTextSpec.StringResource(OSString.accessibility_camera_shootSuccess)
                    .string(context),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    LoadingBackHandler {
        if (!isLoading) {
            onNavigateBack()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        OSTopAppBar(
            modifier = Modifier
                .systemBarsPadding()
                .accessibilityClearForInvisibilityToUser(),
            options = listOf(
                TopAppBarOptionNav(
                    image = OSImageSpec.Drawable(OSDrawable.ic_close),
                    contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
                    onClick = onNavigateBack,
                    state = if (isLoading) OSActionState.Disabled else OSActionState.Enabled,
                ),
            ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .zoomable(rememberZoomState()),
        ) {
            OSImage(
                image = OSImageSpec.Bitmap(bitmap),
                contentDescription = null,
                modifier = captureConfig.modifier
                    // Use same arbitrary bias than capture to avoid image translation
                    .align(BiasAlignment(0f, CameraActivity.VerticalImageBias)),
                contentScale = ContentScale.FillWidth,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = OSDimens.SystemSpacing.Regular),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            CameraOptionButton(
                onClick = onNavigateBack,
                icon = OSDrawable.ic_undo,
                contentDescription = LbcTextSpec.StringResource(OSString.common_cancel),
                modifier = if (isLoading) {
                    Modifier.accessibilityClearForInvisibilityToUser()
                } else {
                    Modifier
                },
            )
            ConfirmButton(
                isLoading = isLoading,
                onClick = onConfirm,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Huge),
            )
            Box(Modifier.size(OSDimens.SystemButtonDimension.Large.container.dp))
        }
    }
}

@Composable
fun ConfirmButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    if (isLoading) {
        Box(
            modifier = modifier
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
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .size(OSDimens.Camera.shutterButtonSize)
                .background(MaterialTheme.colorScheme.background)
                .padding(OSDimens.SystemSpacing.Medium)
                .testTag(UiConstants.TestTag.Item.CameraPreviewConfirmButton),
        ) {
            OSImage(
                image = OSImageSpec.Drawable(OSDrawable.ic_check),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun ImagePreviewScreenFieldPreview() {
    Surface(color = Color.Black) {
        ImagePreviewScreen(
            bitmap = BitmapFactory.decodeByteArray(iconSample, 0, iconSample.size),
            onNavigateBack = {},
            onConfirm = {},
            isLoading = false,
            captureConfig = CaptureConfig.FieldFile,
        )
    }
}

@OsDefaultPreview
@Composable
private fun ImagePreviewScreenItemPreview() {
    Surface(color = Color.Black) {
        ImagePreviewScreen(
            bitmap = BitmapFactory.decodeByteArray(iconSample, 0, iconSample.size),
            onNavigateBack = {},
            onConfirm = {},
            isLoading = false,
            captureConfig = CaptureConfig.ItemIcon,
        )
    }
}
