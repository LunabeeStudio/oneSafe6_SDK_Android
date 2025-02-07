package studio.lunabee.onesafe.atom

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import studio.lunabee.compose.core.LbcTextSpec

@Composable
fun rememberOSImageState(): OSImageState {
    return remember { OSImageState() }
}

@Stable
class OSImageState internal constructor() {
    var error: Throwable? by mutableStateOf(null)
        internal set
}

@Composable
fun OSImage(
    image: OSImageSpec,
    modifier: Modifier = Modifier,
    contentDescription: LbcTextSpec? = null,
    contentScale: ContentScale = ContentScale.Fit,
    imageState: OSImageState = rememberOSImageState(),
) {
    when (image) {
        is OSImageSpec.Data, is OSImageSpec.Uri -> {
            val key = (image as? OSImageSpec.Uri)?.key
            val coilListener = remember(key) { OSImageCoilListener(imageState) }
            val painter = rememberAsyncImagePainter(
                ImageRequest
                    .Builder(LocalContext.current)
                    .memoryCacheKey(key)
                    .listener(coilListener)
                    .data(data = image.data)
                    .build(),
            )
            Image(
                modifier = modifier,
                painter = painter,
                contentDescription = null,
                contentScale = contentScale,
            )
        }
        is OSImageSpec.Drawable -> {
            if (image.isIcon) {
                Icon(
                    painter = painterResource(id = image.getAs()),
                    contentDescription = contentDescription?.string,
                    modifier = modifier,
                    tint = image.tintColor ?: LocalContentColor.current,
                )
            } else {
                Image(
                    painter = painterResource(id = image.getAs()),
                    contentDescription = contentDescription?.string,
                    modifier = modifier,
                )
            }
            imageState.error = null
        }
        is OSImageSpec.Bitmap -> {
            Image(
                modifier = modifier,
                contentDescription = contentDescription?.string,
                contentScale = contentScale,
                bitmap = (image.data as Bitmap).asImageBitmap(),
            )
            imageState.error = null
        }
    }
}

internal class OSImageCoilListener(private val imageState: OSImageState) : ImageRequest.Listener {
    override fun onError(request: ImageRequest, result: ErrorResult) {
        super.onError(request, result)
        imageState.error = result.throwable
    }

    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
        super.onSuccess(request, result)
        imageState.error = null
    }

    override fun onCancel(request: ImageRequest) {
        super.onCancel(request)
        imageState.error = null
    }

    override fun onStart(request: ImageRequest) {
        super.onStart(request)
        imageState.error = null
    }
}
