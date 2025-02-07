package studio.lunabee.onesafe.usecase.item

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.test.InitialTestState
import java.time.Instant
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

@HiltAndroidTest
class MoveToBinItemUseCaseTest : OSHiltUnitTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    @Test
    fun moveToBinItem_empty_test(): TestResult = runTest {
        val item = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!
        moveToBinItemUseCase(item)
        val actualItem = safeItemRepository.getSafeItem(item.id)
        assertTrue(actualItem.isDeleted)
    }

    @Test
    fun moveToBinItem_with_children_test(): TestResult = runTest {
        val item = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!
        val child = createItemUseCase(
            name = null,
            parentId = item.id,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!
        moveToBinItemUseCase(item)
        val actualItem = safeItemRepository.getSafeItem(item.id)
        val actualChild = safeItemRepository.getSafeItem(child.id)

        assertTrue(actualItem.isDeleted)
        assertTrue(actualChild.isDeleted)
        assertEquals(actualItem.id, actualChild.parentId)
        assertEquals(actualItem.id, actualChild.deletedParentId)
    }

    @Test
    fun moveToBinItem_all_test(): TestResult = runTest {
        val sibling = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!
        val parent = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!
        val child = createItemUseCase(
            name = null,
            parentId = parent.id,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!
        moveToBinItemUseCase.all()
        val actualParent = safeItemRepository.getSafeItem(parent.id)
        val actualChild = safeItemRepository.getSafeItem(child.id)
        val actualSibling = safeItemRepository.getSafeItem(sibling.id)

        assertTrue(actualParent.isDeleted)
        assertTrue(actualChild.isDeleted)
        assertTrue(actualSibling.isDeleted)
        assertEquals(actualParent.id, actualChild.parentId)
        assertEquals(actualParent.id, actualChild.deletedParentId)
    }

    @Test
    fun moveToBinItem_with_children_favorite_test(): TestResult = runTest {
        val item = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = true,
            icon = null,
            color = null,
        ).data!!
        val child = createItemUseCase(
            name = null,
            parentId = item.id,
            isFavorite = true,
            icon = null,
            color = null,
        ).data!!
        moveToBinItemUseCase(item)
        val actualItem = safeItemRepository.getSafeItem(item.id)
        val actualChild = safeItemRepository.getSafeItem(child.id)

        assertTrue(actualItem.isDeleted)
        assertTrue(actualChild.isDeleted)
        assertFalse(actualItem.isFavorite)
        assertFalse(actualChild.isFavorite)
        assertEquals(actualItem.id, actualChild.parentId)
        assertEquals(actualItem.id, actualChild.deletedParentId)
    }

    @Test
    fun moveToBinItem_child_before_parent_test(): TestResult = runTest {
        val itemA = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!
        val itemB = createItemUseCase(
            name = null,
            parentId = itemA.id,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        testClock.add(10.milliseconds.toJavaDuration()) // emulate some delay before bin

        moveToBinItemUseCase(itemB)
        moveToBinItemUseCase(itemA)

        val actualA = safeItemRepository.getSafeItem(itemA.id)
        val actualB = safeItemRepository.getSafeItem(itemB.id)

        val expectedA = itemA.copy(
            deletedAt = Instant.now(testClock),
            parentId = null,
            deletedParentId = null,
        )

        val expectedB = itemB.copy(
            deletedAt = Instant.now(testClock),
            parentId = itemA.id,
            deletedParentId = null,
        )

        assertEquals(expectedA, actualA)
        assertEquals(expectedB, actualB)
        assert(actualA.deletedAt!! >= actualA.createdAt)
        assert(actualB.deletedAt!! >= actualB.createdAt)
    }
}
