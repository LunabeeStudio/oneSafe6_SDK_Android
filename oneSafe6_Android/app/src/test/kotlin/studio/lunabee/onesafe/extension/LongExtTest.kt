package studio.lunabee.onesafe.extension

import org.junit.Test
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.common.extensions.byteToHumanReadable
import kotlin.test.assertEquals

class LongExtTest {

    @Test
    fun convert_long_to_human_readable_test() {
        val testCase = listOf(
            Triple(0L, "0", OSString.fileSize_bytes),
            Triple(1000L, "1000", OSString.fileSize_bytes),
            Triple(1024L, "1", OSString.fileSize_kilo),
            Triple(1025L, "1", OSString.fileSize_kilo),
            Triple(1433L, "1", OSString.fileSize_kilo),
            Triple(2000L, "2", OSString.fileSize_kilo),
            Triple(20_000L, "20", OSString.fileSize_kilo),
            Triple(20_000_000L, "19", OSString.fileSize_mega),
            Triple(20_000_000_000L, "19", OSString.fileSize_giga),
            Triple(20_000_000_000_000L, "18", OSString.fileSize_tera),
        )

        testCase.forEach {
            assertEquals(it.first.byteToHumanReadable(), it.second to it.third)
        }
    }
}
