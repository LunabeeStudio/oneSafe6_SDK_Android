package studio.lunabee.onesafe.usecase

import android.content.Context
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.repository.UrlMetadataRepository
import studio.lunabee.onesafe.domain.usecase.GetUrlMetadataUseCase
import studio.lunabee.onesafe.error.OSRemoteError
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@HiltAndroidTest
class GetUrlMetadataUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    private val validUrlStartList: List<String> = listOf(
        "",
        "https://",
        "http://",
        "www.",
        "https://www.",
        "http://www.",
    )

    private val urlMetadataRepository: UrlMetadataRepository = mockk()
    private val getUrlMetadataUseCase: GetUrlMetadataUseCase = GetUrlMetadataUseCase(
        urlMetadataRepository = urlMetadataRepository,
        validUrlStartList = validUrlStartList,
    )

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Test
    fun fetch_url_metadata_from_existing_url() {
        val expectedTitle = "Lunabee Studio"
        val url = "https://lunabee.studio"
        val file = File(context.cacheDir, "test.ico")
        coEvery { urlMetadataRepository.getHtmlPageCode(url = url) } returns HtmlWithMetaProperty
        coEvery { urlMetadataRepository.downloadFavIcon(baseUrl = url, targetFile = file) } returns LBResult.Success(file)
        runTest {
            val urlMetadata = getUrlMetadataUseCase(url = url, iconFile = file, force = false)
            assertSuccess(urlMetadata)
            assertEquals(expected = expectedTitle, actual = urlMetadata.successData.title)
            assertEquals(expected = file, actual = urlMetadata.successData.iconFile)
        }
        coVerify(exactly = 1) { urlMetadataRepository.getHtmlPageCode(url = url) }
        coVerify(exactly = 1) { urlMetadataRepository.downloadFavIcon(baseUrl = url, targetFile = file) }
    }

    @Test
    fun fetch_url_metadata_from_short_url() {
        val expectedTitle = "Lunabee Studio Home"
        val url = "lunabee.studio"
        val file = File(context.cacheDir, "test.ico")
        coEvery { urlMetadataRepository.getHtmlPageCode(url = url) } throws OSRemoteError(
            code = OSRemoteError.Code.UNKNOWN_HTTP_ERROR,
        )
        coEvery { urlMetadataRepository.getHtmlPageCode(url = "https://$url") } returns HtmlWithTitleProperty

        coEvery { urlMetadataRepository.downloadFavIcon(baseUrl = "https://$url", targetFile = file) } returns LBResult.Success(file)
        runTest {
            val urlMetadata = getUrlMetadataUseCase(url = url, iconFile = file, force = false)
            assertSuccess(urlMetadata)
            assertEquals(expected = expectedTitle, actual = urlMetadata.successData.title)
            assertEquals(expected = file, actual = urlMetadata.successData.iconFile)
        }
        coVerify(exactly = 1) { urlMetadataRepository.getHtmlPageCode(url = url) }
        coVerify(exactly = 1) { urlMetadataRepository.getHtmlPageCode(url = "https://$url") }
        coVerify(exactly = 0) { urlMetadataRepository.downloadFavIcon(baseUrl = url, targetFile = file) }
        coVerify(exactly = 1) { urlMetadataRepository.downloadFavIcon(baseUrl = "https://$url", targetFile = file) }
    }

    @Test
    fun fetch_url_metadata_from_bad_url() {
        val url = "randomstring"
        val file = File(context.cacheDir.path, "test.ico")
        coEvery { urlMetadataRepository.getHtmlPageCode(url = url) } throws OSRemoteError(
            code = OSRemoteError.Code.UNKNOWN_HTTP_ERROR,
        )
        validUrlStartList.forEach {
            coEvery { urlMetadataRepository.getHtmlPageCode(url = it + url) } throws
                OSRemoteError(code = OSRemoteError.Code.UNKNOWN_HTTP_ERROR)
        }

        runTest {
            val urlMetadata = getUrlMetadataUseCase(url = url, iconFile = file, false)
            assertFailure(urlMetadata)
        }

        coVerify(exactly = 1) { urlMetadataRepository.getHtmlPageCode(url = url) }
        validUrlStartList.forEach {
            coVerify(exactly = 1) { urlMetadataRepository.getHtmlPageCode(url = it + url) }
        }
        coVerify(exactly = 0) { urlMetadataRepository.downloadFavIcon(baseUrl = url, targetFile = file) }
    }

    @Test
    fun regex_meta_title_html() {
        val stringMetaTitleOkToTest = listOf(
            HtmlWithMetaProperty,
            "<meta property=\"og:site_name\" content=\"Lunabee Studio\" />",
            "<meta property=\"og:site_name\" content=\"Lunabee Studio\"/>",
            "<meta property=\"og:site_name\" content=\"Lunabee Studio\">",
        )

        stringMetaTitleOkToTest.forEach {
            assertNotNull(actual = GetUrlMetadataUseCase.MetaTitlePropertyPattern.toRegex().find(input = it))
        }

        val stringMetaTitleKoToTest = listOf(
            "<meta />",
            "random",
            "<meta property=\"og:site_name\" />",
        )

        stringMetaTitleKoToTest.forEach {
            assertNull(actual = GetUrlMetadataUseCase.MetaTitlePropertyPattern.toRegex().find(input = it))
        }
    }

    @Test
    fun regex_title_html() {
        val stringTitleOkToTest = listOf(
            HtmlWithTitleProperty,
            "<title>Lunabee Studio Home</title>",
            "<title>Lunabee Studio Home<title>",
            "<title></title>",
            "<title><title>",
        )

        stringTitleOkToTest.forEach {
            assertNotNull(actual = GetUrlMetadataUseCase.TitleTagPattern.toRegex().find(input = it))
        }

        val stringTitleKoToTest = listOf(
            "<meta />",
            "random",
            "<title>",
        )

        stringTitleKoToTest.forEach {
            assertNull(actual = GetUrlMetadataUseCase.MetaTitlePropertyPattern.toRegex().find(input = it))
        }
    }

    companion object {
        private const val HtmlWithMetaProperty: String = "<meta property=\"og:site_name\" content=\"Lunabee Studio\" />"
        private const val HtmlWithTitleProperty: String = "<title>Lunabee Studio Home</title>"
    }
}
