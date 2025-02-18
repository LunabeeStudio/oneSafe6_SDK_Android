package studio.lunabee.onesafe.commonui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.test.OSTestConfig
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertContentEquals

class HomeInfoDataTest {
    @Test
    fun compare_by_test() {
        val expected = listOf(
            TestHomeInfoData(HomeInfoType.Error, 0, 0, Instant.EPOCH.plusMillis(1)),
            TestHomeInfoData(HomeInfoType.Error, 0, 0, Instant.EPOCH),
            TestHomeInfoData(HomeInfoType.Standard, 0, 0, Instant.EPOCH.plusMillis(2)),
            TestHomeInfoData(HomeInfoType.Standard, 0, 0, Instant.EPOCH),
        )
        val actual = expected.shuffled(OSTestConfig.random).sorted()
        assertContentEquals(expected, actual)
    }

    class TestHomeInfoData(type: HomeInfoType, key: Any, contentType: Any, visibleSince: Instant) : HomeInfoData(
        type = type,
        key = key,
        contentType = contentType,
        visibleSince = visibleSince,
    ) {
        context(HomeInfoDataNavScope)
        @Composable
        override fun Composable(modifier: Modifier) {
        }
    }
}
