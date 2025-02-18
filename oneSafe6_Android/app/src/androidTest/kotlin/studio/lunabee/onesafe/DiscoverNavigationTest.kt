package studio.lunabee.onesafe

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class DiscoverNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {}

    @Inject lateinit var safeItemRepository: SafeItemRepository

    private val hasDiscoverButton = hasText(getString(id = OSString.home_tutorialDialog_discover_button))
        .and(hasAnyAncestor(hasTestTag(UiConstants.TestTag.Item.DiscoveryItemCard)))

    @Test
    fun discover_prefill_test() {
        invoke {
            hasDiscoverButton
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .performClick()
            hasText(getString(id = OSString.home_discoverBottomSheet_title))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(id = OSString.home_discoverBottomSheet_items_title))
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .performClick()
            hasText(getString(id = OSString.home_discoverBottomSheet_create))
                .waitUntilExactlyOneExists()
                .performClick()
            hasDiscoverButton
                .waitUntilDoesNotExist()
            assertItemCount(NumberOfPrefillItem)
        }
    }

    @Test
    fun discover_items_test() {
        invoke {
            hasDiscoverButton
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .performClick()
            hasText(getString(id = OSString.home_discoverBottomSheet_title))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(id = OSString.home_discoverBottomSheet_prefill_title))
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .performClick()
            hasText(getString(id = OSString.home_discoverBottomSheet_create))
                .waitUntilExactlyOneExists()
                .performClick()
            hasDiscoverButton
                .waitUntilDoesNotExist()
            assertItemCount(NumberOfDiscoverItem)
        }
    }

    @OptIn(FlowPreview::class)
    private fun assertItemCount(expected: Int) {
        runBlocking {
            safeItemRepository.getSafeItemsCountFlow(firstSafeId).filter { it == expected }.timeout(5.seconds).first()
        }
    }

    companion object {
        private const val NumberOfPrefillItem: Int = 8
        private const val NumberOfDiscoverItem: Int = 3
    }
}
