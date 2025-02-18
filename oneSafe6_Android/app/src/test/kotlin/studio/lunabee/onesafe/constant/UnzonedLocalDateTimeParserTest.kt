package studio.lunabee.onesafe.constant

import org.junit.Test
import studio.lunabee.onesafe.AppConstants
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals

class UnzonedLocalDateTimeParserTest {
    private val parser: DateTimeFormatter = AppConstants.Ui.TimeRelatedFieldFormatter.UnzonedLocalDateTimeParser

    @Test
    fun parse_with_timezone_test() {
        val expected = LocalDateTime.of(2023, 3, 7, 13, 27, 58)
        val actualZulu = LocalDateTime.parse("2023-03-07T13:27:58Z", parser)
        val actualGmt5 = LocalDateTime.parse("2023-03-07T13:27:58+05:00", parser)

        assertEquals(expected, actualZulu)
        assertEquals(expected, actualGmt5)
    }

    @Test
    fun parse_without_timezone_test() {
        val expected = LocalDateTime.of(2023, 3, 7, 13, 27, 58)
        val actual = LocalDateTime.parse("2023-03-07T13:27:58", parser)

        assertEquals(expected, actual)
    }
}
