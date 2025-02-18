package studio.lunabee.onesafe.feature.search

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbextensions.lazyFast
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.di.InMemoryMainDatabaseModule
import studio.lunabee.di.InMemoryMainDatabaseNamesModule
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.manager.SearchIndexManager
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.qualifier.DatabaseName
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.domain.usecase.search.GetMatchFromSearchUseCase
import studio.lunabee.onesafe.domain.usecase.search.SearchItemUseCase
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.SqlCipherDBManager
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.test
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.test.assertContentEquals

@HiltAndroidTest
@UninstallModules(InMemoryMainDatabaseModule::class, InMemoryMainDatabaseNamesModule::class)
class SearchItemUseCaseTest : OSHiltTest() {

    override val testDispatcher: TestDispatcher = StandardTestDispatcher()

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @BindValue
    @DatabaseName(DatabaseName.Type.Main)
    val mainDbName: String = ""

    @BindValue
    @DatabaseName(DatabaseName.Type.CipherTemp)
    val tempDbName: String = ""

    @BindValue
    val sqlCipherManager: DatabaseEncryptionManager = SqlCipherDBManager(testDispatcher, context, "", tempDbName)

    // Custom database builder to get the hand on Room coroutine's
    // https://medium.com/@eyalg/testing-androidx-room-kotlin-coroutines-2d1faa3e674f
    // And specific executor for transaction to avoid dead lock
    // https://stackoverflow.com/a/57900487/10935947
    @BindValue
    val managedDispatcherDatabase: MainDatabase = Room.inMemoryDatabaseBuilder(
        context,
        MainDatabase::class.java,
    )
        .setQueryExecutor(testDispatcher.asExecutor())
        .setTransactionExecutor(Executors.newSingleThreadExecutor())
        .build()

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    // FIXME mock SearchIndexManager instead of GetMatchFromSearchUseCase because of issue with mocked hashset
    //  https://github.com/mockk/mockk/issues/340#issuecomment-709122959
    @MockK lateinit var indexManager: SearchIndexManager

    private val getMatchFromSearchUseCase: GetMatchFromSearchUseCase by lazyFast {
        GetMatchFromSearchUseCase(safeItemRepository, itemSettingsRepository, safeRepository)
    }

    private val searchItemUseCase: SearchItemUseCase by lazyFast {
        SearchItemUseCase(getMatchFromSearchUseCase, indexManager)
    }

    @Inject
    lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    private val firstItemName = "Love Beer"
    private val secondItemName = "Love Wine"

    private lateinit var firstItem: SafeItem
    private lateinit var secondItem: SafeItem
    private lateinit var identifierField: SafeItemField

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        runTest(testDispatcher) {
            firstItem = createItemUseCase.test(firstItemName)
            secondItem = createItemUseCase.test(secondItemName)

            identifierField = addFieldUseCase(
                firstItem.id,
                createItemFieldData(
                    name = "field",
                    position = 0.0,
                    value = "identifier",
                    isItemIdentifier = true,
                    kind = SafeItemFieldKind.Text,
                ),
            ).data!!
        }

        val indexSuccessState = LBFlowResult.Success(
            listOf(
                PlainIndexWordEntry("love", firstItem.id, null),
                PlainIndexWordEntry("beer", firstItem.id, null),
                PlainIndexWordEntry("love", secondItem.id, null),
                PlainIndexWordEntry("wine", secondItem.id, null),
            ),
        )

        every { indexManager.decryptedIndex } returns MutableStateFlow(
            indexSuccessState,
        )

        every { indexManager.initStoreIndex(any()) } returns Unit
    }

    @Test
    fun search_match_item_test(): TestResult = runTest(testDispatcher) {
        val expected = linkedSetOf(
            SafeItemWithIdentifier(
                id = firstItem.id,
                encName = firstItem.encName,
                iconId = firstItem.iconId,
                encColor = firstItem.encColor,
                encIdentifier = identifierField.encValue,
                encSecuredDisplayMask = null,
                encIdentifierKind = identifierField.encKind,
                position = firstItem.position,
                updatedAt = firstItem.updatedAt,
            ),
            SafeItemWithIdentifier(
                id = secondItem.id,
                encName = secondItem.encName,
                iconId = secondItem.iconId,
                encColor = secondItem.encColor,
                encIdentifier = null,
                encSecuredDisplayMask = null,
                encIdentifierKind = null,
                position = secondItem.position,
                updatedAt = secondItem.updatedAt,
            ),
        )
        searchItemUseCase("love", true, this)
        val actual = searchItemUseCase.searchResultFlow.first() ?: linkedSetOf()
        assertContentEquals(expected.sortedBy { it.id }, actual.asIterable().sortedBy { it.id })
    }

    @Test
    fun search_match_item_deleted_test(): TestResult = runTest(testDispatcher) {
        moveToBinItemUseCase(secondItem)
        searchItemUseCase("love", true, this)
        val actual = searchItemUseCase.searchResultFlow.first() ?: linkedSetOf()
        assert(actual.any { it.deletedAt != null })
    }

    @Test
    fun no_search_if_min_char_test(): TestResult = runTest(testDispatcher) {
        searchItemUseCase("L", true, this)
        val actual = searchItemUseCase.searchResultFlow.first()
        val expected = null
        assertContentEquals(expected, actual?.asIterable())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun search_match_item_not_final_test(): TestResult = runTest(testDispatcher) {
        val expectedBefore = linkedSetOf<SafeItemWithIdentifier>()
        val expectedAfter = linkedSetOf(
            SafeItemWithIdentifier(
                id = firstItem.id,
                encName = firstItem.encName,
                iconId = firstItem.iconId,
                encColor = firstItem.encColor,
                encIdentifier = identifierField.encValue,
                encSecuredDisplayMask = null,
                encIdentifierKind = identifierField.encKind,
                position = firstItem.position,
                updatedAt = firstItem.updatedAt,
            ),
            SafeItemWithIdentifier(
                id = secondItem.id,
                encName = secondItem.encName,
                iconId = secondItem.iconId,
                encColor = secondItem.encColor,
                encIdentifier = null,
                encSecuredDisplayMask = null,
                encIdentifierKind = null,
                position = secondItem.position,
                updatedAt = secondItem.updatedAt,
            ),
        )

        var actual = emptyList<SafeItemWithIdentifier>()

        launch {
            actual = searchItemUseCase.searchResultFlow.first() ?: emptyList()
        }

        searchItemUseCase("Love", false, this)
        advanceTimeBy(Constant.DelayBeforeSearch.inWholeMilliseconds)

        assertContentEquals(expectedBefore.sortedBy { it.id }, actual.asIterable().sortedBy { it.id })
        runCurrent()
        assertContentEquals(expectedAfter.sortedBy { it.id }, actual.asIterable().sortedBy { it.id })
    }
}
