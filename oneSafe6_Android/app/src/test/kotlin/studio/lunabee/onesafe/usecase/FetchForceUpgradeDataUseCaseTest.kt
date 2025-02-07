package studio.lunabee.onesafe.usecase

import androidx.datastore.core.DataStore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeInfo
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeStrings
import studio.lunabee.onesafe.domain.model.forceupgrade.ForceUpgradeTypeStrings
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.ForceUpgradeRepository
import studio.lunabee.onesafe.domain.usecase.forceupgrade.FetchForceUpgradeDataUseCase
import studio.lunabee.onesafe.domain.usecase.forceupgrade.IsForceUpgradeDisplayedUseCase
import studio.lunabee.onesafe.repository.datasource.ForceUpdateRemoteDatasource
import studio.lunabee.onesafe.repository.repository.ForceUpgradeRepositoryImpl
import studio.lunabee.onesafe.storage.OSForceUpgradeProto
import studio.lunabee.onesafe.storage.datasource.ForceUpgradeLocalDatasourceImpl
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@HiltAndroidTest
class FetchForceUpgradeDataUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var dataStore: DataStore<OSForceUpgradeProto.ForceUpgradeProtoData>

    @FileDispatcher
    @Inject
    lateinit var fileDispatcher: CoroutineDispatcher

    private val forceUpgradeRemoteDatasource: ForceUpdateRemoteDatasource = mockk {
        coEvery { fetchForceUpgradeInfo() } returns MockData.mockFetchedInfo
        coEvery { fetchForceUpgradeStrings(any()) } returns MockData.mockFetchedStrings
    }

    private fun getForceUpgradeRepository(buildNumber: Int): ForceUpgradeRepository {
        val forceUpgradeLocalDatasource = ForceUpgradeLocalDatasourceImpl(
            buildNumber = buildNumber,
            dataStore = dataStore,
            fileDispatcher = fileDispatcher,
        )

        return ForceUpgradeRepositoryImpl(
            forceUpgradeLocalDatasource = forceUpgradeLocalDatasource,
            forceUpgradeRemoteDatasource = forceUpgradeRemoteDatasource,
        )
    }

    /**
     * Assert that we don't download force upgrade strings and store data if not needed
     */
    @Test
    fun no_need_force_upgrade_situation_test() {
        val buildNumber = 8
        val forceUpgradeRepository = getForceUpgradeRepository(buildNumber)
        val isForceUpgradeDisplayedUseCase = IsForceUpgradeDisplayedUseCase(
            forceUpgradeRepository = forceUpgradeRepository,
        )
        val fetchForceUpgradeDataUseCase = FetchForceUpgradeDataUseCase(
            buildNumber = buildNumber,
            forceUpgradeRepository = forceUpgradeRepository,
        )
        runTest {
            fetchForceUpgradeDataUseCase.invoke()
            assertNull(forceUpgradeRepository.getForceUpgradeData().first())
            assertFalse(isForceUpgradeDisplayedUseCase.invoke(buildNumber = buildNumber))
        }
    }

    @Test
    fun force_upgrade_situation_test() {
        val buildNumber = 5
        val forceUpgradeRepository = getForceUpgradeRepository(buildNumber)
        val isForceUpgradeDisplayedUseCase = IsForceUpgradeDisplayedUseCase(
            forceUpgradeRepository = forceUpgradeRepository,
        )
        val fetchForceUpgradeDataUseCase = FetchForceUpgradeDataUseCase(
            buildNumber = buildNumber,
            forceUpgradeRepository = forceUpgradeRepository,
        )
        runTest {
            fetchForceUpgradeDataUseCase.invoke()
            assertNotNull(forceUpgradeRepository.getForceUpgradeData().first())
            assertTrue(isForceUpgradeDisplayedUseCase.invoke(buildNumber = buildNumber))
        }
    }

    private object MockData {
        val mockFetchedInfo = ForceUpgradeInfo(
            forceUpdateBuildNumber = 6,
            softUpdateBuildNumber = 8,
            languageFiles = mapOf(
                "en" to "en",
                "fr" to "fr",
            ),
            fallbackLanguageFile = "en",
        )

        val mockFetchedStrings = ForceUpgradeStrings(
            forceUpgrade = ForceUpgradeTypeStrings(
                title = "title",
                description = "force_description",
                buttonLabel = "update",
            ),
            softUpgrade = ForceUpgradeTypeStrings(
                title = "title",
                description = "soft_description",
                buttonLabel = "update",
            ),
        )
    }
}
