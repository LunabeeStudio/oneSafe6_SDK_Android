package studio.lunabee.onesafe.feature.camera.delegate

import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.core.content.ContextCompat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.common.extensions.byteToHumanReadable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.feature.camera.model.RecordTimerInfo
import studio.lunabee.onesafe.feature.camera.model.RecordingState
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

interface VideoDelegate {
    fun resumeRecording()
    fun pauseRecording()
    val timerInfo: StateFlow<RecordTimerInfo?>
    val videoCaptured: StateFlow<File?>
    val videoSnackbarData: StateFlow<SnackbarState?>
    val recordingState: StateFlow<RecordingState>
    fun deleteVideoCaptured()
    fun recordVideo(controller: LifecycleCameraController, context: Context, isSoundEnabled: Boolean)
}

class VideoDelegateImpl @Inject constructor(
    val fileRepository: FileRepository,
) :
    VideoDelegate, CloseableCoroutineScope by CloseableMainCoroutineScope() {

    private val recording: MutableStateFlow<Recording?> = MutableStateFlow(null)

    private val _recordingState: MutableStateFlow<RecordingState> = MutableStateFlow(RecordingState.Idle)
    override val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _videoSnackbarData: MutableStateFlow<SnackbarState?> = MutableStateFlow(null)
    override val videoSnackbarData: StateFlow<SnackbarState?> = _videoSnackbarData.asStateFlow()

    private val _videoCaptured: MutableStateFlow<File?> = MutableStateFlow(null)
    override val videoCaptured: StateFlow<File?> = _videoCaptured.asStateFlow()
    private val tempVideoFile: File = fileRepository.createTempFile()

    override fun deleteVideoCaptured() {
        _videoCaptured.value?.delete()
        _videoCaptured.value = null
    }

    private fun stopRecording() {
        recording.value?.stop()
        _recordingState.value = RecordingState.Idle
        recording.value = null
    }

    override fun pauseRecording() {
        _recordingState.value = RecordingState.Paused
        recording.value?.pause()
    }

    override fun resumeRecording() {
        _recordingState.value = RecordingState.Recording
        recording.value?.resume()
    }

    override fun recordVideo(controller: LifecycleCameraController, context: Context, isSoundEnabled: Boolean) {
        if (recording.value != null) {
            stopRecording()
        } else {
            tempVideoFile.delete()

            // Permission handled in SoundTrailingAction
            // TODO permission should be add by the module if it needs it
            @SuppressLint("MissingPermission")
            val audioConfig = if (isSoundEnabled) {
                AudioConfig.create(true)
            } else {
                AudioConfig.AUDIO_DISABLED
            }
            _recordingState.value = RecordingState.Recording
            recording.value = controller.startRecording(
                FileOutputOptions.Builder(tempVideoFile).build(),
                audioConfig,
                ContextCompat.getMainExecutor(context),
            ) { event ->
                when (event) {
                    is VideoRecordEvent.Finalize -> {
                        coroutineScope.launch {
                            if (event.hasError()) {
                                recording.value?.close()
                                recording.value = null
                            } else {
                                recording.value?.close()
                                recording.value = null
                                _videoCaptured.value = tempVideoFile
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val timerInfo: StateFlow<RecordTimerInfo?> = recording.flatMapLatest { recording: Recording? ->
        recording?.let {
            flow {
                val startAtTime: Long = System.currentTimeMillis()
                var timeElapsed = 0L
                var pauseTime = 0L
                while (true) {
                    if (recordingState.value == RecordingState.Recording) {
                        val actualFileSize = tempVideoFile.length()
                        if (actualFileSize > Constant.FileMaxSizeBytes) {
                            stopRecording()
                            _videoSnackbarData.value = object : SnackbarState() {
                                override val message: LbcTextSpec
                                    get() = LbcTextSpec.StringResource(
                                        OSString.cameraScreen_fileMaxSize_message,
                                        Constant.FileMaxSizeMegaBytes,
                                    )
                            }
                        }
                        timeElapsed = System.currentTimeMillis() - startAtTime - pauseTime
                        val timerInfo = RecordTimerInfo(
                            timer = timeElapsed.milliseconds,
                            fileSizeInfo = actualFileSize.byteToHumanReadable(),
                        )
                        emit(timerInfo)
                    } else {
                        pauseTime = (System.currentTimeMillis() - startAtTime) - timeElapsed
                    }
                    delay(100.milliseconds)
                }
            }
        } ?: flowOf<RecordTimerInfo?>(null)
    }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(), null)

    override fun close() {
        coroutineScope.cancel()
        tempVideoFile.delete()
    }
}
