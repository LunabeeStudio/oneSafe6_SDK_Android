package studio.lunabee.onesafe.feature.breadcrumb

import com.lunabee.lbcore.model.LBResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.domain.usecase.forceupgrade.IsForceUpgradeDisplayedUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetItemWithAncestorsUseCase
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.repository.repository.SettingsRepository
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.OSUiThreadTest
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import java.time.Instant
import kotlin.test.assertContentEquals

class BreadcrumbViewModelTest : OSUiThreadTest() {
    private val getItemWithAncestorsUseCase: GetItemWithAncestorsUseCase = mockk()
    private val isSignUpUseCase: IsSignUpUseCase = mockk()
    private lateinit var viewModel: BreadcrumbViewModel

    @Before
    fun setUp() {
        val osAppSettings: SettingsRepository = mockk()
        every { osAppSettings.cameraSystemFlow(firstSafeId) } returns flowOf(CameraSystem.InApp)
        coEvery { isSignUpUseCase() } returns true
        val isForceUpgradeDisplayedUseCase: IsForceUpgradeDisplayedUseCase = mockk()
        coEvery { isForceUpgradeDisplayedUseCase(any()) } returns false
        viewModel = BreadcrumbViewModel(
            searchLogicDelegateImpl = mockk(),
            getItemWithAncestorsUseCase = getItemWithAncestorsUseCase,
            decryptUseCase = mockk(),
            getSafeItemActionHelper = mockk(),
            itemEditionFileManager = mockk {
                every { prepareDataForFieldImageCapture() } returns flowOf(
                    when (OSTestConfig.cameraSystem) {
                        CameraSystem.InApp -> CameraData.InApp(mockk())
                        CameraSystem.External -> CameraData.External(mockk())
                    },
                )
            },
        )
    }

    @Test
    fun updateBreadcrumb_init_test(): TestResult = runTest {
        val expectedItems = listOf(RouteBreadcrumbUiData.home())
        val actualItems = viewModel.uiState.filterIsInstance<BreadcrumbUiState.Idle>().first().breadcrumbItems
        assertContentEquals(expectedItems, actualItems)
    }

    @Test
    fun updateBreadcrumb_home_test(): TestResult = runTest {
        val itemId = testUUIDs[0]
        val mockItem = OSTestUtils.createSafeItem(id = itemId, encName = null, encColor = null)
        coEvery { getItemWithAncestorsUseCase(itemId) } returns LBResult.Success(listOf(mockItem))
        viewModel.updateBreadcrumb(ItemBreadcrumbDestination(itemId))
        viewModel.updateBreadcrumb(HardBreadcrumbDestination.Home)
        val expectedItems = listOf(RouteBreadcrumbUiData.home())
        val actualItems = viewModel.uiState.filterIsInstance<BreadcrumbUiState.Idle>().first().breadcrumbItems
        assertContentEquals(expectedItems, actualItems)
    }

    @Test
    fun updateBreadcrumb_bin_test(): TestResult = runTest {
        val itemId = testUUIDs[0]
        val mockItem = OSTestUtils.createSafeItem(id = itemId, encName = null, encColor = null)
        coEvery { getItemWithAncestorsUseCase(itemId) } returns LBResult.Success(listOf(mockItem))
        viewModel.updateBreadcrumb(ItemBreadcrumbDestination(itemId))
        viewModel.updateBreadcrumb(HardBreadcrumbDestination.Bin)
        val expectedItems = listOf(RouteBreadcrumbUiData.home(), RouteBreadcrumbUiData.bin())
        val actualItems = viewModel.uiState.filterIsInstance<BreadcrumbUiState.Idle>().first().breadcrumbItems
        assertContentEquals(expectedItems, actualItems)
    }

    @Test
    fun updateBreadcrumb_id_test(): TestResult = runTest {
        val itemId = testUUIDs[2]
        val mockAncestors = listOf(
            OSTestUtils.createSafeItem(id = testUUIDs[0], encName = null, encColor = null),
            OSTestUtils.createSafeItem(id = testUUIDs[1], encName = null, encColor = null),
            OSTestUtils.createSafeItem(id = itemId, encName = null, encColor = null),
        )
        coEvery { getItemWithAncestorsUseCase(itemId) } returns LBResult.Success(mockAncestors)

        viewModel.updateBreadcrumb(ItemBreadcrumbDestination(itemId))

        val expectedItems = mutableListOf<BreadcrumbUiDataSpec>(RouteBreadcrumbUiData.home())
        mockAncestors.mapTo(expectedItems) {
            ItemBreadcrumbUiData(it.id, DefaultNameProvider(null), BreadcrumbMainAction.AddItem)
        }
        val actualItems = viewModel.uiState.filterIsInstance<BreadcrumbUiState.Idle>().first().breadcrumbItems

        assertContentEquals(expectedItems, actualItems)
    }

    @Test
    fun updateBreadcrumb_deleted_test(): TestResult = runTest {
        val itemId = testUUIDs[0]
        val mockItem = OSTestUtils.createSafeItem(
            id = itemId,
            encName = null,
            deletedAt = Instant.ofEpochMilli(0),
            encColor = null,
        )
        coEvery { getItemWithAncestorsUseCase(itemId) } returns LBResult.Success(listOf(mockItem))

        viewModel.updateBreadcrumb(ItemBreadcrumbDestination(itemId))

        val expectedItems = mutableListOf(
            RouteBreadcrumbUiData.home(),
            RouteBreadcrumbUiData.bin(),
            ItemBreadcrumbUiData(mockItem.id, DefaultNameProvider(null), BreadcrumbMainAction.None),
        )
        val actualItems = viewModel.uiState.filterIsInstance<BreadcrumbUiState.Idle>().first().breadcrumbItems

        assertContentEquals(expectedItems, actualItems)
    }
}
