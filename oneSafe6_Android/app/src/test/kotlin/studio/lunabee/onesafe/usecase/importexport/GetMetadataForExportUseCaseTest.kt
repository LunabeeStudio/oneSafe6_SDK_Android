package studio.lunabee.onesafe.usecase.importexport

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.importexport.usecase.GetMetadataForExportUseCase
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertSuccess
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class GetMetadataForExportUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    @Inject
    lateinit var getMetadataForExportUseCase: GetMetadataForExportUseCase

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun get_export_metadata_test() {
        runTest {
            repeat(times = 10) {
                createItemUseCase.test(name = "$it", position = it.toDouble())
            }

            val result = getMetadataForExportUseCase()
            assertSuccess(result)
            assertEquals(result.successData.itemCount, 10)
        }
    }
}
