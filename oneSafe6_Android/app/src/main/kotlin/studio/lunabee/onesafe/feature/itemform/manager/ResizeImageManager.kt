package studio.lunabee.onesafe.feature.itemform.manager

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.usecase.ResizeIconUseCase
import javax.inject.Inject

class ResizeImageManager @Inject constructor(
    private val resizeIconUseCase: ResizeIconUseCase,
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) {
    suspend fun resizeImage(context: Context, image: OSImageSpec): OSImageSpec {
        return when (image) {
            is OSImageSpec.Data,
            is OSImageSpec.Drawable,
            is OSImageSpec.Bitmap,
            -> image // not expected to be resized as it came from our resources or database (i.e already resized).
            is OSImageSpec.Uri -> {
                withContext(dispatcher) {
                    if (image.getAs<Uri>().scheme == AppConstants.FileProvider.SchemeProvider) {
                        context.contentResolver.openInputStream(image.getAs())?.use { it.readBytes() }?.let { imgData ->
                            OSImageSpec.Data(data = resizeIconUseCase(srcData = imgData))
                        }
                    } else {
                        val srcData = image.getAs<Uri>().toFile().readBytes()
                        OSImageSpec.Data(data = resizeIconUseCase(srcData = srcData))
                    } ?: image // will not be resized in case of any issue.
                }
            }
        }
    }
}
