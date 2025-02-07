package studio.lunabee.onesafe.commonui

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lunabee.lbloading.DefaultLoadingContent
import com.lunabee.lbloading.LoadingView

@Composable
fun OSLoadingView() {
    LoadingView(
        contentDescription = stringResource(R.string.common_accessibility_loadingInProgress),
    ) {
        DefaultLoadingContent {
            LoadingLottie(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun LoadingLottie(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.loader),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        reverseOnRepeat = true,
        iterations = LottieConstants.IterateForever,
        clipSpec = LottieClipSpec.Frame(min = LottieStartFrame, max = LottieStopFrame),
    )
    LottieAnimation(
        modifier = modifier
            .size(LottieSize),
        composition = composition,
        enableMergePaths = true,
        progress = { progress },
    )
}

private val LottieSize: Dp = 125.dp
private const val LottieStartFrame: Int = 7
private const val LottieStopFrame: Int = 64
