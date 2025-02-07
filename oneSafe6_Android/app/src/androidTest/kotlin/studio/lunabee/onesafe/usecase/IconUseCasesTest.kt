package studio.lunabee.onesafe.usecase

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.di.FrameworkTestModule
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.DeleteIconUseCase
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.SetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@HiltAndroidTest
class IconUseCasesTest : OSHiltTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var getIconUseCase: GetIconUseCase

    @Inject lateinit var deleteIconUseCase: DeleteIconUseCase

    @Inject lateinit var setIconUseCase: SetIconUseCase

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var safeItemRepository: SafeItemRepository

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val tmpIconDir = File(context.cacheDir, FrameworkTestModule.ICON_DIR)
    private val iconDir = File(context.filesDir, "icons") // TODO replace module to fix dir name

    init {
        tmpIconDir.deleteRecursively()
        iconDir.deleteRecursively()
    }

    @Test
    fun create_item_with_icon(): TestResult = runTest {
        val icon = LbcResourcesHelper.readResourceAsBytes("icon_259_194.jpeg")

        val item = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = icon,
            color = null,
        ).data!!

        val retrievedItem = safeItemRepository.getSafeItem(item.id)
        val actualIconId = retrievedItem.iconId

        assertNotNull(actualIconId)

        val actualIconResult = getIconUseCase(actualIconId, item.id)

        assertSuccess(actualIconResult)

        val actualIconBitmap = BitmapFactory.decodeByteArray(
            actualIconResult.successData,
            0,
            actualIconResult.successData.size,
        )

        assertEquals(FrameworkTestModule.RESIZE_ICON_SIZE, actualIconBitmap.height)
        assertEquals(FrameworkTestModule.RESIZE_ICON_SIZE, actualIconBitmap.width)
        assertTrue(tmpIconDir.listFiles()!!.isEmpty())
        assertEquals(1, iconDir.listFiles()!!.size)
    }

    @Test
    fun delete_icon_from_item(): TestResult = runTest {
        val icon = LbcResourcesHelper.readResourceAsBytes("icon_259_194.jpeg")

        val item = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = icon,
            color = null,
        ).data!!

        val retrievedItem = safeItemRepository.getSafeItem(item.id)
        val actualDeleteResult = deleteIconUseCase(retrievedItem)

        assertSuccess(actualDeleteResult)
        assertTrue(iconDir.listFiles()!!.isEmpty())
        assertTrue(fileRepository.getFiles(firstSafeId).isEmpty())

        val retrievedItemAfterDelete = safeItemRepository.getSafeItem(item.id)

        assertNull(retrievedItemAfterDelete.iconId)
    }

    @Test
    fun update_item_with_icon(): TestResult = runTest {
        val icon = LbcResourcesHelper.readResourceAsBytes("icon_259_194.jpeg")
        val icon2 = LbcResourcesHelper.readResourceAsBytes("icon_700_523.png")

        val item = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = icon,
            color = null,
        ).data!!

        val retrievedItem = safeItemRepository.getSafeItem(item.id)
        val initialIconResult = getIconUseCase(retrievedItem.iconId!!, item.id)

        setIconUseCase(retrievedItem, icon2)

        val retrievedItemAfterUpdate = safeItemRepository.getSafeItem(item.id)

        val actualIconId = retrievedItemAfterUpdate.iconId
        assertNotNull(actualIconId)
        assertNotEquals(retrievedItem.iconId, actualIconId)

        val actualIconResult = getIconUseCase(actualIconId, item.id)

        assertSuccess(actualIconResult)
        assertTrue(tmpIconDir.listFiles()!!.isEmpty())
        assertEquals(1, iconDir.listFiles()!!.size)

        val initialIconBitmap = BitmapFactory.decodeByteArray(
            initialIconResult.data!!,
            0,
            initialIconResult.data!!.size,
        )
        val actualIconBitmap = BitmapFactory.decodeByteArray(
            actualIconResult.successData,
            0,
            actualIconResult.successData.size,
        )

        assertFalse("Bitmap icon has not changed") { initialIconBitmap.sameAs(actualIconBitmap) }

        setIconUseCase(retrievedItemAfterUpdate, icon)

        val retrievedItemAfterReInit = safeItemRepository.getSafeItem(item.id)
        val actualIconResultAfterReInit = getIconUseCase(retrievedItemAfterReInit.iconId!!, item.id)
        val actualIconBitmapAfterReInit = BitmapFactory.decodeByteArray(
            actualIconResultAfterReInit.data!!,
            0,
            actualIconResultAfterReInit.data!!.size,
        )

        assertTrue("Bitmap icon unstable") { initialIconBitmap.sameAs(actualIconBitmapAfterReInit) }
    }
}
