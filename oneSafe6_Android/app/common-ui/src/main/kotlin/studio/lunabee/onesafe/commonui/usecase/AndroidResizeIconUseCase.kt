package studio.lunabee.onesafe.commonui.usecase

import android.os.Build
import com.lunabee.lbextensions.downscaleAndCrop
import com.lunabee.lbextensions.downscaleAndCropPreR
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.usecase.ResizeIconUseCase
import java.io.File

class AndroidResizeIconUseCase @AssistedInject constructor(
    @Assisted("width") private val width: Int,
    @Assisted("height") private val height: Int,
    @Assisted private val tmpDir: File,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : ResizeIconUseCase {
    override suspend operator fun invoke(srcData: ByteArray): ByteArray = withContext(dispatcher) {
        val srcFile = File(tmpDir, TMP_IMPORT_ICON)
        srcFile.parentFile?.mkdirs()
        srcFile.createNewFile()
        srcFile.writeBytes(srcData)
        val resizedData = invoke(srcFile)
        srcFile.delete()
        resizedData
    }

    override suspend operator fun invoke(srcFile: File): ByteArray = withContext(dispatcher) {
        val targetFile = File(tmpDir, TMP_IMPORT_ICON_RESIZE)
        targetFile.parentFile?.mkdirs()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            srcFile.downscaleAndCrop(targetFile, width, height)
        } else {
            srcFile.downscaleAndCropPreR(targetFile, width, height)
        }
        val resizedData = targetFile.readBytes()
        targetFile.delete()
        resizedData
    }

    companion object {
        private const val TMP_IMPORT_ICON = "tmp_import_icon"
        private const val TMP_IMPORT_ICON_RESIZE = "tmp_import_icon_resize"
    }
}

@AssistedFactory
interface AndroidResizeIconUseCaseFactory {
    fun create(@Assisted("width") width: Int, @Assisted("height") height: Int, tmpDir: File): AndroidResizeIconUseCase
}
