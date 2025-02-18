package studio.lunabee.onesafe.common.model

import org.junit.Test
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSString
import kotlin.test.assertEquals

class DefaultNameProviderTest {

    @Test
    fun truncatedName_test() {
        val maxLength = AppConstants.Ui.ItemNameTruncateLength
        val tests = listOf(
            Pair(null, LbcTextSpec.StringResource(id = OSString.common_noName)),
            Pair("", LbcTextSpec.StringResource(id = OSString.common_noName)),
            Pair(" ", LbcTextSpec.Raw(" ")),
            Pair("test", LbcTextSpec.Raw("test")),
            Pair("test ", LbcTextSpec.Raw("test ")),
            Pair("a".repeat(maxLength), LbcTextSpec.Raw("a".repeat(maxLength))),
            Pair("a".repeat(maxLength + 5), LbcTextSpec.Raw("a".repeat(maxLength) + "…")),
        )
        tests.forEach { test ->
            val value = test.first
            val expectedResult = test.second
            val truncatedName: LbcTextSpec = DefaultNameProvider(value).truncatedName
            assertEquals(expectedResult, truncatedName)
        }
    }

    @Test
    fun placeholder_test() {
        val tests = listOf(
            "    Test" to LbcTextSpec.Raw("T"),
            "Test" to LbcTextSpec.Raw("T"),
            "Test " to LbcTextSpec.Raw("T"),
            "Test Test " to LbcTextSpec.Raw("TT"),
            "Test test t" to LbcTextSpec.Raw("TT"),
            "Test\u0009test" to LbcTextSpec.Raw("TT"), // tab
            " " to LbcTextSpec.StringResource(OSString.common_noNamePlaceholder),
            null to LbcTextSpec.StringResource(OSString.common_noNamePlaceholder),
            "" to LbcTextSpec.StringResource(OSString.common_noNamePlaceholder),
            "       " to LbcTextSpec.StringResource(OSString.common_noNamePlaceholder),
        )
        tests.forEach { (name, expectedInitial) ->
            val placeholder: LbcTextSpec = DefaultNameProvider(name).placeholderName
            assertEquals(expected = expectedInitial, actual = placeholder, name)
        }
    }
}
