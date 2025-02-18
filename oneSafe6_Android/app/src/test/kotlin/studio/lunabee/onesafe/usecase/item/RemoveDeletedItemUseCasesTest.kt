package studio.lunabee.onesafe.usecase.item

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RemoveDeletedItemUseCase
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertSuccess
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

@HiltAndroidTest
class RemoveDeletedItemUseCasesTest : OSHiltUnitTest() {

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    @Inject lateinit var removeDeletedItemUseCase: RemoveDeletedItemUseCase

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val iconDir = File(context.filesDir, "icons")

    @Test
    fun remove_item_with_icon(): TestResult = runTest {
        val icon = LbcResourcesHelper.readResourceAsBytes("icon_259_194.jpeg")
        val item = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = icon,
            color = null,
        ).data!!
        moveToBinItemUseCase(item)
        val actualRemoveResult = removeDeletedItemUseCase(item)
        assertSuccess(actualRemoveResult)
        assertTrue(iconDir.listFiles()!!.isEmpty())
        assertFailsWith<OSStorageError> {
            safeItemRepository.getSafeItem(item.id)
        }

        assertFailsWith<OSStorageError> {
            safeItemKeyRepository.getSafeItemKey(item.id)
        }
    }

    @Test
    fun remove_item_include_child(): TestResult = runTest {
        val icon = LbcResourcesHelper.readResourceAsBytes("icon_259_194.jpeg")
        val icon2 = LbcResourcesHelper.readResourceAsBytes("icon_700_523.png")
        val item = createItemUseCase.test(
            icon = icon,
        )

        val itemChild = createItemUseCase.test(
            parentId = item.id,
            icon = icon2,
        )

        moveToBinItemUseCase(item)

        val actualRemoveResult = removeDeletedItemUseCase(item)
        assertSuccess(actualRemoveResult)

        val actualFiles = iconDir.listFiles()
        assertEquals(0, actualFiles!!.size, actualFiles.joinToString { it.name })
        assertFailsWith<OSStorageError> {
            safeItemRepository.getSafeItem(item.id)
        }
        assertFailsWith<OSStorageError> {
            safeItemRepository.getSafeItem(itemChild.id)
        }

        assertFailsWith<OSStorageError> {
            safeItemKeyRepository.getSafeItemKey(item.id)
        }
        assertFailsWith<OSStorageError> {
            safeItemKeyRepository.getSafeItemKey(itemChild.id)
        }
    }

    @Test
    fun removeItem_reparent_with_ancestor_test(): TestResult = runTest {
        // Setup
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
        val itemC = createItemUseCase(
            name = null,
            parentId = itemB.id,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        testClock.add(10.milliseconds.toJavaDuration()) // emulate some delay before bin

        moveToBinItemUseCase(itemC)
        moveToBinItemUseCase(itemB)

        // Test
        removeDeletedItemUseCase(safeItemRepository.getSafeItem(itemB.id))

        val actualC = safeItemRepository.getSafeItem(itemC.id)
        val expectedC = itemC.copy(
            deletedAt = actualC.deletedAt,
            parentId = itemA.id,
            deletedParentId = null,
        )

        assertEquals(expectedC, actualC)
        assert(actualC.deletedAt!! >= actualC.createdAt)
    }
}
