package studio.lunabee.onesafe.commonui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.Instant

interface HomeInfoDataNavScope {
    val navigateFromHomeInfoDataToBackupSettings: () -> Unit
    val navigateFromHomeInfoDataToBubblesOnBoarding: () -> Unit
}

abstract class HomeInfoData(
    val type: HomeInfoType,
    val key: Any,
    val contentType: Any,
    protected val visibleSince: Instant,
) : Comparable<HomeInfoData> {
    context(HomeInfoDataNavScope)
    @Composable
    abstract fun Composable(modifier: Modifier)

    override fun compareTo(other: HomeInfoData): Int {
        return compareBy<HomeInfoData>({ it.type.ordinal }, { -it.visibleSince.toEpochMilli() }).compare(this, other)
    }
}
