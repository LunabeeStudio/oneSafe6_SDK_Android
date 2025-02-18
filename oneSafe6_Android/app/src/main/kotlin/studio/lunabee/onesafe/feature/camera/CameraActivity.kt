package studio.lunabee.onesafe.feature.camera

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.lunabee.lbextensions.getSerializableExtraCompat
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig
import studio.lunabee.onesafe.feature.camera.model.OSMediaType
import studio.lunabee.onesafe.feature.camera.model.Orientation
import studio.lunabee.onesafe.feature.itemform.manager.InAppMediaCapture
import java.io.File

private val logger = LBLogger.get<CameraActivity>()

@AndroidEntryPoint
class CameraActivity : FragmentActivity() {
    private val viewModel by viewModels<CameraActivityViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<CameraViewModelFactory> { factory ->
                factory.create(captureConfig, thumbnailFile, captureFile)
            }
        },
    )

    val captureConfig: CaptureConfig by lazy {
        val argCaptureConfig: CaptureConfig? = intent.getSerializableExtraCompat(ExtraCaptureConfig)
        if (argCaptureConfig == null) {
            logger.e("Missing capture target param in intent extra")
            setResult(RESULT_CANCELED)
            finish()
            CaptureConfig.FieldFile // not used -> finish
        } else {
            argCaptureConfig
        }
    }

    private val thumbnailFile: File? by lazy { IntentCompat.getParcelableExtra(intent, ExtraThumbnailUri, Uri::class.java)?.toFile() }
    private val captureFile: File? by lazy { IntentCompat.getParcelableExtra(intent, ExtraCaptureUri, Uri::class.java)?.toFile() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrim = android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(scrim = android.graphics.Color.TRANSPARENT),
        )

        setupOrientationListener()

        // Handle autolock (in background) by finishing the activity
        lifecycleScope.launch {
            viewModel.cameraUiState.collectLatest {
                if (!it.isCryptoLoaded) {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        }

        setContent {
            CameraActivityScreen(
                viewModel = viewModel,
                onSuccess = { thumbnailUri, captureUri, mediaType ->
                    val data = Intent()
                        .putExtra(ExtraThumbnailUri, thumbnailUri)
                        .putExtra(ExtraCaptureUri, captureUri)
                        .putExtra(ExtraMediaType, mediaType)
                    setResult(RESULT_OK, data)
                    finish()
                },
                onCancel = {
                    setResult(RESULT_CANCELED)
                    finish()
                },
                captureConfig,
            )
        }
    }

    private fun setupOrientationListener() {
        val orientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                val rotation = ContextCompat.getDisplayOrDefault(this@CameraActivity).rotation
                // Fix orientation for device with landscape natural orientation (ie tablets)
                val fixedOrientation = orientation + when (rotation) {
                    Surface.ROTATION_90 -> 90
                    Surface.ROTATION_180 -> -180
                    Surface.ROTATION_270 -> -270
                    else -> 0
                }
                viewModel.updateOrientation(Orientation.getOrientation(fixedOrientation))
            }
        }
        orientationEventListener.enable()
    }

    data class Input(
        val captureConfig: CaptureConfig,
        val inAppPhotoCapture: InAppMediaCapture,
    )

    class Contract : ActivityResultContract<Input, Pair<Int, InAppMediaCapture?>>() {
        override fun createIntent(context: Context, input: Input): Intent {
            return Intent(context, CameraActivity::class.java)
                .putExtra(ExtraCaptureConfig, input.captureConfig)
                .putExtra(ExtraThumbnailUri, input.inAppPhotoCapture.plainThumbnailFile?.toUri())
                .putExtra(ExtraCaptureUri, input.inAppPhotoCapture.encryptedFile?.toUri())
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Pair<Int, InAppMediaCapture?> {
            return resultCode to intent?.let {
                InAppMediaCapture(
                    plainThumbnailFile = IntentCompat.getParcelableExtra(intent, ExtraThumbnailUri, Uri::class.java)?.toFile(),
                    encryptedFile = IntentCompat.getParcelableExtra(intent, ExtraCaptureUri, Uri::class.java)?.toFile(),
                    mediaType = intent.getSerializableExtraCompat(ExtraMediaType) ?: OSMediaType.PHOTO,
                )
            }
        }
    }

    companion object {
        private const val ExtraCaptureConfig: String = "CAPTURE_CONFIG"
        private const val ExtraThumbnailUri: String = "THUMBNAIL_URI"
        private const val ExtraCaptureUri: String = "CAPTURE_URI"
        private const val ExtraMediaType: String = "MEDIA_TYPE"
        const val VerticalImageBias: Float = -0.8f
    }
}
