package studio.lunabee.onesafe.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.onesafe.feature.settings.about.AboutScreen
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalTestApi::class)
class AboutScreenTest : LbcComposeTest() {

    private val navigateBack: () -> Unit = spyk({})
    private val onClickOnYoutube: () -> Unit = spyk({})
    private val onClickOnFacebook: () -> Unit = spyk({})
    private val onClickOnTwitter: () -> Unit = spyk({})
    private val onClickOnTikTok: () -> Unit = spyk({})
    private val onClickOnCredits: () -> Unit = spyk({})
    private val onClickOnLibraries: () -> Unit = spyk({})
    private val onClickOnTerms: () -> Unit = spyk({})

    /**
     * Test that all the action of the screen can and are triggered
     */
    @Test
    fun actions_triggering_test() {
        setScreen {
            /* YOUTUBE */
            onNodeWithTag(UiConstants.TestTag.Item.AboutScreenList)
                .performScrollToNode(hasText(getString(OSString.aboutScreen_infoCard_youtubeLabel)))
            onNodeWithText(getString(OSString.aboutScreen_infoCard_youtubeLabel)).performClick()
            verify(exactly = 1) { onClickOnYoutube.invoke() }

            /* FACEBOOK */
            onNodeWithTag(UiConstants.TestTag.Item.AboutScreenList)
                .performScrollToNode(hasText(getString(OSString.aboutScreen_infoCard_facebookLabel)))
            onNodeWithText(getString(OSString.aboutScreen_infoCard_facebookLabel)).performClick()
            verify(exactly = 1) { onClickOnFacebook.invoke() }

            /* TWITTER */
            onNodeWithTag(UiConstants.TestTag.Item.AboutScreenList)
                .performScrollToNode(hasText(getString(OSString.aboutScreen_infoCard_twitterLabel)))
            onNodeWithText(getString(OSString.aboutScreen_infoCard_twitterLabel)).performClick()
            verify(exactly = 1) { onClickOnTwitter.invoke() }

            /* TIKTOK */
            onNodeWithTag(UiConstants.TestTag.Item.AboutScreenList)
                .performScrollToNode(hasText(getString(OSString.aboutScreen_infoCard_tiktokLabel)))
            onNodeWithText(getString(OSString.aboutScreen_infoCard_tiktokLabel)).performClick()
            verify(exactly = 1) { onClickOnTikTok.invoke() }

            /* TERMS */
            onNodeWithTag(UiConstants.TestTag.Item.AboutScreenList)
                .performScrollToNode(hasText(getString(OSString.aboutScreen_infoCard_termsLabel)))
            onNodeWithText(getString(OSString.aboutScreen_infoCard_termsLabel)).performClick()
            verify(exactly = 1) { onClickOnTerms.invoke() }

            /* CREDITS */
            onNodeWithTag(UiConstants.TestTag.Item.AboutScreenList)
                .performScrollToNode(hasText(getString(OSString.aboutScreen_infoCard_termsLabel)))
            onNodeWithTag(UiConstants.TestTag.Item.AboutScreenList)
                .performScrollToNode(hasText(getString(OSString.aboutScreen_creditCard_label)))
            onNodeWithText(getString(OSString.aboutScreen_creditCard_label)).performClick()
            verify(exactly = 1) { onClickOnCredits.invoke() }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    private fun setScreen(
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            setContent {
                AboutScreen(
                    navigateBack = navigateBack,
                    onClickOnCredits = onClickOnCredits,
                    onClickOnYoutube = onClickOnYoutube,
                    onClickOnFacebook = onClickOnFacebook,
                    onClickOnTwitter = onClickOnTwitter,
                    onClickOnTiktok = onClickOnTikTok,
                    onClickOnTerms = onClickOnTerms,
                    onClickOnLibraries = onClickOnLibraries,
                )
            }
            block()
        }
    }
}
