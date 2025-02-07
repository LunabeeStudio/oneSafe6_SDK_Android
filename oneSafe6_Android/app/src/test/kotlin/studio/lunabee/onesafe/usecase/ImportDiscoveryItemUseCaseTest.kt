package studio.lunabee.onesafe.usecase

import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.safeitem.DiscoveryData
import studio.lunabee.onesafe.domain.model.safeitem.DiscoveryField
import studio.lunabee.onesafe.domain.model.safeitem.DiscoveryItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.firstSafeId
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@HiltAndroidTest
class ImportDiscoveryItemUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var safeItemFieldRepository: SafeItemFieldRepository

    @Inject lateinit var importDiscoveryItemUseCase: ImportDiscoveryItemUseCase

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    @Test
    fun import_single_item_test() {
        runTest {
            val discoveryData = DiscoveryData(
                labels = labels,
                data = listOf(
                    DiscoveryItem(
                        title = LunabeeNameKey,
                        isFavorite = true,
                        color = null,
                        items = listOf(),
                        fields = listOf(
                            DiscoveryField(
                                isItemIdentifier = false,
                                isSecured = false,
                                kind = SafeItemFieldKind.Email.id,
                                position = 0,
                                showPrediction = true,
                                name = LunabeeEmailNameKey,
                                placeholder = LunabeePlaceHolderKey,
                                value = LunabeeEmailValueKey,
                                formattingMask = LunabeeEmailFormattingMask,
                                secureDisplayMask = LunabeeEmailSecureMask,
                            ),
                        ),
                    ),
                ),
            )

            val result = importDiscoveryItemUseCase(discoveryData, "fr") // Test fallback locale
            assert(result is LBResult.Success)

            val savedItem = safeItemRepository.getAllSafeItems(firstSafeId).first()
            val field = safeItemFieldRepository.getSafeItemFields(savedItem.id).first()
            val decryptedName = decryptUseCase(savedItem.encName!!, savedItem.id, String::class)
            assertEquals(
                expected = LunabeeName,
                actual = decryptedName.data.orEmpty(),
            )
            assert(savedItem.isFavorite)
            assertEquals(LunabeeEmailName, decryptUseCase(field.encName!!, savedItem.id, String::class).data)
            assertEquals(LunabeeEmailValue, decryptUseCase(field.encValue!!, savedItem.id, String::class).data)
            assertEquals(LunabeePlaceHolder, decryptUseCase(field.encPlaceholder!!, savedItem.id, String::class).data)
            assertEquals(LunabeeEmailFormattingMask, decryptUseCase(field.encFormattingMask!!, savedItem.id, String::class).data)
            assertEquals(LunabeeEmailSecureMask, decryptUseCase(field.encSecureDisplayMask!!, savedItem.id, String::class).data)
            assertFalse(field.isSecured)
            assert(field.showPrediction)
            assertEquals(SafeItemFieldKind.Email.id, decryptUseCase(field.encKind!!, savedItem.id, String::class).data)
        }
    }

    @Test
    fun import_item_with_children_test() {
        runTest {
            val discoveryData = DiscoveryData(
                labels = labels,
                data = listOf(
                    DiscoveryItem(
                        title = LunabeeNameKey,
                        isFavorite = true,
                        color = null,
                        items = listOf(
                            DiscoveryItem(
                                title = MacNameKey,
                                isFavorite = false,
                                items = listOf(
                                    DiscoveryItem(
                                        title = AndroidStudioNameKey,
                                        isFavorite = true,
                                    ),
                                ),
                            ),
                        ),
                        fields = listOf(),
                    ),
                ),
            )

            val result = importDiscoveryItemUseCase(discoveryData, "en")
            assert(result is LBResult.Success)

            val allItems = safeItemRepository.getAllSafeItems(firstSafeId)

            assertEquals(3, allItems.size)

            val lunabeeItem = allItems.first { it.parentId == null }
            val macItem = allItems.first { it.parentId == lunabeeItem.id }
            val androidItem = allItems.first { it.parentId == macItem.id }
            assertEquals(MacName, decryptUseCase(macItem.encName!!, macItem.id, String::class).data)
            assertFalse(macItem.isFavorite)
            assertEquals(AndroidStudioName, decryptUseCase(androidItem.encName!!, androidItem.id, String::class).data)
            assert(androidItem.isFavorite)
        }
    }
}

private const val LunabeeNameKey: String = "lunabee.name"
private const val LunabeeEmailNameKey: String = "lunabee.email.name"
private const val LunabeeEmailValueKey: String = "lunabee.email.value"
private const val LunabeePlaceHolderKey: String = "lunabee.email.placeholder"
private const val LunabeeName: String = "Lunabee"
private const val LunabeeEmailValue: String = "studio@lunabee.com"
private const val LunabeeEmailName: String = "email"
private const val LunabeePlaceHolder: String = "email placeholder"
private const val LunabeeEmailFormattingMask: String = "########"
private const val LunabeeEmailSecureMask: String = "..#.."
private const val MacNameKey: String = "mac.name"
private const val MacName: String = "Mac"
private const val AndroidStudioNameKey: String = "androidstudio.name"
private const val AndroidStudioName: String = "Android Studio"

private val labels = mapOf(
    "en" to mapOf(
        LunabeeNameKey to LunabeeName,
        LunabeeEmailNameKey to LunabeeEmailName,
        LunabeeEmailValueKey to LunabeeEmailValue,
        LunabeePlaceHolderKey to LunabeePlaceHolder,
        MacNameKey to MacName,
        AndroidStudioNameKey to AndroidStudioName,
    ),
)
