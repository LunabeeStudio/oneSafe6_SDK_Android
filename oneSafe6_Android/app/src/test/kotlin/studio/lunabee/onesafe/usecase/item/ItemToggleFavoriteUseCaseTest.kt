package studio.lunabee.onesafe.usecase.item

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemToggleFavoriteUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@HiltAndroidTest
class ItemToggleFavoriteUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var itemToggleFavoriteUseCase: ItemToggleFavoriteUseCase

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Test
    fun toggle_favorite_test(): TestResult = runTest {
        val item = createItemUseCase(null, null, false, null, null).data!!
        assertFalse(item.isFavorite)
        itemToggleFavoriteUseCase(item.id)
        var retrievedItem = safeItemRepository.getSafeItem(item.id)
        assertTrue(retrievedItem.isFavorite)
        itemToggleFavoriteUseCase(item.id)
        retrievedItem = safeItemRepository.getSafeItem(item.id)
        assertFalse(retrievedItem.isFavorite)
    }
}
